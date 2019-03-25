package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.pojo.Specification;
import com.pinyougou.service.SpecificationService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/spec")
public class SpecificationController {

    @Reference(timeout = 1000000)
    private SpecificationService specificationService;

    @GetMapping("/findAsPage")
    public PageResult findAsPage(Integer pageNum, Integer rows, Specification specification) {
        if (specification != null && !StringUtils.isEmpty(specification.getSpecName())) {
            try {
                specification.setSpecName(new String(specification.getSpecName().getBytes("ISO8859-1"), "UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        PageResult pageResult = specificationService.findAsPage(pageNum, rows, specification);
        return pageResult;
    }

    @PostMapping("/save")
    public boolean save(@RequestBody Specification specification) {
        try {
            specificationService.save(specification);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @PostMapping("/update")
    public boolean update(@RequestBody Specification specification) {
        try {
            specificationService.update(specification);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @GetMapping("/findByIdAndName")
    public List<Map<String, Object>> findByIdAndName() {
        return specificationService.findByIdAndName();
    }
}
