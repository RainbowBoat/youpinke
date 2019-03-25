package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.pojo.Goods;
import com.pinyougou.service.GoodsService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference(timeout = 100000)
    private GoodsService goodsService;

    @GetMapping("/findAsPage")
    public PageResult findAsPage(Goods goods, Integer pageNum, Integer rows) {

        if (!StringUtils.isEmpty(goods.getGoodsName())) {
            try {
                goods.setGoodsName(new String(goods.getGoodsName().getBytes("ISO8859-1"), "UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        PageResult pageResult = goodsService.findAsPage(goods, pageNum, rows);

        return pageResult;
    }

    @GetMapping("/remove")
    public boolean remove(Long[] selectIds) {
        try {
            goodsService.deleteAll(selectIds);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("/pass")
    public boolean pass(Long[] selectIds) {
        try {
            goodsService.passAll(selectIds);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("/refuse")
    public boolean refuse(Long[] selectIds) {
        try {
            goodsService.refuseAll(selectIds);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}
