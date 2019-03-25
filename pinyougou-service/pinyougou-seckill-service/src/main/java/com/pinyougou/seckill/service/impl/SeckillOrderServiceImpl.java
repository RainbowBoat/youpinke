package com.pinyougou.seckill.service.impl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.SeckillGoodsMapper;
import com.pinyougou.mapper.SeckillOrderMapper;
import com.pinyougou.pojo.SeckillGoods;
import com.pinyougou.pojo.SeckillOrder;
import com.pinyougou.service.SeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

@Service(interfaceName = "com.pinyougou.service.SeckillOrderService")
@Transactional
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;

    @Override
    public void save(SeckillOrder seckillOrder) {

    }

    @Override
    public void update(SeckillOrder seckillOrder) {

    }

    @Override
    public void delete(Serializable id) {

    }

    @Override
    public void deleteAll(Serializable[] ids) {

    }

    @Override
    public SeckillOrder findOne(Serializable id) {
        return null;
    }

    @Override
    public List<SeckillOrder> findAll() {
        return null;
    }

    @Override
    public List<SeckillOrder> findByPage(SeckillOrder seckillOrder, int page, int rows) {
        return null;
    }

    @Override
    public synchronized void submitOrder(Long id, String userId) {
        try {

            // 从Redis中取出秒杀货物列表
            SeckillGoods seckillGoods = (SeckillGoods)redisTemplate.boundHashOps("seckillGoodsList").get(id);
            if (seckillGoods != null && seckillGoods.getStockCount() > 0) {
                Integer newStockCount = seckillGoods.getStockCount() - 1;
                seckillGoods.setStockCount(newStockCount);

                // 判断这次该商品库存减一后是否还有货
                if (newStockCount == 0) {
                    seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
                    // 如果没库存了就把缓存中该货物删除
                    redisTemplate.boundHashOps("seckillGoodsList").delete(id);
                } else {
                    // 如果还有货就把修改了之后的库存数量更新到Redis
                    redisTemplate.boundHashOps("seckillGoodsList").put(id, seckillGoods);
                }

                SeckillOrder seckillOrder = new SeckillOrder();
                seckillOrder.setId(idWorker.nextId());
                seckillOrder.setSeckillId(id);
                seckillOrder.setMoney(seckillGoods.getCostPrice());
                seckillOrder.setUserId(userId);
                seckillOrder.setSellerId(seckillGoods.getSellerId());
                seckillOrder.setCreateTime(new Date());
                seckillOrder.setStatus("0");

                // 将秒杀订单数据存储到Redis 但是这种格式存储的话, 如果同个用户再下一单不就会覆盖了吗
                redisTemplate.boundHashOps("seckillOrderList").put(userId, seckillOrder);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SeckillOrder findOneFromRedis(String userId) {
        return (SeckillOrder) redisTemplate.boundHashOps("seckillOrderList").get(userId);
    }

    @Override
    public void updateSeckillOrderStatus(String userId, String transaction_id) {
        try {
            SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("seckillOrderList").get(userId);
            if (seckillOrder != null) {
                seckillOrder.setPayTime(new Date());
                seckillOrder.setStatus("1");
                seckillOrder.setTransactionId(transaction_id);
                seckillOrderMapper.insertSelective(seckillOrder);
                redisTemplate.boundHashOps("seckillOrderList").delete(userId);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<SeckillOrder> findOrderByTimeOut() {
        try {

            List<SeckillOrder> seckillOrderList = new ArrayList<>();
            List<SeckillOrder> allSeckillOrderList = (List<SeckillOrder>)redisTemplate.boundHashOps("seckillOrderList").values();

            if (allSeckillOrderList != null && allSeckillOrderList.size() > 0) {
                long deadTime = new Date().getTime() - 60;
                for (SeckillOrder seckillOrder : allSeckillOrderList) {
                    if (seckillOrder.getCreateTime().getTime() < deadTime) {
                        seckillOrderList.add(seckillOrder);
                    }

                }
            }

            return seckillOrderList;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteFromRedis(SeckillOrder seckillOrder){
        try{
            // 删除Redis缓存中的订单
            redisTemplate.boundHashOps("seckillOrderList")
                    .delete(seckillOrder.getUserId());
            /** ######## 恢复库存数量 #######*/
            // 从Redis查询秒杀商品
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate
                    .boundHashOps("seckillGoodsList")
                    .get(seckillOrder.getSeckillId());
            // 判断缓存中是否存在该商品
            if (seckillGoods != null){
                // 修改缓存中秒杀商品的库存
                seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
            }else{ // 代表秒光
                // 从数据库查询该商品
                seckillGoods = seckillGoodsMapper.
                        selectByPrimaryKey(seckillOrder.getSeckillId());
                // 设置秒杀商品库存数量
                seckillGoods.setStockCount(1);
            }
            // 存入缓存
            redisTemplate.boundHashOps("seckillGoodsList")
                    .put(seckillOrder.getSeckillId(), seckillGoods);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
