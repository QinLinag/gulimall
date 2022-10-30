package com.atguigu.gulimall.order.vo;

import lombok.Data;

@Data
public class LockStockResult {

    private Long skuId;
    private Integer num;
    private Boolean locked;

}
