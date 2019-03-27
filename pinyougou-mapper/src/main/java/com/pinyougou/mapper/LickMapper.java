package com.pinyougou.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

public interface LickMapper {

    @Insert("INSERT INTO tb_lick VALUES (#{lickedId}, #{lickerId})")
    void save(@Param("lickerId") String lickerId, @Param("lickedId") String lickedId);

    @Delete("DELETE FROM tb_lick WHERE licked_id = #{lickedId}")
    void brokeUp(String lickedId);

}
