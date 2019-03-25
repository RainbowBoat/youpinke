package com.pinyougou.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.common.util.HttpClientUtils;
import com.pinyougou.mapper.UserMapper;
import com.pinyougou.pojo.User;
import com.pinyougou.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service(interfaceName = "com.pinyougou.service.UserService")
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Value("${sms.url}")
    private String smsUrl;
    @Value("${sms.signName}")
    private String signName;
    @Value("${sms.templateCode}")
    private String templateCode;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void save(User user) {
        try {
            user.setCreated(new Date());
            user.setUpdated(user.getCreated());
            user.setPassword(DigestUtils.md5Hex(user.getPassword()));
            userMapper.insertSelective(user);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(User user) {

    }

    @Override
    public void delete(Serializable id) {

    }

    @Override
    public void deleteAll(Serializable[] ids) {

    }

    @Override
    public User findOne(Serializable id) {
        return null;
    }

    @Override
    public List<User> findAll() {
        return null;
    }

    @Override
    public List<User> findByPage(User user, int page, int rows) {
        return null;
    }

    @Override
    public boolean sendCode(String phone) {
        try {
            //生成6位随机数
            String code = UUID.randomUUID().toString().replaceAll("-", "").replaceAll("[a-z|A-Z]", "").substring(0, 6);

            //调用短信发送接口
            HttpClientUtils httpClientUtils = new HttpClientUtils(false);

            Map<String, String> params = new HashMap<>();
            params.put("phone", phone);
            params.put("signName", signName);
            params.put("templateCode", templateCode);
            params.put("templateParam", "{\"number\":\"" + code + "\"}");

            String content = httpClientUtils.sendPost(smsUrl, params);
            Map<String, Object> resMap = JSON.parseObject(content, Map.class);

            //存入Redis中, 90秒失效
            redisTemplate.boundValueOps(phone).set(code, 90, TimeUnit.SECONDS);

            return (boolean)resMap.get("success");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean checkCode(String phone, String smsCode) {
        String oldCode = redisTemplate.boundValueOps(phone).get();
        if (oldCode != null) {
            return oldCode.equals(smsCode);
        } else {
            return false;
        }
    }


}
