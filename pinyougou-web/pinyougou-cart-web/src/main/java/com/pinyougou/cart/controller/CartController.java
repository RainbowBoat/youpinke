package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.Cart;
import com.pinyougou.common.util.CookieUtils;
import com.pinyougou.service.CartService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
@CrossOrigin(origins = {"http://item.pinyougou.com", "http://search.pinyougou.com"}, allowCredentials = "true")
public class CartController {

    @Reference
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @GetMapping("/addCart")
    public boolean addCart(Long itemId, Integer num) {
        try {

            String username = request.getRemoteUser();

            List<Cart> cartList = findCart();

            if (StringUtils.isNoneBlank(username)) {
                cartList = cartService.addToCart(cartList, itemId, num);
                cartService.saveToRedis(username, cartList);
            } else {
                cartList = cartService.addToCart(cartList, itemId, num);

                CookieUtils.setCookie(request, response, CookieUtils.CookieName.PINYOUGOU_CART, JSON.toJSONString(cartList), 3600 * 24, true);

            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("/findCart")
    public List<Cart> findCart() {

        String username = request.getRemoteUser();

        List<Cart> carts;

        if (StringUtils.isNoneBlank(username)) {
            carts = cartService.findFromRedis(username);

            String cartStr = CookieUtils.getCookieValue(request, CookieUtils.CookieName.PINYOUGOU_CART, true);

            if (StringUtils.isNoneBlank(cartStr)) {
                List<Cart> cookieCarts = JSON.parseArray(cartStr, Cart.class);

                if (cookieCarts != null && cookieCarts.size() > 0) {
                    carts = cartService.mergeCart(cookieCarts, carts);
                    cartService.saveToRedis(username, carts);
                    CookieUtils.deleteCookie(request, response, CookieUtils.CookieName.PINYOUGOU_CART);
                }
            }
        } else {
            String cartStr = CookieUtils.getCookieValue(request, CookieUtils.CookieName.PINYOUGOU_CART, true);
            if (cartStr == null) {
                cartStr = "[]";
            }
            carts = JSON.parseArray(cartStr, Cart.class);
        }

        return carts;
    }

    @GetMapping("/tempCart")
    public boolean tempCart(String[] itemIds) {
        try {

            String userId = request.getRemoteUser();

            cartService.makeTempCart(userId, itemIds);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("/findTempCartFromRedis")
    public List<Cart> findTempCartFromRedis() {
        String userId = request.getRemoteUser();

        List<Cart> tempCartList = cartService.findTempCartFromRedis(userId);

        return tempCartList;
    }

    @GetMapping("/letOthers2Pay")
    public boolean letOthers2Pay(String[] itemIds) {
        try {

            String userId = request.getRemoteUser();

//            cartService.letOthers2Pay(userId, itemIds);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
