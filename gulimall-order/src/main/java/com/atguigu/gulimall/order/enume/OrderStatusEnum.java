package com.atguigu.gulimall.order.enume;

public enum OrderStatusEnum {
    CREATE_NEW(0,"代付款"),
    PAYED(1,"衣服狂"),
    SENDEN(2,"已发货"),
    RECIEVED(3,"已完成"),
    CANCLED(4,"已取消"),
    SERVICING(5,"售后中"),
    SERVICED(6,"售后完成");
    private Integer code;
    private String msg;

    public Integer getCode(){
        return code;
    }

    public String getMsg(){
        return msg;
    }


    OrderStatusEnum(Integer code,String msg){

    }

}
