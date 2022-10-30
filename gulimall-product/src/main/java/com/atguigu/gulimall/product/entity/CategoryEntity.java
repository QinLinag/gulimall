package com.atguigu.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotBlank;


/**
 * 商品三级分类
 * 
 * @author qinliang
 * @email 2874974475@qq.com
 * @date 2022-10-04 22:39:43
 */
@Data
@TableName("pms_category")
public class CategoryEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 分类id
	 */
	@TableId
	private Long catId;
	/**
	 * 分类名称
	 */
	@NotBlank(message = "category name can not be null")    //如果开启校验后，新增的这个实例就会校验这个字段是否为空也不能是空白串
	private String name;
	/**
	 * 父分类id
	 */
	private Long parentCid;
	/**
	 * 层级
	 */
	private Integer catLevel;
	/**
	 * 是否显示[0-不显示，1显示]
	 */

	@TableLogic(value = "1",delval = "0")    //逻辑删除字段   1表示不删除，0表示删除
	private Integer showStatus;
	/**
	 * 排序
	 */
	private Integer sort;
	/**
	 * 图标地址
	 */
	private String icon;
	/**
	 * 计量单位
	 */
	private String productUnit;
	/**
	 * 商品数量
	 */
	private Integer productCount;


	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@TableField(exist = false)  //这个表示在数据库中不存在
	private List<CategoryEntity> children;

}
