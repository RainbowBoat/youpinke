package com.pinyougou.mapper;

import tk.mybatis.mapper.common.Mapper;

import com.pinyougou.pojo.Content;

import java.io.Serializable;

/**
 * ContentMapper 数据访问接口
 * @date 2019-02-27 16:23:05
 * @version 1.0
 */
public interface ContentMapper extends Mapper<Content>{


    void deleteAll(Serializable[] ids);
}