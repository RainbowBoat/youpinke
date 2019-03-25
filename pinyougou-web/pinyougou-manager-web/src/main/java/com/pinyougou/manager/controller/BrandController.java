package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.pojo.Brand;
import com.pinyougou.service.BrandService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@RestController
public class BrandController {

    @Reference(timeout = 10000)
    private BrandService brandService;

    @GetMapping("/brand/findAll")
    public List<Brand> findAll() {
        List<Brand> list = brandService.findAll2();
        return list;
    }

    @PostMapping("/brand/save")
    public boolean save(@RequestBody Brand brand) {
        try {
            brandService.save(brand);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("/brand/findAsPage")
    public PageResult findAsPage(Integer pageNum, Integer rows, Brand brand) {
        if (brand != null && StringUtils.isNoneBlank(brand.getName())) {
            try {
                brand.setName(new String(brand.getName().getBytes("ISO8859-1"), "UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        PageResult pageResult = brandService.findAsPage(pageNum, rows, brand);
        return pageResult;
    }

    @GetMapping("/brand/deleteSelections")
    public boolean deleteSelections(long[] selections) {
        try {
            brandService.deleteSelections(selections);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @GetMapping("/brand/findByIdAndName")
    public List<Map<String, Object>> findAllByIdAndName() {
        return brandService.findAllByIdAndName();
    }
}
