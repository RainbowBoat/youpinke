package com.pinyougou.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.service.ShowOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/historyOrder")
public class HistoryOrdersController {

    @Reference(timeout = 100000)
    private ShowOrderService showOrderService;

    @Autowired
    private HttpServletRequest request;

    @PostMapping("/findOrders")
    public Map<String, Object> findOrders(@RequestBody Map<String, String> pageParam) {
        String userId = request.getRemoteUser();

        Map<String, Object> data = showOrderService.findOrders(userId,  pageParam);

        return data;
    }

}
