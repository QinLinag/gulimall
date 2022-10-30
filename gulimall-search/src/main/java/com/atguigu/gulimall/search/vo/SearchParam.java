package com.atguigu.gulimall.search.vo;


import lombok.Data;

import java.util.List;

@Data
public class SearchParam {
    private String keyword;  //页面传递过来的全文匹配关键字
    private Long catalog3Id;  //三级分类id

    private String sort;  //排序条件

    private Integer hasStrock=1;  //是否显示有货
    private String skuPrice; //价格区间查询
    private List<Long> brandId;  //进行品牌查询，可以多个
    private List<String> attrs;  //按照属性
    private Integer pageNum;  //页码，


    private String _queryString;//原生的所有查询条件
















}
