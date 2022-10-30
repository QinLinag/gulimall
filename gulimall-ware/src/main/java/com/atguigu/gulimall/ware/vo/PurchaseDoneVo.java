package com.atguigu.gulimall.ware.vo;


import lombok.Data;

import java.util.List;

@Data
public class PurchaseDoneVo {

    private Long id;   //采购id

    private List<PurchaseItemDoneVo> items;

}
