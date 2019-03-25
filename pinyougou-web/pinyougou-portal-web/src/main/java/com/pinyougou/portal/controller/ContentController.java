package com.pinyougou.portal.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.Content;
import com.pinyougou.service.ContentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/content")
public class ContentController {

    @Reference(timeout = 100000)
    private ContentService contentService;

    @GetMapping("/findContentByCategoryId")
    public List<Content> findContentByCategoryId(Long categoryId) {
        try {
            List<Content> contentList = contentService.findByCategoryId(categoryId);
            return contentList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
