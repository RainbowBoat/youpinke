package com.pinyougou.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import com.pinyougou.pojo.Order;

import java.util.List;
import java.util.Map;

/**
 * OrderMapper 数据访问接口
 * @date 2019-02-27 16:23:05
 * @version 1.0
 */
public interface OrderMapper extends Mapper<Order>{

    List<Order> findOrdersByUserId(@Param("userId") String userId, @Param("status") String[] status);

    @Select("select * from tb_order where receiver = #{receiver} and status = #{status}")
    List<Order> findLickOrderByLickedId(@Param("receiver") String receiver, @Param("status") String status);

}