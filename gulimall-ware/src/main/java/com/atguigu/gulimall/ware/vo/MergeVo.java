package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

@Data
public class MergeVo {

    private Long purChaseId;   //整合的采购单
    private List<Long> items;   //[1,2,3,4] 合并项集合

}
