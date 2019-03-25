package com.pinyougou.item.controller;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {

    @GetMapping("/user/showLoginName")
    public Map<String, Object> showLoginName(HttpServletRequest request) {
        Map<String, Object> data = new HashMap<>();
        String username = request.getRemoteUser();
        if (!StringUtils.isEmpty(username)) {
            data.put("loginName", username);
        }
        return data;
    }
}
