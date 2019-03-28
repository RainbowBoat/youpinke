package com.pinyougou.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.Areas;
import com.pinyougou.pojo.Cities;
import com.pinyougou.pojo.Provinces;
import com.pinyougou.pojo.User;
import com.pinyougou.service.AreasService;
import com.pinyougou.service.CitiesService;
import com.pinyougou.service.ProvincesService;
import com.pinyougou.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    @Reference(timeout = 100000)
    private UserService userService;
    @Reference(timeout = 10000)
    private ProvincesService provincesService;
    @Reference(timeout = 10000)
    private CitiesService citiesService;
    @Reference(timeout = 10000)
    private AreasService areasService;
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

    @GetMapping("/findAll")
    public List<Provinces>findProvincesByParentId(){
        List<Provinces> ProvincesList= provincesService.findAll();
        return ProvincesList;
    }
    @GetMapping("/findProvincesByTwo")
    public List<Cities>findProvincesByTwo(String provinceId){
        return citiesService.findProvincesByTwo(provinceId);
    }
    @GetMapping("/findProvincesByThree")
    public List<Areas>findProvincesByThree(String cityId){
        return  areasService.findProvincesByThree(cityId);
    }
    @PostMapping("/submit")
    public boolean submit(@RequestBody User user){
        try {
            userService.submit(user);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    @GetMapping("/findNumber")
    public String findNumber(HttpServletRequest request){
        String user= request.getRemoteUser();
        return userService.findNumber(user);
    }
    @PostMapping("/saveUser")
    public boolean saveUser(@RequestBody User user,HttpServletRequest request){
        try {
            String username = request.getRemoteUser();
            user.setUsername(username);
            userService.saveUser(user);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    @GetMapping("/clickJudge")
    public boolean clickJudge(String code,String phone,String codes,HttpServletRequest request){
        try {
           Object oldcode = request.getSession().getAttribute(VerifyController.VERIFY_CODE);
            if(oldcode.equals(codes)){
                    boolean b=(boolean)userService.clickJudge(code,phone,request.getRemoteUser());
                    if(b!=false){
                        return true;
                    }
                }

        } catch (Exception e) {
            e.printStackTrace();

        }
        return false;
    }
}
