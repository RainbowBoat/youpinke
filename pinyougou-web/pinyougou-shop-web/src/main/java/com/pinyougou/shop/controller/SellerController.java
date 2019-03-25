package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.Seller;
import com.pinyougou.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/seller")
public class SellerController {

    @Reference(timeout = 100000)
    private SellerService sellerService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/save")
    public boolean save(@RequestBody Seller seller) {
        try {
            seller.setPassword(passwordEncoder.encode(seller.getPassword()));
            sellerService.save(seller);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @PostMapping("/update")
    public boolean update(@RequestBody Seller seller) {
        try {
            sellerService.update(seller);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @GetMapping("/getLoginName")
    public Map<String, String> getLoginName() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, String> map = new HashMap<>();
        map.put("loginName", username);
        return map;
    }
}
