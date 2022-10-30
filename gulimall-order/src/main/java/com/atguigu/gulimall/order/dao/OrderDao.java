package com.atguigu.gulimall.order.dao;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author qinliang
 * @email 2874974475@qq.com
 * @date 2022-10-04 23:40:10
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
