package com.pinyougou.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.User;
import com.pinyougou.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {


    @Reference(timeout = 100000)
    private UserService userService;
    @Autowired
    private HttpServletRequest request;

    @PostMapping("/save")
    public boolean save(@RequestBody User user, String smsCode) {
        try {
            boolean checkCode = userService.checkCode(user.getPhone(), smsCode);
            if (!checkCode) return false;

            userService.save(user);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("/sendCode")
    public boolean save(String phone) {
        try {
            if (!StringUtils.isEmpty(phone)) {
                return userService.sendCode(phone);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("/showLoginName")
    public Map<String, Object> showLoginName() {
        Map<String, Object> data = new HashMap<>();
        data.put("loginName", request.getRemoteUser().toString());
        return data;
    }
}
