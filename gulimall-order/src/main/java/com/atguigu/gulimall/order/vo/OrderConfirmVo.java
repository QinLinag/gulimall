package com.atguigu.gulimall.order.vo;


import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class OrderConfirmVo {
    @Getter@Setter
   private List<MemberAddressVo> address;
    @Getter@Setter
   private List<OrderItemVo> items;

   //优惠券信息
   @Getter@Setter
    Integer integration;

   //防重令牌
    @Getter@Setter
   private String orderToken;

    @Getter@Setter
    Map<Long,Boolean> stocks;


    private BigDecimal total;  //订单总额
    private BigDecimal payPrice; //应付价格，



    public Integer getCount(){
        Integer i=0;
        if (items!=null){
            for (OrderItemVo item : items) {
                i++;
            }
        }
        return i;
    }





    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        if (items!=null) {
            for (OrderItemVo item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum=sum.add(multiply);
            }
        }
        total=sum;
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getPayPrice() {
        BigDecimal sum = new BigDecimal("0");
        if (items!=null) {
            for (OrderItemVo item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum=sum.add(multiply);
            }
        }
        payPrice=sum;
        return payPrice;
    }

    public void setPayPrice(BigDecimal payPrice) {
        this.payPrice = payPrice;
    }
}
