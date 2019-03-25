package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.SeckillGoods;
import com.pinyougou.service.SeckillGoodsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/seckill")
public class SeckillGoodsController {

    @Reference(timeout = 100000)
    private SeckillGoodsService seckillGoodsService;

    @GetMapping("/findSeckillGoods")
    public List<SeckillGoods> findSeckillGoods() {
        List<SeckillGoods> seckillGoods = seckillGoodsService.findSeckillGoods();
        return seckillGoods;
    }

    @GetMapping("/findOne")
    public SeckillGoods findOne(Long id) {
        SeckillGoods seckillGoods = seckillGoodsService.findOne(id);
        return seckillGoods;
    }
}
