package com.pinyougou.sellergoods.serrvice.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.mapper.SpecificationOptionMapper;
import com.pinyougou.mapper.TypeTemplateMapper;
import com.pinyougou.pojo.Specification;
import com.pinyougou.pojo.SpecificationOption;
import com.pinyougou.pojo.TypeTemplate;
import com.pinyougou.service.SpecificationOptionService;
import com.pinyougou.service.TypeTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Service(interfaceClass = com.pinyougou.service.TypeTemplateService.class)
@Transactional
public class TypeTemplateServiceImpl implements TypeTemplateService {

    @Autowired
    private TypeTemplateMapper typeTemplateMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SpecificationOptionMapper specificationOptionMapper;

    @Override
    public void save(TypeTemplate typeTemplate) {
        typeTemplateMapper.insertSelective(typeTemplate);
    }

    @Override
    public void update(TypeTemplate typeTemplate) {
        typeTemplateMapper.updateByPrimaryKeySelective(typeTemplate);
    }

    @Override
    public void delete(Serializable id) {

    }

    @Override
    public void deleteAll(Serializable[] ids) {

    }

    @Override
    public TypeTemplate findOne(Serializable id) {
        return typeTemplateMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<TypeTemplate> findAll() {
        return typeTemplateMapper.selectByExample(null);
    }

    @Override
    public List<TypeTemplate> findByPage(TypeTemplate typeTemplate, int page, int rows) {
        return null;
    }

    //将品牌和规格放入Redis
    private void saveToRedis() {
        List<TypeTemplate> templateList = findAll();
        for (TypeTemplate typeTemplate : templateList) {
            List<Map> brandList = JSON.parseArray(typeTemplate.getBrandIds(), Map.class);
            redisTemplate.boundHashOps("brandList").put(typeTemplate.getId(), brandList);

            List<Map> specList = findSpecByTemplateId(typeTemplate.getId());
            redisTemplate.boundHashOps("specList").put(typeTemplate.getId(), specList);
        }
        System.out.println("将品牌和规格放入Redis");
    }

    @Override
    public PageResult finAsPage(Integer pageNum, Integer rows, TypeTemplate typeTemplate) {
        PageInfo<TypeTemplate> pageInfo = PageHelper.startPage(pageNum, rows).doSelectPageInfo(new ISelect() {
            @Override
            public void doSelect() {
                typeTemplateMapper.findAll(typeTemplate);
            }
        });
        saveToRedis();
        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public List<Map> findSpecByTemplateId(Long id) {

        TypeTemplate typeTemplate = typeTemplateMapper.selectByPrimaryKey(id);

        List<Map> maps = JSON.parseArray(typeTemplate.getSpecIds(), Map.class);

        for (Map map : maps) {
            Object specId = map.get("id");
            SpecificationOption specificationOption = new SpecificationOption();
            specificationOption.setSpecId(Long.valueOf(specId.toString()));
            List<SpecificationOption> options = specificationOptionMapper.select(specificationOption);
            map.put("options", options);
        }
        return maps;
    }
}
