package com.pinyougou.mapper;

import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import com.pinyougou.pojo.Goods;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * GoodsMapper 数据访问接口
 * @date 2019-02-27 16:23:05
 * @version 1.0
 */
public interface GoodsMapper extends Mapper<Goods>{

    List<Goods> findAsPage(Goods goods);

    List<Map<String, Object>> findAll(Goods goods);

    void deleteAll(Serializable[] ids);

    void updateAll(@Param("ids") Serializable[] ids, @Param("auditStatus") Integer auditStatus);

    void updateMarketable(@Param("ids") Serializable[] ids, @Param("marketable") String marketable);

}