package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/lickedCart")
public class LickedCartController {

    @Autowired
    private HttpServletRequest request;

    @Reference(timeout = 100000)
    private CartService cartService;

    @GetMapping("/letOthers2Pay")
    public boolean letOthers2Pay(String[] itemIds) {

        try {
            String lickedId = request.getRemoteUser();

            cartService.makeTempCartForLicker(lickedId, itemIds);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
