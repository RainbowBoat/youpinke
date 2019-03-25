package com.pinyougou.shop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

import static com.pinyougou.shop.controller.VerifyController.VERIFY_CODE;

@Controller
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @RequestMapping("/login")
    public String login(String username, String password, String code, HttpServletRequest request) {
        if ("post".equalsIgnoreCase(request.getMethod())) {
            String oldCode = (String) request.getSession().getAttribute(VERIFY_CODE);
            if (oldCode.equalsIgnoreCase(code)) {
                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
                Authentication authenticate = authenticationManager.authenticate(token);
                if (authenticate.isAuthenticated()) {
                    SecurityContextHolder.getContext().setAuthentication(authenticate);
                    return "redirect:/admin/index.html";
                }
            }
        }
        return "redirect:/shoplogin.html";
    }
}
