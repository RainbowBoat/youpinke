package com.pinyougou.mapper;

import com.pinyougou.pojo.Brand;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface BrandMapper extends Mapper<Brand> {

    List<Brand> findAll(Brand brand);

    @Select("select * from tb_brand where name = #{name}")
    List<Brand> findByName(String name);

    @Insert("insert into tb_brand (name, first_char) values (#{name}, #{firstChar})")
    void add(Brand brand);

    @Update("update tb_brand set name = #{name}, first_char = #{firstChar} where id = #{id}")
    void update(Brand brand);

    void deleteSelections(long[] ids);

    @Select("SELECT id, name AS text FROM tb_brand ORDER BY id ASC;")
    List<Map<String, Object>> findAllByIdAndName();
}
