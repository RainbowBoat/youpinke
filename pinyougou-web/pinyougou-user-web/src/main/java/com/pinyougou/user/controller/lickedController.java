package com.pinyougou.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.service.LickService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/licked")
public class lickedController {

    @Autowired
    private HttpServletRequest request;

    @Reference(timeout = 100000)
    private LickService lickService;

    @GetMapping("/findLickers")
    public Map<String, Object> findLickers() {
        String userId = request.getRemoteUser();
        Map<String, Object> data = new HashMap<>();

        List<String> lickerList = lickService.findLickers(userId);
        data.put("lilckerList", lickerList);

        String lickerId = lickService.findMyLicker(userId);
        if (lickerId != null && lickerId != "") {
            data.put("licker", lickerId);
        }

        return data;
    }

    @GetMapping("/acceptLicker")
    public boolean acceptLicker(String lickerId) {
        try {
            String lickedId = request.getRemoteUser();

            lickService.acceptLicker(lickerId, lickedId);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("/refuseLicker")
    public boolean refuseLicker(String lickerId) {
        try {
            String lickedId = request.getRemoteUser();

            lickService.refuseLicker(lickerId, lickedId);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("/deleteLicker")
    public boolean deleteLicker(String lickerId) {
        try {
            String lickedId = request.getRemoteUser();

            lickService.brokeUp(lickerId, lickedId);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("/getLickedMsg")
    public List<String> getLickedMsg() {
        String lickedId = request.getRemoteUser();

        List<String> lickedMsgList = lickService.getLickedMsg(lickedId);

        if (lickedMsgList == null || lickedMsgList.size() == 0) {
            lickedMsgList = new ArrayList<>();
        }

        return lickedMsgList;
    }
}
