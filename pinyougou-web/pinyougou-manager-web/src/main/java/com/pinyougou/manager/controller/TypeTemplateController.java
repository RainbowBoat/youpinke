package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.pojo.TypeTemplate;
import com.pinyougou.service.TypeTemplateService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/template")
public class TypeTemplateController {

    @Reference(timeout = 10000)
    private TypeTemplateService typeTemplateService;

    @RequestMapping("/findAsPage")
    public PageResult findAsPage(Integer pageNum, Integer rows, TypeTemplate typeTemplate) {
        PageResult pageResult = typeTemplateService.finAsPage(pageNum, rows, typeTemplate);
        return pageResult;
    }

    @RequestMapping("/save")
    public boolean save(@RequestBody TypeTemplate typeTemplate) {
        try {
            typeTemplateService.save(typeTemplate);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @RequestMapping("/update")
    public boolean update(@RequestBody TypeTemplate typeTemplate) {
        try {
            typeTemplateService.update(typeTemplate);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


}
