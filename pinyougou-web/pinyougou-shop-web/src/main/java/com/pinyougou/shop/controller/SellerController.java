package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.pojo.Seller;
import com.pinyougou.pojo.User;
import com.pinyougou.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import sun.security.util.Password;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
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

    //回显数据  (zhang)
    @GetMapping("/showData")
    public Seller showData(){
        try {
            String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
            Seller seller = sellerService.findOne(sellerId);
            return seller;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    @PostMapping("/savePassword")
    public boolean savePassword(@RequestBody Map<String,String> map){
        try {

            String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
            String newPassword = map.get("newPassword");
            String oldPassword = map.get("password");
           if (sellerService.updatePassword(newPassword,oldPassword,sellerId)){
               return true;
           }else {
               return false;
           }
        }catch (Exception e){
           e.printStackTrace();
        }
        return false;
    }

    @GetMapping("/saveSeller")
    public boolean saveSeller(Seller seller){
        try {
          return  true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return  false;
    }
}
