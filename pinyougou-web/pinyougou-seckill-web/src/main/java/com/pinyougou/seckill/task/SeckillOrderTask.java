package com.pinyougou.seckill.task;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.SeckillOrder;
import com.pinyougou.service.SeckillOrderService;
import com.pinyougou.service.WeChatPayService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SeckillOrderTask {

    @Reference(timeout = 100000)
    private SeckillOrderService seckillOrderService;
    @Reference(timeout = 100000)
    private WeChatPayService weChatPayService;

    @Scheduled(cron = "0/3 * * * * ?")
    public void closeOrderTask() {
        List<SeckillOrder> seckillOrderList = seckillOrderService.findOrderByTimeOut();

        System.out.println("==毫秒==" + System.currentTimeMillis());

        for (SeckillOrder seckillOrder : seckillOrderList) {

            Map<String, String> map = weChatPayService.closePayTimeOut(seckillOrder.getId().toString());

            if ("SUCCESS".equals(map.get("result_code"))) {
                System.out.println("===超时，删除订单===");

                seckillOrderService.deleteFromRedis(seckillOrder);
            }
        }
    }
}
