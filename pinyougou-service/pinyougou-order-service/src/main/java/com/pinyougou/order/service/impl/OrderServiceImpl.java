package com.pinyougou.order.service.impl;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.Cart;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.OrderItemMapper;
import com.pinyougou.mapper.OrderMapper;
import com.pinyougou.mapper.PayLogMapper;
import com.pinyougou.pojo.Order;
import com.pinyougou.pojo.OrderItem;
import com.pinyougou.pojo.PayLog;
import com.pinyougou.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Service(interfaceName = "com.pinyougou.service.OrderService")
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private PayLogMapper payLogMapper;

    @Override
    public void save(Order order) {
        try {
            String username = order.getUserId();
            List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("tempCartList").get(order.getUserId());

            // 保存订单编号集合
            StringBuilder orderIdsBuilder = new StringBuilder();
            double totalMoney = 0;

            for (Cart cart : cartList) {
                Order newOrder = new Order();

                Long id = idWorker.nextId();
                newOrder.setOrderId(id);
                newOrder.setPaymentType(order.getPaymentType());
                newOrder.setStatus("1");
                newOrder.setCreateTime(new Date());
                newOrder.setUpdateTime(newOrder.getCreateTime());
                newOrder.setUserId(order.getUserId());
                newOrder.setReceiverAreaName(order.getReceiverAreaName());
                newOrder.setReceiverMobile(order.getReceiverMobile());
                newOrder.setReceiver(order.getReceiver());
                newOrder.setSourceType("2");
                newOrder.setSellerId(cart.getSellerId());

                double payment = 0;

                for (OrderItem orderItem : cart.getOrderItems()) {
                    Long orderItemId = idWorker.nextId();
                    orderItem.setId(orderItemId);
                    orderItem.setOrderId(newOrder.getOrderId());
                    payment += orderItem.getPrice().doubleValue();
                    orderItemMapper.insert(orderItem);
                }

                newOrder.setPayment(new BigDecimal(payment));
                orderMapper.insert(newOrder);

                orderIdsBuilder.append(String.valueOf(id));
                orderIdsBuilder.append("-");
                totalMoney += payment;
            }

            String orderIds = orderIdsBuilder.toString();

            if ("1".equals(order.getPaymentType())) {
                PayLog payLog = new PayLog();
                payLog.setOutTradeNo(String.valueOf(idWorker.nextId()));
                payLog.setCreateTime(new Date());
                payLog.setTotalFee((long)(totalMoney*100));
                payLog.setUserId(order.getUserId());
                payLog.setTradeState("0");
                payLog.setOrderList(orderIds.substring(0, orderIds.length()-1));
                payLog.setPayType(order.getPaymentType());

                payLogMapper.insertSelective(payLog);
                // 将支付单保存到Redis
                redisTemplate.boundValueOps("payLog_" + order.getUserId()).set(payLog);
            }

            List<Cart> sourceCartList = (List<Cart>)redisTemplate.boundValueOps("cart_" + username).get();

            deleteTempCartList(cartList, sourceCartList, username);

//            redisTemplate.delete("cart_" + username);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Order order) {

    }

    @Override
    public void delete(Serializable id) {

    }

    @Override
    public void deleteAll(Serializable[] ids) {

    }

    @Override
    public Order findOne(Serializable id) {
        return null;
    }

    @Override
    public List<Order> findAll() {
        return null;
    }

    @Override
    public List<Order> findByPage(Order order, int page, int rows) {
        return null;
    }

    @Override
    public void updateOrderStatus(String outTradeNo, String transactionId) {
        PayLog payLog = payLogMapper.selectByPrimaryKey(outTradeNo);
        payLog.setPayTime(new Date());
        payLog.setTransactionId(transactionId);
        payLog.setTradeState("1");
        payLogMapper.updateByPrimaryKeySelective(payLog);

        String[] orderIds = payLog.getOrderList().split("-");
        for (String orderId : orderIds) {
            Order order = new Order();
            order.setOrderId(Long.parseLong(orderId));
            order.setStatus("2");
            order.setUpdateTime(new Date());
            order.setPaymentTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
        }

        redisTemplate.delete("payLog_" + payLog.getUserId());
    }

    // 从源购物车集合中删除临时购物车中集合中的商品
    private void deleteTempCartList(List<Cart> tempCartList, List<Cart> sourceCartList, String userId) {
        // 遍历临时集合, 遍历源集合, 判断两集合中遍历到的商品的itemId是否相同, 相同则删除
        List<Map.Entry<Integer, Integer>> indexList = (List<Map.Entry<Integer, Integer>>) redisTemplate.boundHashOps("tempCartIndexList").get(userId);

        Collections.reverse(indexList);

        for (Map.Entry<Integer, Integer> entry : indexList) {
            int cartIndex = entry.getKey().intValue();
            int itemIndex = entry.getValue().intValue();
            Cart cart = sourceCartList.get(cartIndex);
            List<OrderItem> orderItems = cart.getOrderItems();
            orderItems.remove(itemIndex);
        }
        redisTemplate.boundValueOps("cart_" + userId).set(sourceCartList);

        redisTemplate.boundHashOps("tempCartList").delete(userId);
    }
}
