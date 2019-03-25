package com.pinyougou.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.PayLog;
import com.pinyougou.service.PayLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.Serializable;
import java.util.List;

@Service(interfaceName = "com.pinyougou.service.PayLogService")
public class PayLogServiceImpl implements PayLogService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void save(PayLog payLog) {

    }

    @Override
    public void update(PayLog payLog) {

    }

    @Override
    public void delete(Serializable id) {

    }

    @Override
    public void deleteAll(Serializable[] ids) {

    }

    @Override
    public PayLog findOne(Serializable id) {
        return null;
    }

    @Override
    public List<PayLog> findAll() {
        return null;
    }

    @Override
    public List<PayLog> findByPage(PayLog payLog, int page, int rows) {
        return null;
    }

    @Override
    public PayLog findFromRedis(String userId) {
        PayLog payLog = (PayLog) redisTemplate.boundValueOps("payLog_" + userId).get();
        return payLog;
    }
}
