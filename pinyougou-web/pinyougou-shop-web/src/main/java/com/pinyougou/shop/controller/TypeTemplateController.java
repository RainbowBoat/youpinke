package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TypeTemplate;
import com.pinyougou.service.TypeTemplateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/template")
public class TypeTemplateController {

    @Reference(timeout = 10000)
    private TypeTemplateService typeTemplateService;

    @GetMapping("/findOne")
    public TypeTemplate findOne(Long id) {
        TypeTemplate typeTemplate = typeTemplateService.findOne(id);
        return typeTemplate;
    }

    @GetMapping("/findSpecByTemplateId")
    public List<Map> findSpecByTemplateId(Long id) {
        return typeTemplateService.findSpecByTemplateId(id);

    }
}
