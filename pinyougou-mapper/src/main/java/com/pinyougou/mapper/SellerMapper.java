package com.pinyougou.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

import com.pinyougou.pojo.Seller;

import java.util.List;

/**
 * SellerMapper 数据访问接口
 * @date 2019-02-27 16:23:05
 * @version 1.0
 */
public interface SellerMapper extends Mapper<Seller>{

    List<Seller> findAsPage(Seller seller);

    @Update("UPDATE tb_seller set status = #{status} where seller_id = #{sellerId}")
    void updateStatus(@Param("sellerId") String sellerId, @Param("status") Integer status);

    @Update("UPDATE tb_seller set password = #{password} where seller_id = #{sellerId}")
    void updatePassword(@Param("password")String password, @Param("sellerId")String sellerId);
}