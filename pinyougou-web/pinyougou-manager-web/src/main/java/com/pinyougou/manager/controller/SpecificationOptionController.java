package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.SpecificationOption;
import com.pinyougou.service.SpecificationOptionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/specOption")
public class SpecificationOptionController {

    @Reference
    private SpecificationOptionService specificationOptionService;

    @RequestMapping("/findBySpId")
    public List<SpecificationOption> findBySpId(Long specId) {
        return specificationOptionService.findBySpId(specId);
    }
}
