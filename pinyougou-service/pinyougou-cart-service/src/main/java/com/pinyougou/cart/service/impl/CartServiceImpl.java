package com.pinyougou.cart.service.impl;
import java.math.BigDecimal;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.Cart;
import com.pinyougou.mapper.ItemMapper;
import com.pinyougou.pojo.Item;
import com.pinyougou.pojo.OrderItem;
import com.pinyougou.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

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

@Service(interfaceName = "com.pinyougou.service.CartService")
@Transactional
public class CartServiceImpl implements CartService {

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

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

    @Override
    public void saveToRedis(String username, List<Cart> cartList) {
        redisTemplate.boundValueOps("cart_" + username).set(cartList);
    }

    @Override
    public List<Cart> findFromRedis(String username) {
        return (List<Cart>)redisTemplate.boundValueOps("cart_" + username).get() == null ? new ArrayList<Cart>() : (List<Cart>)redisTemplate.boundValueOps("cart_" + username).get();
    }

    @Override
    public List<Cart> mergeCart(List<Cart> cookieCarts, List<Cart> carts) {
        for (Cart cookieCart : cookieCarts) {
            for (OrderItem orderItem : cookieCart.getOrderItems()) {
                carts = addToCart(carts, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return carts;
    }

    @Override
    public void makeTempCart(String userId, String[] itemIds) {
        try {
            makeBufferCartList(userId, itemIds, "tempCartIndexList", "tempCartList");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Cart> findTempCartFromRedis(String userId) {
        return (List<Cart>)redisTemplate.boundHashOps("tempCartList").get(userId);
    }

    @Override
    public void makeTempCartForLicker(String lickedId, String[] itemIds) {
        try {
            makeBufferCartList(lickedId, itemIds, "tempCartForLickerIndexList", "tempCartForLickerList");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Cart> findYourLickedCart(String lickerId, String lickedId) {
        try {
            return (List<Cart>)redisTemplate.boundHashOps("tempCartForLickerList").get(lickedId);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void lickerTempCartForLicked(String lickerId, String lickedId, String[] itemIds) {
        makeBufferCartList(lickedId, itemIds, "lickerTempCartOfLickedIndexList", "lickerTempCartOfLickedList");
    }

    @Override
    public List<Cart> findLickerTempCartForLicked(String lickedId) {
        return (List<Cart>)redisTemplate.boundHashOps("lickerTempCartOfLickedList").get(lickedId);
    }

    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if (sellerId.equals(cart.getSellerId())) {
                return cart;
            }
        }
        return null;
    }

    private OrderItem searchOrderItem(List<OrderItem> orderItems, Long itemId) {
        for (OrderItem orderItem : orderItems) {
            if (itemId.equals(orderItem.getItemId())) {
                return orderItem;
            }
        }
        return null;
    }

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

    private void makeBufferCartList(String userId, String[] itemIds, String buffIndexListNameInRedis, String buffListNameInRedis) {
        try {

            List<Cart> cartList = findFromRedis(userId);
            List<Cart> tempCartList = new ArrayList<>();
            List<Map.Entry<Integer, Integer>> indexList = new ArrayList<>();
            List<String> itemIdArr = Arrays.asList(itemIds);
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

            redisTemplate.boundHashOps(buffIndexListNameInRedis).put(userId, indexList);

            redisTemplate.boundHashOps(buffListNameInRedis).put(userId, tempCartList);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
