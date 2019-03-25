package com.pinyougou.cart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {

    @Autowired
    private HttpServletRequest request;

    @GetMapping("/user/showName")
    public Map<String, Object> showName() {
        Map<String, Object> data = new HashMap<>();
        data.put("loginName", request.getRemoteUser().toString());
        return data;
    }
}
