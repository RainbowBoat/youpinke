package com.pinyougou.service;

import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.pojo.Specification;
import java.util.List;
import java.io.Serializable;
import java.util.Map;

/**
 * SpecificationService 服务接口
 * @date 2019-02-27 16:23:55
 * @version 1.0
 */
public interface SpecificationService {

	PageResult findAsPage(Integer pageNum, Integer rows, Specification specification);

	/** 添加方法 */
	void save(Specification specification);

	/** 修改方法 */
	void update(Specification specification);

	/** 根据主键id删除 */
	void delete(Serializable id);

	/** 批量删除 */
	void deleteAll(Serializable[] ids);

	/** 根据主键id查询 */
	Specification findOne(Serializable id);

	/** 查询全部 */
	List<Specification> findAll();

	/** 多条件分页查询 */
	List<Specification> findByPage(Specification specification, int page, int rows);

    List<Map<String,Object>> findByIdAndName();
}