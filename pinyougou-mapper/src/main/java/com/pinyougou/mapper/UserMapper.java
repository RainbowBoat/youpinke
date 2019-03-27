package com.pinyougou.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

import com.pinyougou.pojo.User;

/**
 * UserMapper 数据访问接口
 * @date 2019-02-27 16:23:05
 * @version 1.0
 */
public interface UserMapper extends Mapper<User>{


    @Select("SELECT * FROM tb_user WHERE username=#{user}")
    User findNumber(String user);

    @Update("update tb_user set password = #{password} where username = #{username}")
    void update(@Param("password")String password, @Param("username")String username);
    @Select("SELECT id FROM tb_user WHERE username=#{username}")
    Long selectById(String username);

}