package com.pinyougou.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.service.LickService;
import com.pinyougou.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/licker")
public class LickerController {

    @Reference(timeout = 100000)
    private LickService lickService;

    @Reference(timeout = 100000)
    private UserService userService;

    @Autowired
    private HttpServletRequest request;

    @PostMapping("/beALicker")
    public boolean beALicker(@RequestBody Map<String, String> licked) {
        try {

            String lickerId = request.getRemoteUser();

            String lickedId = licked.get("userId");
            String phone = licked.get("phone");

            if (!userService.checkPhone(lickedId, phone)) {
                return false;
            }

            lickService.beALicker(lickerId, lickedId);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    @GetMapping("/findLickeds")
    public List<String> findLickeds() {
        String lickerId = request.getRemoteUser();

        List<String> lickedLIst = lickService.findLickeds(lickerId);

        return lickedLIst;
    }

    @GetMapping("/beAMan")
    public boolean beAMan(String lickedId) {
        try {
            String lickerId = request.getRemoteUser();

            lickService.brokeUp(lickerId, lickedId);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("/getLickerMsg")
    public List<String> getLickerMsg() {
        String lickerId = request.getRemoteUser();

        List<String> lickedMsgList = lickService.getLickerMsg(lickerId);

        if (lickedMsgList == null || lickedMsgList.size() == 0) {
            lickedMsgList = new ArrayList<>();
        }

        return lickedMsgList;
    }

}
