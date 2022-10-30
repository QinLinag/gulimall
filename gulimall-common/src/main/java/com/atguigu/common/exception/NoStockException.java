package com.atguigu.common.exception;

public class NoStockException extends RuntimeException{

    private Long skuId;
    public NoStockException(){
        super("商品："+"没有库存了");
    }
}
