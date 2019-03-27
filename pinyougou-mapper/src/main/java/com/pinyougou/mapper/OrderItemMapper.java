package com.pinyougou.mapper;

import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import com.pinyougou.pojo.OrderItem;

import java.util.List;

/**
 * OrderItemMapper 数据访问接口
 * @date 2019-02-27 16:23:05
 * @version 1.0
 */
public interface OrderItemMapper extends Mapper<OrderItem>{

    @Select("select * from tb_order_item where order_id = #{orderId}")
    List<OrderItem> findOrderItemsByOrderId(long orderId);

}