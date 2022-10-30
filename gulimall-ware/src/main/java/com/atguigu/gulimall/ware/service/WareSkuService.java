package com.atguigu.gulimall.ware.service;

import com.atguigu.common.to.StockLockedTo;
import com.atguigu.gulimall.ware.vo.FareVo;
import com.atguigu.gulimall.ware.vo.LockStockResult;
import com.atguigu.gulimall.ware.vo.SkuHasStockVo;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author qinliang
 * @email 2874974475@qq.com
 * @date 2022-10-04 23:45:46
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds);

    //根据收货地址计算运费
    FareVo getFare(Long addrId);

    Boolean orderLockStock(WareSkuLockVo vo);

    void unlockStock(StockLockedTo to);
}

