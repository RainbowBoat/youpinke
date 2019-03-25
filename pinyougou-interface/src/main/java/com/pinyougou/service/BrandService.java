package com.pinyougou.service;

import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.pojo.Brand;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface BrandService {

    List<Brand> findAll();

    List<Brand> findAll2();

    void save(Brand brand);

    PageResult findAsPage(Integer pageNum, Integer rows, Brand brand);

    void deleteSelections(long[] ids);

    List<Map<String, Object>> findAllByIdAndName();
}
