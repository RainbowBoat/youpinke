package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.SeckillGoods;
import com.pinyougou.pojo.SeckillOrder;
import com.pinyougou.service.SeckillOrderService;
import com.pinyougou.service.WeChatPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class SeckillOrderController {

    @Reference(timeout = 1000000)
    private WeChatPayService weChatPayService;

    @Reference(timeout = 1000000)
    private SeckillOrderService seckillOrderService;

    @Autowired
    private HttpServletRequest request;

    @GetMapping("/submitOrder")
    public boolean submitOrder(Long id) {
        try {
            String userId = request.getRemoteUser();

            seckillOrderService.submitOrder(id, userId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    @GetMapping("/genPayCode")
    public Map<String, Object> genPayCode() {
        String userId = request.getRemoteUser();
        // 得到当前用户的秒杀订单列表
        SeckillOrder seckillOrder = seckillOrderService.findOneFromRedis(userId);
        long totalFee = (long) (seckillOrder.getMoney().doubleValue() * 100);
        Map<String, Object> resultMap = weChatPayService.genPayCode(seckillOrder.getId().toString(), String.valueOf(totalFee));
        return resultMap;
    }

    @GetMapping("/queryPayStatus")
    public Map<String, Object> queryPayStatus(String outTradeNo) {
        String userId = request.getRemoteUser();
        Map<String, Object> data = new HashMap<>();
        data.put("payStatus", "3");
        Map<String, String> resultMap = weChatPayService.queryPayStatus(outTradeNo);

        if (resultMap != null && resultMap.size() > 0) {
            if ("SUCCESS".equals(resultMap.get("trade_state"))) {
                seckillOrderService.updateSeckillOrderStatus(userId, resultMap.get("transaction_id"));
                data.put("payStatus", "1");
            }
            if ("NOTPAY".equals(resultMap.get("trade_state"))) {
                data.put("payStatus", "2");
            }
        }
        return data;
    }
}
