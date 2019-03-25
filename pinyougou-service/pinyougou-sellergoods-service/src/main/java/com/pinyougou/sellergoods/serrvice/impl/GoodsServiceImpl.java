package com.pinyougou.sellergoods.serrvice.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.*;

@Service(interfaceName = "com.pinyougou.service.GoodsService")
//默认回滚运行时异常?还是所有异常
@Transactional
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private GoodsDescMapper goodsDescMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private SellerMapper sellerMapper;

    @Autowired
    private ItemCatMapper itemCatMapper;

    @Override
    public void save(Goods goods) {
        try {
            goods.setAuditStatus("0");
            goodsMapper.insertSelective(goods);

            //给描述表设置主键Id, 即商品的主键Id
            goods.getGoodsDesc().setGoodsId(goods.getId());
            goodsDescMapper.insertSelective(goods.getGoodsDesc());

            //保存所有item
            List<Item> items = goods.getItems();
            if ("1".equals(goods.getIsEnableSpec())) {
                for (Item item : items) {
                    StringBuilder itemTitle = new StringBuilder();
                    itemTitle.append(goods.getGoodsName());
                    String spec = item.getSpec();
                    Map<String, Object> specMap = JSON.parseObject(spec);
                    for (Object value : specMap.values()) {
                        itemTitle.append(" " + value);
                    }
                    item.setTitle(itemTitle.toString());
                    setItemInfo(item, goods);
                    itemMapper.insertSelective(item);
                }
            }else  {
                /** 创建SKU具体商品对象 */
                Item item = new Item();
                /** 设置SKU商品的标题 */
                item.setTitle(goods.getGoodsName());
                /** 设置SKU商品的价格 */
                item.setPrice(goods.getPrice());
                /** 设置SKU商品库存数据 */
                item.setNum(9999);
                /** 设置SKU商品启用状态 */
                item.setStatus("1");
                /** 设置是否默认*/
                item.setIsDefault("1");
                /** 设置规格选项 */
                item.setSpec("{}");
                setItemInfo(item, goods);
                itemMapper.insertSelective(item);
            }
        } catch (Exception e) {
            //如果不用try catch就不会提示前端
            throw new RuntimeException(e);
        }
    }

    private void setItemInfo(Item item, Goods goods) {
        /**
         * 保存item
         */
        //查询品牌名称
        Brand brand = brandMapper.selectByPrimaryKey(goods.getBrandId());

        //查询商家名称
        Seller seller = sellerMapper.selectByPrimaryKey(goods.getSellerId());

        //查询三级目录
        ItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id());

        //获取图片url
        String itemImages = goods.getGoodsDesc().getItemImages();
        List<Map> images = JSON.parseArray(itemImages, Map.class);
        String url = "";
        for (Map image : images) {
            url = (String)image.get("url");
            break;
        }
            item.setImage(url);
            item.setCategoryid(goods.getCategory3Id());
            item.setCreateTime(new Date());
            item.setUpdateTime(new Date());
            item.setGoodsId(goods.getId());
            item.setSellerId(goods.getSellerId());
            item.setCategory(itemCat.getName());
            item.setBrand(brand.getName());
            item.setSeller(seller.getNickName());
    }

    @Override
    public void update(Goods goods) {

    }

    @Override
    public void delete(Serializable id) {

    }

    @Override
    public void deleteAll(Serializable[] ids) {
        if (ids == null || ids.length == 0) return;
        try {
            goodsMapper.deleteAll(ids);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Goods findOne(Serializable id) {
        return null;
    }

    @Override
    public List<Goods> findAll() {
        return null;
    }

    @Override
    public PageResult findByPage(Goods goods, int page, int rows) {
        PageInfo<Goods> pageInfo = PageHelper.startPage(page, rows).doSelectPageInfo(new ISelect() {
            @Override
            public void doSelect() {
                goodsMapper.findAsPage(goods);
            }
        });

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public PageResult findAsPage(Goods goods, Integer pageNum, Integer rows) {

        PageInfo<Map<String, Object>> pageInfo = PageHelper.startPage(pageNum, rows).doSelectPageInfo(new ISelect() {
            @Override
            public void doSelect() {
                goodsMapper.findAll(goods);
            }
        });

        List<Map<String, Object>> maps = pageInfo.getList();

        Iterator<Map<String, Object>> iterator = maps.iterator();
        while (iterator.hasNext()) {
            Map<String, Object> map = iterator.next();
            if ("1".equals(map.get("isDelete"))) iterator.remove();
            if (map.get("category1Id") != null && map.get("category1Id") != null) map.put("category1Id", itemCatMapper.selectByPrimaryKey(map.get("category1Id")).getName());
            if (map.get("category2Id") != null && map.get("category2Id") != null) map.put("category2Id", itemCatMapper.selectByPrimaryKey(map.get("category2Id")).getName());
            if (map.get("category3Id") != null && map.get("category3Id") != null) map.put("category3Id", itemCatMapper.selectByPrimaryKey(map.get("category3Id")).getName());
        }

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());

    }

    @Override
    public void passAll(Long[] ids) {
        if (ids == null || ids.length == 0) return;
        try {
            goodsMapper.updateAll(ids, 1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void refuseAll(Long[] ids) {
        if (ids == null || ids.length == 0) return;
        try {
            goodsMapper.updateAll(ids, 2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void upAll(Long[] ids) {
        try {
            goodsMapper.updateMarketable(ids, "1");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void downAll(Long[] ids) {
        try {
            goodsMapper.updateMarketable(ids, "0");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> getGoods(Long goodsId) {
        try {
            Map<String, Object> dataModel = new HashMap<>();
            Goods goods = goodsMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goods", goods);
            GoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goodsDesc", goodsDesc);

            String itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();

            dataModel.put("itemCat1", itemCat1);
            dataModel.put("itemCat2", itemCat2);
            dataModel.put("itemCat3", itemCat3);

            //查询商品分类
            Example example = new Example(Item.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("status", 1);
            criteria.andEqualTo("goodsId", goodsId);
            example.orderBy("isDefault").desc();
            List<Item> itemList = itemMapper.selectByExample(example);
            dataModel.put("itemList", JSON.toJSONString(itemList));

            return dataModel;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Item> findItemByGoodsId(Long[] ids) {
        try {
            Example example = new Example(Item.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andIn("goodsId", Arrays.asList(ids));
            return itemMapper.selectByExample(example);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
