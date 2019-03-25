package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.ItemCat;
import com.pinyougou.service.ItemCatService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/itemCat")
public class ItemCatController {

    @Reference(timeout = 10000)
    private ItemCatService itemCatService;

    @RequestMapping("/findItemsByParentId")
    public List<ItemCat> findItemsByParentId(Integer parentId) {
        List<ItemCat> itemList = itemCatService.findByParentId(parentId);
        return itemList;
    }
}
