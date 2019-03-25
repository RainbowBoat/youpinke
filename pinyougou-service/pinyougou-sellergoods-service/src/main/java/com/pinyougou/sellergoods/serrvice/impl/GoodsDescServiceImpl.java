package com.pinyougou.sellergoods.serrvice.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.GoodsDescMapper;
import com.pinyougou.pojo.Goods;
import com.pinyougou.pojo.GoodsDesc;
import com.pinyougou.service.GoodsDescService;
import com.pinyougou.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

@Service(interfaceName = "com.pinyougou.service.GoodsDescService")
@Transactional
public class GoodsDescServiceImpl implements GoodsDescService {

    @Autowired
    private GoodsDescMapper goodsDescMapper;


    @Override
    public void save(GoodsDesc goodsDesc) {
//        try {
//            goodsDescMapper.insertSelective(goodsDesc);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }

    @Override
    public void update(GoodsDesc goodsDesc) {

    }

    @Override
    public void delete(Serializable id) {

    }

    @Override
    public void deleteAll(Serializable[] ids) {

    }

    @Override
    public GoodsDesc findOne(Serializable id) {
        return null;
    }

    @Override
    public List<GoodsDesc> findAll() {
        return null;
    }

    @Override
    public List<GoodsDesc> findByPage(GoodsDesc goodsDesc, int page, int rows) {
        return null;
    }
}
