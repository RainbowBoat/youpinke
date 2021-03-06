package com.pinyougou.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.Cart;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.OrderItemMapper;
import com.pinyougou.mapper.OrderMapper;
import com.pinyougou.mapper.PayLogMapper;
import com.pinyougou.pojo.Order;
import com.pinyougou.pojo.OrderItem;
import com.pinyougou.pojo.PayLog;
import com.pinyougou.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

/**
 * cart_userId - 用户的源购物车
 * userId - tempCartList 用户自己的临时购物车
 * userId - tempCartIndexList 用户自己的临时购物车索引
 * lickedId - tempCartForLickerList lickedId给舔狗准备的临时购物车
 * lickedId - tempCartForLickerIndexList lickedId给舔狗准备的临时购物车索引
 * lickedId - lickerTempCartOfLickedList 舔狗在lickedId准备的临时购物车的基础上进一步筛选的临时购物车
 * lickedId - lickerTempCartOfLickedIndexList 舔狗在lickedId准备的临时购物车的基础上进一步筛选的临时购物车的索引
 */

@Service(interfaceName = "com.pinyougou.service.OrderService")
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private PayLogMapper payLogMapper;

    @Override
    public void save(Order order) {
        try {
            String username = order.getUserId();
            List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("tempCartList").get(order.getUserId());

            PayLog payLog = createPayLog(order, cartList, "1");

            // 将支付单保存到Redis
            redisTemplate.boundValueOps("payLog_" + order.getUserId()).set(payLog);

            List<Cart> sourceCartList = (List<Cart>)redisTemplate.boundValueOps("cart_" + username).get();

            // 遍历临时集合, 遍历源集合, 判断两集合中遍历到的商品的itemId是否相同, 相同则删除
            List<Map.Entry<Integer, Integer>> indexList = (List<Map.Entry<Integer, Integer>>) redisTemplate.boundHashOps("tempCartIndexList").get(username);


            List<Cart> afterList = deleteTempCartList(cartList, sourceCartList, indexList, username);
            redisTemplate.boundValueOps("cart_" + username).set(afterList);

            // 将用户的临时购物车删除
            redisTemplate.boundHashOps("tempCartList").delete(username);
            redisTemplate.boundHashOps("tempCartIndexList").delete(username);

//            redisTemplate.delete("cart_" + username);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Order order) {

    }

    @Override
    public void delete(Serializable id) {

    }

    @Override
    public void deleteAll(Serializable[] ids) {

    }

    @Override
    public Order findOne(Serializable id) {
        return null;
    }

    @Override
    public List<Order> findAll() {
        return null;
    }

    @Override
    public List<Order> findByPage(Order order, int page, int rows) {
        return null;
    }

    @Override
    public void updateStraightOrderStatus(String outTradeNo, String transactionId) {

        PayLog payLog = updateOrderStatus(outTradeNo, transactionId, "2");

        redisTemplate.delete("payLog_" + payLog.getUserId());
    }

    @Override
    public void saveLickOrder(Order order) {
//        String lickerId = order.getUserId();
        String lickedId = order.getReceiver();

        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("lickerTempCartOfLickedList").get(lickedId);
        PayLog payLog = createPayLog(order, cartList, "3");

        redisTemplate.boundHashOps("lickPayLog").put(lickedId, payLog);

        // 将舔狗进一部筛选的购物车的数据从licked的源购物车中删除
        List<Cart> sourceCartList = (List<Cart>)redisTemplate.boundValueOps("cart_" + lickedId).get();
        List<Map.Entry<Integer, Integer>> indexList = (List<Map.Entry<Integer, Integer>>)redisTemplate.boundHashOps("lickerTempCartOfLickedIndexList").get(lickedId);
        List<Cart> afterListOfSourceList = deleteTempCartList(cartList, sourceCartList, indexList, lickedId);
        redisTemplate.boundValueOps("cart_" + lickedId).set(afterListOfSourceList);

        // 将licked准备给舔狗的购物车中舔狗已支付的商品删除
        List<Cart> tempCartForLickerList = (List<Cart>)redisTemplate.boundHashOps("tempCartForLickerList").get(lickedId);
        // 舔狗筛选的购物车相对于中间购物车的索引
        List<Map.Entry<Integer, Integer>> tempCartForLickerIndexList = (List<Map.Entry<Integer, Integer>>)redisTemplate.boundHashOps("lickerTempCartOfLickedToMidIndexList").get(lickedId);
        List<Cart> afterListOfMidList = deleteTempCartList(cartList, tempCartForLickerList, tempCartForLickerIndexList, lickedId);
        redisTemplate.boundHashOps("tempCartForLickerList").put(lickedId, afterListOfMidList);

        // 将舔狗进一部筛选的购物车删除
        redisTemplate.boundHashOps("lickerTempCartOfLickedList").delete(lickedId);
        redisTemplate.boundHashOps("lickerTempCartOfLickedIndexList").delete(lickedId);
        redisTemplate.boundHashOps("lickerTempCartOfLickedToMidIndexList").delete(lickedId);

        // 通知licked
        makeLickedMsg(lickedId, "你的舔狗已帮你下单");
    }

    @Override
    public void updateLickOrderStatus(String outTradeNo, String transactionId, String lickedId) {
        PayLog payLog = updateOrderStatus(outTradeNo, transactionId, "4");
        redisTemplate.boundHashOps("lickPayLog").delete(lickedId);
    }

    // 从源购物车集合中删除临时购物车中集合中的商品
    private List<Cart> deleteTempCartList(List<Cart> tempCartList, List<Cart> sourceCartList, List<Map.Entry<Integer, Integer>> indexList, String userId) {

        Collections.reverse(indexList);

        for (Map.Entry<Integer, Integer> entry : indexList) {
            int cartIndex = entry.getKey().intValue();
            int itemIndex = entry.getValue().intValue();
            Cart cart = sourceCartList.get(cartIndex);
            List<OrderItem> orderItems = cart.getOrderItems();
            orderItems.remove(itemIndex);
            // 判断这个商家的商品是否都被删除了, 如果是则把这个商家从购物车中删除
            if (orderItems == null || orderItems.size() == 0) {
                sourceCartList.remove(cart);
            }
        }
        return sourceCartList;
    }

    // 生成支付订单对象
    private PayLog createPayLog(Order order, List<Cart> cartList, String orderStatus) {
        // 保存订单编号集合
        StringBuilder orderIdsBuilder = new StringBuilder();
        double totalMoney = 0;

        for (Cart cart : cartList) {
            Order newOrder = new Order();

            Long id = idWorker.nextId();
            newOrder.setOrderId(id);
            newOrder.setPaymentType(order.getPaymentType());
            newOrder.setStatus(orderStatus);
            newOrder.setCreateTime(new Date());
            newOrder.setUpdateTime(newOrder.getCreateTime());
            newOrder.setUserId(order.getUserId());
            newOrder.setReceiverAreaName(order.getReceiverAreaName());
            newOrder.setReceiverMobile(order.getReceiverMobile());
            newOrder.setReceiver(order.getReceiver());
            newOrder.setSourceType("2");
            newOrder.setSellerId(cart.getSellerId());

            double payment = 0;

            for (OrderItem orderItem : cart.getOrderItems()) {
                Long orderItemId = idWorker.nextId();
                orderItem.setId(orderItemId);
                orderItem.setOrderId(newOrder.getOrderId());
                payment += orderItem.getPrice().doubleValue();
                orderItemMapper.insert(orderItem);
            }

            newOrder.setPayment(new BigDecimal(payment));
            orderMapper.insert(newOrder);

            orderIdsBuilder.append(String.valueOf(id));
            orderIdsBuilder.append("-");
            totalMoney += payment;
        }

        String orderIds = orderIdsBuilder.toString();


        PayLog payLog = new PayLog();
        payLog.setOutTradeNo(String.valueOf(idWorker.nextId()));
        payLog.setCreateTime(new Date());
        payLog.setTotalFee((long)(totalMoney*100));
        payLog.setUserId(order.getUserId());
        payLog.setTradeState("0");
        payLog.setOrderList(orderIds.substring(0, orderIds.length()-1));
        payLog.setPayType(order.getPaymentType());

        payLogMapper.insertSelective(payLog);
//            // 将支付单保存到Redis
//            redisTemplate.boundValueOps("payLog_" + order.getUserId()).set(payLog);

        return payLog;

    }

    // 更新订单状态
    private PayLog updateOrderStatus(String outTradeNo, String transactionId, String orderStatus) {
        PayLog payLog = payLogMapper.selectByPrimaryKey(outTradeNo);
        payLog.setPayTime(new Date());
        payLog.setTransactionId(transactionId);
        payLog.setTradeState("1");
        payLogMapper.updateByPrimaryKeySelective(payLog);

        String[] orderIds = payLog.getOrderList().split("-");
        for (String orderId : orderIds) {
            Order order = new Order();
            order.setOrderId(Long.parseLong(orderId));
            order.setStatus(orderStatus);
            order.setUpdateTime(new Date());
            order.setPaymentTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
        }

        return payLog;

    }
    /**
     * 给舔狗留言
     * @param lickerId
     * @param msg
     */
    private void makeLickerMsg(String lickerId, String msg) {
        // 得到舔狗的消息集合
        List<String> lickerMsgList = (List<String>) redisTemplate.boundHashOps("lickerMsg").get(lickerId);
        // 判断集合有效性
        if (lickerMsgList == null || lickerMsgList.size() == 0) {
            lickerMsgList = new ArrayList<>();
        }
        lickerMsgList.add(msg);

        // 将消息存储
        redisTemplate.boundHashOps("lickerMsg").put(lickerId, lickerMsgList);
    }

    /**
     * 给licked留言
     * @param lickedId
     * @param msg
     */
    private void makeLickedMsg(String lickedId, String msg) {
        List<String> lickedMsgList = (List<String>) redisTemplate.boundHashOps("lickedMsg").get(lickedId);
        if (lickedMsgList == null || lickedMsgList.size() == 0) {
            lickedMsgList = new ArrayList<>();
        }
        lickedMsgList.add(msg);
        redisTemplate.boundHashOps("lickedMsg").put(lickedId, lickedMsgList);
    }
}
