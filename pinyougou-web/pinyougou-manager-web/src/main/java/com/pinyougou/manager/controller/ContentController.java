package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.pojo.Content;
import com.pinyougou.service.ContentService;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;

@RestController
@RequestMapping("/content")
public class ContentController {

    @Reference(timeout = 100000)
    private ContentService contentService;

    @GetMapping("/findAsPage")
    public PageResult findAsPage(Content content, Integer pageNum, Integer rows) {
        try {
            PageResult pageResult = contentService.findByPage(content, pageNum, rows);
            return pageResult;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @PostMapping("/save")
    public boolean save(@RequestBody Content content) {
        try {
            contentService.save(content);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @PostMapping("/update")
    public boolean update(@RequestBody Content content) {
        try {
            contentService.update(content);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("/remove")
    public boolean remove(Long[] ids) {
        try {
            contentService.deleteAll(ids);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
