package com.pinyougou.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.service.ItemSearchService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ItemSearchController {

    @Reference(timeout = 100000)
    private ItemSearchService itemSearchService;

    @PostMapping("/Search")
    public Map<String, Object> search(@RequestBody Map<String, Object> params) {
        Map<String, Object> data = itemSearchService.search(params);
        return data;
    }

}
