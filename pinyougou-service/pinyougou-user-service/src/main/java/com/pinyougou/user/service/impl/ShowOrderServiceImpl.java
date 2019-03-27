package com.pinyougou.user.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.OrderItemMapper;
import com.pinyougou.mapper.OrderMapper;
import com.pinyougou.pojo.Order;
import com.pinyougou.pojo.OrderItem;
import com.pinyougou.service.AddressService;
import com.pinyougou.service.ShowOrderService;
import com.sun.tools.corba.se.idl.constExpr.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(interfaceName = "com.pinyougou.service.ShowOrderService")
@Transactional
public class ShowOrderServiceImpl implements ShowOrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    public Map<String, Object> findOrders(String userId, Map<String, String> pageParam, String[] status) {

        int pageNum = Integer.parseInt(pageParam.get("page"));
        int rows = Integer.parseInt(pageParam.get("rows"));
        PageInfo<Order> pageInfo = PageHelper.startPage(pageNum, rows).doSelectPageInfo(new ISelect() {
            @Override
            public void doSelect() {
                orderMapper.findOrdersByUserId(userId, status);
            }
        });

        return prepareData(pageInfo);
    }

    @Override
    public Map<String, Object> findLickOrders(String receiver, Map<String, String> pageParam, String status) {
        int pageNum = Integer.parseInt(pageParam.get("page"));
        int rows = Integer.parseInt(pageParam.get("rows"));
        PageInfo<Order> pageInfo = PageHelper.startPage(pageNum, rows).doSelectPageInfo(new ISelect() {
            @Override
            public void doSelect() {
                orderMapper.findLickOrderByLickedId(receiver, status);
            }
        });

        return prepareData(pageInfo);
    }


    private Map<String, Object> prepareData(PageInfo<Order> pageInfo) {
        // 封装了页面参数和数据集合
        Map<String, Object> finalMap = new HashMap<>();
        // 数据集合
        List<Map<String, Object>> dataMapList = new ArrayList<>();

        List<Order> orderList = pageInfo.getList();
        for (Order order : orderList) {
            Map<String, Object> dataMap = new HashMap<>();
            List<OrderItem> orderItemsByOrderId = orderItemMapper.findOrderItemsByOrderId(order.getOrderId());
            dataMap.put("order", order);
            dataMap.put("orderItems", orderItemsByOrderId);
            // 将一个订单放入数据集合
            dataMapList.add(dataMap);
        }

        finalMap.put("totalPages", pageInfo.getPages());
        finalMap.put("total", pageInfo.getTotal());
        finalMap.put("orderList", dataMapList);

        return finalMap;
    }
}
