package com.pinyougou.sms.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.service.SmsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sms")
public class SmsController {

    @Reference(timeout = 100000)
    private SmsService smsService;

    /**
     * 发送短信方法
     * @param phone 手机号码
     * @param signName 签名
     * @param templateCode 短信模板
     * @param templateParam 模板参数(json格式)
     * @return true 发送成功 false 发送失败
     */
    @PostMapping("/sendSms")
    public Map<String, Object> sendSms(String phone, String signName, String templateCode, String templateParam) {
        boolean success = smsService.sendSms(phone, signName, templateCode, templateParam);
        Map<String, Object> map = new HashMap<>();
        map.put("success", success);
        return map;
    }
}
