package com.pinyougou.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.common.util.HttpClientUtils;
import com.pinyougou.mapper.UserMapper;
import com.pinyougou.pojo.User;
import com.pinyougou.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

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
            System.out.println("验证码："+code);
            //存入Redis中, 90秒失效
            redisTemplate.boundValueOps(phone).set(code, 90, TimeUnit.SECONDS);
            //调用短信发送接口
            HttpClientUtils httpClientUtils = new HttpClientUtils(false);

            Map<String, String> params = new HashMap<>();
            params.put("phone", phone);
            params.put("signName", signName);
            params.put("templateCode", templateCode);
            params.put("templateParam", "{'code':"+code+"'}");

            String content = httpClientUtils.sendPost(smsUrl, params);
            Map<String, Object> resMap = JSON.parseObject(content, Map.class);

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

    @Override
    public boolean checkPhone(String userId, String phone) {
        Example example = new Example(User.class);

        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username", userId);

        List<User> users = userMapper.selectByExample(example);
        User user = users.get(0);

        if (phone.equals(user.getPhone())) {
            return true;
        } else {
            return false;
        }
    }
    @Override
    public void submit(User user) {
        try {

            // 密码加密
            user.setPassword(DigestUtils.md5Hex(user.getPassword()));
            userMapper.update(user.getPassword(),user.getUsername());
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }

    }

    @Override
    public String findNumber(String user) {
        User user1 =userMapper.findNumber(user);
        return user1.getPhone();
    }

    @Override
    public void saveUser(User user) {
       Long id=userMapper.selectById(user.getUsername());
        user.setId(id);
        userMapper.updateByPrimaryKeySelective(user);
    }

    @Override
    public boolean clickJudge(String code,String phone,String username) {

        String co = redisTemplate.boundValueOps(phone).get();
        if(StringUtils.isNoneBlank(co)&&code.equals(co)){
            User user1= userMapper.selectByPhoneId(username);
            user1.setPhone(phone);
            userMapper.updateByPrimaryKeySelective(user1);
            return true;
        }
        return false;
    }


}
