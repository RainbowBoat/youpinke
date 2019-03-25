package com.pinyougou.service;

import com.pinyougou.cart.Cart;

import java.util.List;
import java.util.Map;

public interface CartService {
    List<Cart> addToCart(List<Cart> cartList, Long itemId, Integer num);

    void saveToRedis(String username, List<Cart> cartList);

    List<Cart> findFromRedis(String username);

    List<Cart> mergeCart(List<Cart> cookieCarts, List<Cart> carts);

    void makeTempCart(String userId, String[] itemIds);

    List<Cart> findTempCartFromRedis(String userId);

}
