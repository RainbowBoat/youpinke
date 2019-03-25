package com.pinyougou.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.mapper.ContentCategoryMapper;
import com.pinyougou.pojo.ContentCategory;
import com.pinyougou.service.ContentCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

@Service(interfaceName = "com.pinyougou.service.ContentCategoryService")
@Transactional
public class ContentCategoryServiceImpl implements ContentCategoryService {

    @Autowired
    private ContentCategoryMapper contentCategoryMapper;

    @Override
    public void save(ContentCategory contentCategory) {
        try {
            contentCategoryMapper.insertSelective(contentCategory);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(ContentCategory contentCategory) {
        try {
            contentCategoryMapper.updateByPrimaryKeySelective(contentCategory);
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
            contentCategoryMapper.deleteAll(ids);
        } catch (Exception e) {
            e.printStackTrace();


        }
    }

    @Override
    public ContentCategory findOne(Serializable id) {
        return null;
    }

    @Override
    public List<ContentCategory> findAll() {
        try {
            return contentCategoryMapper.selectAll();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PageResult findByPage(ContentCategory contentCategory, int page, int rows) {
        PageInfo<ContentCategory> pageInfo = PageHelper.startPage(page, rows).doSelectPageInfo(new ISelect() {
            @Override
            public void doSelect() {
                contentCategoryMapper.selectAll();
            }
        });

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }
}
