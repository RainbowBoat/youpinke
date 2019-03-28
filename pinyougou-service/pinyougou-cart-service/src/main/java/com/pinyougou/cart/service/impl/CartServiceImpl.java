package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.Cart;
import com.pinyougou.mapper.ItemMapper;
import com.pinyougou.pojo.Item;
import com.pinyougou.pojo.OrderItem;
import com.pinyougou.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * cart_userId - 用户的源购物车
 * userId - tempCartList 用户自己的临时购物车
 * userId - tempCartIndexList 用户自己的临时购物车索引
 * lickedId - tempCartForLickerList lickedId给舔狗准备的临时购物车
 * lickedId - tempCartForLickerIndexList lickedId给舔狗准备的临时购物车索引
 * lickedId - lickerTempCartOfLickedList 舔狗在lickedId准备的临时购物车的基础上进一步筛选的临时购物车
 * lickedId - lickerTempCartOfLickedIndexList 舔狗在lickedId准备的临时购物车的基础上进一步筛选的临时购物车的相对于源购物车的索引
 * lickedId - lickerTempCartOfLickedToMidIndexList 舔狗进一步筛选的购物车相对于licked给舔狗准备的购物车的索引
 * lickedId - "lickerMsg" + lickerId lickedid给舔狗的通知
 */

@Service(interfaceName = "com.pinyougou.service.CartService")
@Transactional
public class CartServiceImpl implements CartService {

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 个人用户商品添加到购物车
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    @Override
    public List<Cart> addToCart(List<Cart> cartList, Long itemId, Integer num) {
        Item item = itemMapper.selectByPrimaryKey(itemId);
        if (item == null || item.getSellerId() == null || item.getSellerId().equals("")) return cartList;

        String sellerId = item.getSellerId();
        Cart cart = searchCartBySellerId(cartList, sellerId);

        if (cart == null) {
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());

            List<OrderItem> orderItemList = new ArrayList<>();
            OrderItem orderItem = createOrderItem(item, num);
            orderItemList.add(orderItem);
            cart.setOrderItems(orderItemList);
            cartList.add(cart);

        } else {

            List<OrderItem> orderItems = cart.getOrderItems();
            OrderItem orderItem = searchOrderItem(orderItems, itemId);

            if (orderItem == null) {
                orderItem = createOrderItem(item, num);
                orderItems.add(orderItem);
            } else {
                orderItem.setNum(orderItem.getNum() + num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getNum() * item.getPrice().doubleValue()));
            }

            if (orderItem.getNum() == 0) {
                orderItems.remove(orderItem);
            }

            if (orderItems.size() == 0) {
                cartList.remove(cart);
            }

        }


        return cartList;
    }

    /**
     * 将个人用户的购物车保存到Redis
     * @param username
     * @param cartList
     */
    @Override
    public void saveToRedis(String username, List<Cart> cartList) {
        redisTemplate.boundValueOps("cart_" + username).set(cartList);
    }

    /**
     * 从购物车中找到个人用户的购物车
     * @param username
     * @return
     */
    @Override
    public List<Cart> findFromRedis(String username) {
        return (List<Cart>)redisTemplate.boundValueOps("cart_" + username).get() == null ? new ArrayList<Cart>() : (List<Cart>)redisTemplate.boundValueOps("cart_" + username).get();
    }

    /**
     * 合并Redis购物车和cookie购物车
     * @param cookieCarts
     * @param carts
     * @return
     */
    @Override
    public List<Cart> mergeCart(List<Cart> cookieCarts, List<Cart> carts) {
        for (Cart cookieCart : cookieCarts) {
            for (OrderItem orderItem : cookieCart.getOrderItems()) {
                carts = addToCart(carts, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return carts;
    }

    /**
     * 个人用户的临时购物车
     * @param userId
     * @param itemIds
     */
    @Override
    public void makeTempCart(String userId, String[] itemIds) {
        try {
            List<Cart> sourceList = findFromRedis(userId);
            makeBufferCartList(userId, itemIds, sourceList,  "tempCartIndexList", "tempCartList");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从Redis中找到个人用户的临时购物车
     * @param userId
     * @return
     */
    @Override
    public List<Cart> findTempCartFromRedis(String userId) {
        return (List<Cart>)redisTemplate.boundHashOps("tempCartList").get(userId);
    }

    /**
     * LickedId给舔狗准备购物车
     * @param lickedId
     * @param itemIds
     */
    @Override
    public void makeTempCartForLicker(String lickedId, String[] itemIds) {
        try {
            List<Cart> sourceList = findFromRedis(lickedId);
            makeBufferCartList(lickedId, itemIds, sourceList, "tempCartForLickerIndexList", "tempCartForLickerList");
            String lickerId = redisTemplate.boundHashOps("theLickerId").get(lickedId).toString();
            makeLickerMsg(lickerId, lickedId + "有" + itemIds.length + "件想买的商品");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 舔狗从Redis找到lickedId给自己准备的购物车
     * @param lickerId
     * @param lickedId
     * @return
     */
    @Override
    public List<Cart> findYourLickedCart(String lickerId, String lickedId) {
        try {
            return (List<Cart>)redisTemplate.boundHashOps("tempCartForLickerList").get(lickedId);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 保存舔狗在lickedId给他准备的购物车的基础上进一步筛选的购物车
     * @param lickerId
     * @param lickedId
     * @param itemIds
     */
    @Override
    public void lickerTempCartForLicked(String lickerId, String lickedId, String[] itemIds) {
        List<Cart> sourceList = findFromRedis(lickedId);
        makeBufferCartList(lickedId, itemIds, sourceList, "lickerTempCartOfLickedIndexList", "lickerTempCartOfLickedList");

        List<Cart> yourLickedCart = findYourLickedCart(lickerId, lickedId);
        makeBufferCartList(lickedId, itemIds, yourLickedCart, "lickerTempCartOfLickedToMidIndexList", "uselessList");
    }

    /**
     * 从Redis中找舔狗在lickedId给他准备的购物车的基础上进一步筛选的购物车
     * @param lickedId
     * @return
     */
    @Override
    public List<Cart> findLickerTempCartForLicked(String lickedId) {
        return (List<Cart>)redisTemplate.boundHashOps("lickerTempCartOfLickedList").get(lickedId);
    }

    /**
     * 根据商家Id找到对应的购物车
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if (sellerId.equals(cart.getSellerId())) {
                return cart;
            }
        }
        return null;
    }

    /**
     * 根据OrderItemId找对应的OrderItem
     * @param orderItems
     * @param itemId
     * @return
     */
    private OrderItem searchOrderItem(List<OrderItem> orderItems, Long itemId) {
        for (OrderItem orderItem : orderItems) {
            if (itemId.equals(orderItem.getItemId())) {
                return orderItem;
            }
        }
        return null;
    }

    /**
     * 将tem转化为OrderItem
     * @param item
     * @param num
     * @return
     */
    private OrderItem createOrderItem(Item item, Integer num) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItemId(item.getId());
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setTitle(item.getTitle());
        orderItem.setPrice(item.getPrice());
        orderItem.setNum(num);
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));
        orderItem.setPicPath(item.getImage());
        orderItem.setSellerId(item.getSellerId());
        return orderItem;
    }

    /**
     * 生成临时购物车
     * @param userId
     * @param itemIds
     * @param sourceList
     * @param buffIndexListNameInRedis
     * @param buffListNameInRedis
     */
    private void makeBufferCartList(String userId, String[] itemIds, List<Cart> sourceList ,String buffIndexListNameInRedis, String buffListNameInRedis) {
        try {

            List<Cart> cartList = sourceList;
            List<Cart> tempCartList = new ArrayList<>();
            List<Map.Entry<Integer, Integer>> indexList = new ArrayList<>();

            // asList的返回对象是一个Arrays内部类,并没有实现集合的修改方法。Arrays.asList体现的是适配器模式，只是转换接口，后台的数据仍是数组。
            List<String> itemIdArr = Arrays.asList(itemIds);
            // 所以这里需要创建一个ArrayList
            ArrayList<String> itemIdList = new ArrayList<>(itemIdArr);

            // 根据用户选中的id, 从购物车中复制一份到临时购物车中
            if (cartList != null && cartList.size() > 0) {
                for (Cart cart : cartList) {
                    // 获得这个购物车在购物车中的索引
                    int cartIndex = cartList.indexOf(cart);
                    // 遍历cart的orderItems, 将id相同的放到新的cart中
                    // 获得原购物集合中该商家的购物车
                    List<OrderItem> orderItems = cart.getOrderItems();
                    // 新建临时购物车
                    if (orderItems != null && orderItems.size() > 0) {
                        Cart newCart = new Cart();
                        List<OrderItem> newOrderItems = new ArrayList<>();
                        Iterator<String> iterator = itemIdList.iterator();
                        while (iterator.hasNext()) {
                            String itemId = iterator.next();
                            for (OrderItem orderItem : orderItems) {
                                int itemIndex = orderItems.indexOf(orderItem);
                                if (Long.parseLong(itemId) == orderItem.getItemId()) {
                                    newOrderItems.add(orderItem);
                                    newCart.setSellerId(cart.getSellerId());
                                    newCart.setSellerName(cart.getSellerName());

                                    // 将这个商品的二维索引存储起来
                                    Map.Entry<Integer, Integer> entry = new AbstractMap.SimpleEntry<Integer, Integer>(cartIndex, itemIndex);
                                    indexList.add(entry);
                                    iterator.remove();
                                }
                            }

                        }

                        newCart.setOrderItems(newOrderItems);

                        tempCartList.add(newCart);
                    }
                }
            }

            redisTemplate.boundHashOps(buffIndexListNameInRedis).put(userId, indexList);

            redisTemplate.boundHashOps(buffListNameInRedis).put(userId, tempCartList);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    /**
     * 给舔狗留言
     * @param lickedId
     * @param msg
     */
//    private void makeLickerMsg(String lickerId, String lickedId, String msg) {
//        // 得到当前用户对改舔狗的留言
//        List<String> lickerMsgList = (List<String>) redisTemplate.boundHashOps("lickerMsg" + lickerId).get(lickedId);
//        // 判断集合有效性
//        if (lickerMsgList == null || lickerMsgList.size() == 0) {
//            lickerMsgList = new ArrayList<>();
//        }
//        lickerMsgList.add(msg);
//
//        // 将消息存储
//        redisTemplate.boundHashOps("lickerMsg" + lickerId).put(lickedId, lickerMsgList);
//    }
}
