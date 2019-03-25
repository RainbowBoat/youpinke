package com.pinyougou.mapper;

import com.pinyougou.pojo.ItemCat;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import com.pinyougou.pojo.Item;

import java.io.Serializable;
import java.util.List;

/**
 * ItemMapper 数据访问接口
 * @date 2019-02-27 16:23:05
 * @version 1.0
 */
public interface ItemMapper extends Mapper<Item>{

    @Select("select * from tb_item_cat where parent_id = #{parentId}")
    List<ItemCat> findByParentId(Integer parentId);

    List<Item> findByGoodsId(Long goodsId);


}