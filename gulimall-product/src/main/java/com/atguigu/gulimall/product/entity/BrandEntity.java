package com.atguigu.gulimall.product.entity;

import com.atguigu.common.valid.AddGroup;
import com.atguigu.common.valid.UpdateGroup;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 * 
 * @author qinliang
 * @email 2874974475@qq.com
 * @date 2022-10-04 22:39:43
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@Null(message = "the new entity should be null",groups = {AddGroup.class})  //新增实例时，id必须为空，
	@NotNull(message = "changed entity should not be null",groups = {UpdateGroup.class})  //修改实例时id不能为空
	@TableId
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotBlank(message = "brand can not be null",groups = {AddGroup.class,UpdateGroup.class})    //如果开启校验后，新增的这个实例就会校验这个字段是否为空也不能是空白串
	private String name;
	/**
	 * 品牌logo地址
	 */
	@NotEmpty(groups = {AddGroup.class})
	@URL(message = "the url must be valid",groups = {AddGroup.class,UpdateGroup.class})
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */

	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@NotNull(groups = {AddGroup.class})
	@Pattern(regexp = "^[a-zA-Z]$",message = "first letter should be between a and z or between A and Z",groups = {AddGroup.class,UpdateGroup.class})  //自定义效验注解
	private String firstLetter;
	/**
	 * 排序
	 */
	private Integer sort;

}





















