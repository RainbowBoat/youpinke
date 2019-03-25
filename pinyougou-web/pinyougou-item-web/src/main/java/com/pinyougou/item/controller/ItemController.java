package com.pinyougou.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.service.GoodsService;
import com.pinyougou.service.ItemService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class ItemController {

    @Reference(timeout = 100000)
    private GoodsService goodsService;

    @GetMapping("/{goodsId}")
    public String getGoods(@PathVariable("goodsId") Long goodsId, Model model) {
        Map<String, Object> data = goodsService.getGoods(goodsId);

        model.addAllAttributes(data);

        return "item";
    }

//    @GetMapping("/{goodsId2}")
//    public String getGood2s(@PathVariable("goodsId") Long goodsId, Model model) {
//        Map<String, Object> data = goodsService.getGoods(goodsId);
//
//        model.addAllAttributes(data);
//
//        return "item";
//    }
}
