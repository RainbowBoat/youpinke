package com.pinyougou.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.mapper.ContentMapper;
import com.pinyougou.pojo.Content;
import com.pinyougou.pojo.ContentCategory;
import com.pinyougou.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.List;

@Service(interfaceName = "com.pinyougou.service.ContentService")
@Transactional
public class ContentServiceImpl implements ContentService {

    @Autowired
    private ContentMapper contentMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void save(Content content) {
        if (content == null) return;
        try {
            contentMapper.insertSelective(content);
            redisTemplate.delete("contents");
            System.out.println("清除缓存");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Content content) {
        if (content == null) return;
        try {
            contentMapper.updateByPrimaryKeySelective(content);
            redisTemplate.delete("contents");
            System.out.println("清除缓存");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Serializable id) {

    }

    @Override
    public void deleteAll(Serializable[] ids) {
        if (ids == null || ids.length == 0) return;
        try {
            contentMapper.deleteAll(ids);
            redisTemplate.delete("contents");
            System.out.println("清除缓存");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Content findOne(Serializable id) {
        return null;
    }

    @Override
    public List<Content> findAll() {
        return null;
    }

    @Override
    public PageResult findByPage(Content content, int page, int rows) {
        PageInfo<ContentCategory> pageInfo = PageHelper.startPage(page, rows).doSelectPageInfo(new ISelect() {
            @Override
            public void doSelect() {
                contentMapper.selectAll();
            }
        });

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public List<Content> findByCategoryId(Long categoryId) {

        try {
            List<Content> contents = (List<Content>) redisTemplate.boundValueOps("contents").get();
            if (contents != null && contents.size() > 0) {
                System.out.println("这次是从redis拿的");
                return contents;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Example example = new Example(Content.class);

            Example.Criteria criteria = example.createCriteria();

            criteria.andEqualTo("categoryId", categoryId);

            criteria.andEqualTo("status", "1");

            example.orderBy("sortOrder").asc();

            List<Content> contentList = contentMapper.selectByExample(example);

            try {
                redisTemplate.boundValueOps("contents").set(contentList);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("这次不是从redis拿的");
            return contentList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
