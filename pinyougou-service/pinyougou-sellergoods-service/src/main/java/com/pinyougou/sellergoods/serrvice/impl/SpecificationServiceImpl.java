package com.pinyougou.sellergoods.serrvice.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.mapper.SpecificationMapper;
import com.pinyougou.mapper.SpecificationOptionMapper;
import com.pinyougou.pojo.Specification;
import com.pinyougou.pojo.SpecificationOption;
import com.pinyougou.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Service(interfaceClass = com.pinyougou.service.SpecificationService.class)
@Transactional
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private SpecificationMapper specificationMapper;

    @Autowired
    private SpecificationOptionMapper specificationOptionMapper;

    @Override
    public PageResult findAsPage(Integer pageNum, Integer rows, Specification specification) {
        PageInfo<Specification> pageInfo = PageHelper.startPage(pageNum, rows).doSelectPageInfo(new ISelect() {
            @Override
            public void doSelect() {
                specificationMapper.findAll(specification);
            }
        });
        List<Specification> specificationOptions = pageInfo.getList();
        PageResult pageResult = new PageResult(pageInfo.getTotal(), specificationOptions);
        return pageResult;
    }

    @Override
    public void save(Specification specification) {
        try {
            specificationMapper.insertSelective(specification);
            specificationOptionMapper.save(specification);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public void update(Specification specification) {

    }

    @Override
    public void delete(Serializable id) {

    }

    @Override
    public void deleteAll(Serializable[] ids) {

    }

    @Override
    public Specification findOne(Serializable id) {
        return null;
    }

    @Override
    public List<Specification> findAll() {
        return null;
    }

    @Override
    public List<Specification> findByPage(Specification specification, int page, int rows) {
        return null;
    }

    @Override
    public List<Map<String, Object>> findByIdAndName() {
        return specificationMapper.findByIdAndName();
    }
}
