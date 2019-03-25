package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.pojo.Seller;
import com.pinyougou.service.SellerService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller")
public class SellerController {

    @Reference(timeout = 100000)
    private SellerService sellerService;

    @GetMapping("/findAsPage")
    public PageResult findAsPage(Integer pageNum, Integer rows, Seller seller) {
        try {
            if (seller != null && !StringUtils.isEmpty(seller.getName())) {
                seller.setName(new String(seller.getName().getBytes("ISO8859-1"), "UTF-8"));
            }
            if (seller != null && !StringUtils.isEmpty(seller.getNickName())) {
                seller.setNickName(new String(seller.getNickName().getBytes("ISO8859-1"), "UTF-8"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        PageResult pageResult = sellerService.findByPage(seller, pageNum, rows);
        return pageResult;
    }

    @GetMapping("/updateStatus")
    public boolean updateStatus(String sellerId, Integer status) {
        try {
            sellerService.updateStatus(sellerId, status);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
