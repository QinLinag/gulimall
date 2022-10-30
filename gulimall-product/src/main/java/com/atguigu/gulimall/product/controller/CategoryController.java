package com.atguigu.gulimall.product.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;

import javax.validation.Valid;


/**
 * 商品三级分类
 *
 * @author qinliang
 * @email 2874974475@qq.com
 * @date 2022-10-04 22:39:43
 */
@RestController
@RequestMapping("product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 查出所有分类以及子类，以树形结构组装起来
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:category:list")
    public R list(){

        List<CategoryEntity> entities=categoryService.listWithTree();

        return R.ok().put("entities", entities);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
    //@RequiresPermissions("product:category:info")
    public R info(@PathVariable("catId") Long catId){
		CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("category", category);
    }

    /**
     * 保存  新增功能
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:category:save")
    public R save(@Valid @RequestBody CategoryEntity category, BindingResult result){  //@Valid开启校验功能

        if (result.hasErrors()){

            Map<String,String> map=new HashMap<String,String>();

            //1.获取效验的错误结果
            result.getFieldErrors().forEach((item)->{
                //Field 获取到错误提示
                String message = item.getDefaultMessage();
                //获取错误的字段的名字
                String field = item.getField();
                //将所有的错误以kv形式放到map中
                map.put(field,message);
            });

            return R.error(400,"save：result is not valid").put("data",map);
        }

        categoryService.save(category);

        return R.ok();
    }



    @RequestMapping("/update/sort")
    //@RequiresPermissions("product:category:update")
    public R update(@RequestBody CategoryEntity[] category){
        categoryService.updateBatchById(Arrays.asList(category));

        return R.ok();
    }


    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:category:update")
    public R update(@RequestBody CategoryEntity category){
		categoryService.updateCascade(category);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:category:delete")
    public R delete(@RequestBody Long[] catIds){
//		categoryService.removeByIds(Arrays.asList(catIds));

        //1.检查当前删除菜单是否被别的地方引用
        categoryService.removeMenuByIds(Arrays.asList(catIds));
        return R.ok();
    }

}
