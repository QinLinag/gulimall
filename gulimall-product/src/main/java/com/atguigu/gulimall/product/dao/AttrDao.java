package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品属性
 * 
 * @author qinliang
 * @email 2874974475@qq.com
 * @date 2022-10-04 22:39:43
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {

    List<Long> searchAttrIds(@Param("attrIds") List<Long> attrIds);
}
