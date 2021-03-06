package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.pojo.Order;
import com.pinyougou.pojo.PayLog;
import com.pinyougou.service.OrderService;
import com.pinyougou.service.PayLogService;
import com.pinyougou.service.WeChatPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private HttpServletRequest request;

    @Reference(timeout = 100000)
    private OrderService orderService;

    @Reference(timeout = 100000)
    private WeChatPayService weChatPayService;

    @Reference
    private PayLogService payLogService;

    @PostMapping("/saveOrder")
    public boolean saveOrder(@RequestBody Order order) {
        try {
            String username = request.getRemoteUser();
            order.setUserId(username);
            order.setSourceType("2");
            orderService.save(order);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("/genPayCode")
    public Map<String, Object> genPayCode() {
        String userId = request.getRemoteUser();
        PayLog payLog = payLogService.findFromRedis(userId);
        return weChatPayService.genPayCode(payLog.getOutTradeNo(), String.valueOf(payLog.getTotalFee()));
    }

    @GetMapping("/queryPayStatus")
    public Map<String, Object> queryPayStatus(String outTradeNo) {
        Map<String, Object> data = new HashMap<>();
        data.put("payStatus", "3");
        Map<String, String> resultMap = weChatPayService.queryPayStatus(outTradeNo);

        if (resultMap != null && resultMap.size() > 0) {
            if ("SUCCESS".equals(resultMap.get("trade_state"))) {
                data.put("payStatus", "1");
                orderService.updateStraightOrderStatus(outTradeNo, resultMap.get("transaction_id"));
            }
            if ("NOTPAY".equals(resultMap.get("trade_state"))) data.put("payStatus", "2");
        }

        return data;
    }

    @PostMapping("/saveLickOrder")
    public boolean saveLickOrder(@RequestBody Order order) {
        try {
            String lickerId = request.getRemoteUser();
            order.setUserId(lickerId);
            order.setSourceType("2");
            orderService.saveLickOrder(order);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("/genLickPayCode")
    public Map<String, Object> genLickPayCode(String lickedId) {
        PayLog payLog = payLogService.findLickPayLogFromRedis(lickedId);
        Map<String, Object> map = weChatPayService.genPayCode(payLog.getOutTradeNo(), String.valueOf(payLog.getTotalFee()));
        return map;
    }

    @GetMapping("/queryLickPayStatus")
    public Map<String, Object> queryLickPayStatus(String outTradeNo, String lickedId) {
        Map<String, Object> data = new HashMap<>();
        data.put("payStatus", "3");
        Map<String, String> resultMap = weChatPayService.queryPayStatus(outTradeNo);

        if (resultMap != null && resultMap.size() > 0) {
            if ("SUCCESS".equals(resultMap.get("trade_state"))) {
                data.put("payStatus", "1");
                orderService.updateLickOrderStatus(outTradeNo, resultMap.get("transaction_id"), lickedId);
            }
            if ("NOTPAY".equals(resultMap.get("trade_state"))) data.put("payStatus", "2");
        }

        return data;
    }

}
