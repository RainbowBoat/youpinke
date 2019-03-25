package com.pinyougou.mapper;

import com.pinyougou.pojo.Specification;
import org.apache.ibatis.annotations.Insert;
import tk.mybatis.mapper.common.Mapper;

import com.pinyougou.pojo.SpecificationOption;

import java.util.List;

/**
 * SpecificationOptionMapper 数据访问接口
 * @date 2019-02-27 16:23:05
 * @version 1.0
 */
public interface SpecificationOptionMapper extends Mapper<SpecificationOption>{

    void save(Specification specification);

    List<SpecificationOption> findBySpId(Long specId);
}