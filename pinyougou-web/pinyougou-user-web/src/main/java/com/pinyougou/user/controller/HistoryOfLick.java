package com.pinyougou.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.Address;
import com.pinyougou.service.AddressService;
import com.pinyougou.service.ShowOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/historyOfLick")
public class HistoryOfLick {

    @Reference(timeout = 100000)
    private AddressService addressService;

    @Reference(timeout = 100000)
    private ShowOrderService showOrderService;

    @Autowired
    private HttpServletRequest request;

    @PostMapping("/GetHistoryOfLick")
    public Map<String, Object> findOrders(@RequestBody Map<String, String> pageParam) {
        String lickedId = pageParam.get("lickedId");
        List<Address> addressList = addressService.findAllByUsername(lickedId);
        Address address = addressList.get(0);
        String receiver = address.getContact();
        String status = "4";
        Map<String, Object> data = showOrderService.findLickOrders(receiver, pageParam, status);

        return data;
    }
}
