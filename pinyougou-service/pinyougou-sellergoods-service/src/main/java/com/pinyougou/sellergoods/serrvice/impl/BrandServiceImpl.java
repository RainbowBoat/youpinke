package com.pinyougou.sellergoods.serrvice.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.mapper.BrandMapper;
import com.pinyougou.pojo.Brand;
import com.pinyougou.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Service(interfaceClass = com.pinyougou.service.BrandService.class)
@Transactional
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandMapper brandMapper;

    @Override
    public List<Brand> findAll() {
        List<Brand> list = brandMapper.selectAll();
        return list;
    }

    @Override
    public List<Brand> findAll2() {
        PageInfo<Brand> pageInfo = PageHelper.startPage(1, 10).doSelectPageInfo(new ISelect() {
            @Override
            public void doSelect() {
                brandMapper.selectAll();
            }
        });

        System.out.println("总记录数" + pageInfo.getTotal());
        System.out.println("总页数" + pageInfo.getPages());
        return pageInfo.getList();
    }

    @Override
    public void save(Brand brand) {
        List<Brand> brands = brandMapper.findByName(brand.getName());
        if (brands != null && brands.size() > 0) {
            brandMapper.update(brand);
        } else {
            brandMapper.add(brand);
        }
    }

    @Override
    public PageResult findAsPage(Integer pageNum, Integer rows, Brand brand) {
        PageInfo<Brand> pageInfo = PageHelper.startPage(pageNum, rows).doSelectPageInfo(new ISelect() {
            @Override
            public void doSelect() {
                brandMapper.findAll(brand);
            }
        });
        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public void deleteSelections(long[] ids) {
        if (ids != null && ids.length > 0) {
            brandMapper.deleteSelections(ids);
        }
    }

    @Override
    public List<Map<String, Object>> findAllByIdAndName() {
        return brandMapper.findAllByIdAndName();
    }


}
