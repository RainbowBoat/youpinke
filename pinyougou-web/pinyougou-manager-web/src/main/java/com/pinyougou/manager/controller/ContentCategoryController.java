package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.pojo.ContentCategory;
import com.pinyougou.service.ContentCategoryService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;

@RestController
@RequestMapping("/contentCategory")
public class ContentCategoryController {

    @Reference(timeout = 100000)
    private ContentCategoryService contentCategoryService;

    @GetMapping("/findAsPage")
    public PageResult findAsPage(ContentCategory contentCategory, Integer pageNum, Integer rows) {
        try {

            if (!StringUtils.isEmpty(contentCategory.getName())) {
                contentCategory.setName(new String(contentCategory.getName().getBytes("ISO8859-1"), "UTF-8"));
            }

            PageResult pageResult = contentCategoryService.findByPage(contentCategory, pageNum, rows);
            return pageResult;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @GetMapping("/remove")
    public boolean remove(Long[] ids) {
        try {
            contentCategoryService.deleteAll(ids);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @PostMapping("/save")
    public boolean save(@RequestBody ContentCategory contentCategory) {
        try {
            if (!StringUtils.isEmpty(contentCategory.getName())) {
                contentCategory.setName(new String(contentCategory.getName().getBytes("ISO8859-1"), "UTF-8"));
            }
            contentCategoryService.save(contentCategory);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @PostMapping("/update")
    public boolean update(@RequestBody ContentCategory contentCategory) {
        try {
            if (!StringUtils.isEmpty(contentCategory.getName())) {
                contentCategory.setName(new String(contentCategory.getName().getBytes("ISO8859-1"), "UTF-8"));
            }
            contentCategoryService.update(contentCategory);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("/findAll")
    public List<ContentCategory> findAll() {
        try {
            return contentCategoryService.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
