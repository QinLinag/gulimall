package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderSubmitVo {
    private Long addrId; //收货地址
    private Integer payType;  //支付方式

    //无需提交需要购买的商品，去购物车再获取一遍，保证实时性

    //优惠、发票

    private String orderToken; //防重令牌
    private BigDecimal payPrice;  //应付价格，验价

    private String note;   //订单的备注

    //用户相关信息，直接区session取出登录的用户，

}
