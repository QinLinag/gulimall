package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.constant.ProductConstant;
import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.gulimall.product.feign.CouponFeignService;
import com.atguigu.gulimall.product.feign.SearchFeignService;
import com.atguigu.gulimall.product.feign.WareFeignService;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.*;
import com.qiniu.util.StringUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson.TypeReference;

@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService imagesService;

    @Autowired
    AttrService attrService;

    @Autowired
    ProductAttrValueService attrValueService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    SearchFeignService searchFeignService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    //TODO 高级部分完善

    @Transactional
    @Override
    public void savaSpuInfo(SpuSaveVo vo) {
        //1.保存spu基本信息，pms_spu_info
        SpuInfoEntity infoEntity = new SpuInfoEntity();
        try {
            BeanUtils.copyProperties(infoEntity,vo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        infoEntity.setCreateTime(new Date());
        infoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(infoEntity);

        //2.保存spu的描述图片pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(infoEntity.getId());
        descEntity.setDecript(String.join(",",decript));   //将数组中的String拼接在一起返回一个String
        spuInfoDescService.saveSpuInfoDesc(descEntity);

        //3.保存spu的图片集 pms_spu_images
        List<String> images = vo.getImages();
        imagesService.saveImages(infoEntity.getId(),images);

        //4.保存spu的规格参数，pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map((attr) -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setAttrId(attr.getAttrId());
            AttrEntity byId = attrService.getById(attr.getAttrId());
            valueEntity.setAttrName(byId.getAttrName());
            valueEntity.setAttrValue(attr.getAttrValues());
            valueEntity.setQuickShow(attr.getShowDesc());
            valueEntity.setSpuId(infoEntity.getId());

            return valueEntity;
        }).collect(Collectors.toList());
        attrValueService.saveProductAttr(collect);

        //6.保存spu的积分信息，gulimall_sms->sms_spu_bounds
            //远程调用
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        try {
            BeanUtils.copyProperties(bounds,spuBoundTo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        spuBoundTo.setSpuId(infoEntity.getId());
        R r1 = couponFeignService.saveSpuBounds(spuBoundTo);
        if(r1.getCode()!=0){
            log.error("远程保存优惠信息失败");
        }


        //5.保存当前spu对应的所有sku信息；
        //5.1)sku的基本信息，pms_sku_info
        List<Skus> skus = vo.getSkus();
        if(skus!=null&&skus.size()>0){
            skus.forEach((item)->{
                String defaultImg="";
                for (Images image : item.getImages()) {
                    if(1==image.getDefaultImg()){
                        defaultImg=image.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                try {
                    BeanUtils.copyProperties(item,skuInfoEntity);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                skuInfoEntity.setBrandId(infoEntity.getBrandId());
                skuInfoEntity.setCatalogId(infoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(infoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                skuInfoService.saveSkuInfo(skuInfoEntity);



                //5.2)sku的图片信息，pms_sku_images
                Long skuId = skuInfoEntity.getSkuId();  //sku保存后自增后的id，用于下面数据封装

                //TODO 没有图片，路径无需保存
                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter((entity)->{      //过滤空图片，
                    return !StringUtils.isNullOrEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());
                skuImagesService.saveBatch(imagesEntities);

                //5.3）sku的销售属性信息，pms_sku_sale_attr_value
                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntityList = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    try {
                        BeanUtils.copyProperties(a, skuSaleAttrValueEntity);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntityList);


                //5.4）sku的优惠、满减等信息，gulimall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price
                        //远程调用
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                try {
                    BeanUtils.copyProperties(item,skuReductionTo);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                skuReductionTo.setSkuId(skuId);
                if(skuReductionTo.getSkuId()>0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0"))==1){
                    R r = couponFeignService.saveSkuReduciton(skuReductionTo);
                    if(r.getCode()!=0){
                        log.error("远程保存优惠信息失败");
                    }
                }
            });
        }



    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity infoEntity) {
        this.baseMapper.insert(infoEntity);
    }



    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        String key = (String)params.get("key");
        if(!StringUtils.isNullOrEmpty(key)){
            wrapper.and((w)->{
                w.eq("id",key).or().like("spu_name",key);   //这里是or条件，不能和后面的一起拼接
                                                                    //因为：status=1 and id=key or spu_name like key ,如果是这样的话，那么or成立就不用管status了，
                                            //而我们想要的是，status=1 and (id=key or spu_name like key)
            });
        }
        String status = (String)params.get("status");
        if(!StringUtils.isNullOrEmpty(status)){
                wrapper.eq("publish_status",status);
        }
        String brandId = (String)params.get("brandId");
        if(!StringUtils.isNullOrEmpty(brandId)&&!"0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id",brandId);
        }
        String catelogId = (String)params.get("catelogId");
        if(!StringUtils.isNullOrEmpty(catelogId)&&!"0".equalsIgnoreCase(catelogId)){
            wrapper.eq("catalog_id",catelogId);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }





    //商品上架，
    @Override
    public void up(Long spuId) {

        //1.查出当前spuid对应的所有sku信息，品牌的名字
        List<SkuInfoEntity> skus=skuInfoService.getSkusBySpuId(spuId);

        //id集合
        List<Long> skuIdList = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());


        //查出当前sku的所有可以被用来检索的规格属性
        List<ProductAttrValueEntity> baseAttrs = attrValueService.baseAttrlistforspu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(attr -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());

        List<Long> searchAttrIds=attrService.selectSearchAttrIds(attrIds);

        Set<Long> idSet = new HashSet<>(searchAttrIds);

        List<SkuEsModel.Attrs> attrsList = baseAttrs.stream().filter(item -> {
            return idSet.contains(item.getAttrId());
        }).map(item -> {
                    SkuEsModel.Attrs attrs1 = new SkuEsModel.Attrs();
                    try {
                        BeanUtils.copyProperties(item, attrs1);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    return attrs1;
                }
        ).collect(Collectors.toList());


        //TODO 1.发送远程调用，库存系统查询是否有库存
        R skusHasStock =null;
        Map<Long, Boolean> hasStockMap=null;
        try{
            wareFeignService.getSkusHasStock(skuIdList);

            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<>() {
            };

            skusHasStock.getData("data",typeReference).stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, item -> item.getHasStock()));
        }catch (Exception e){
            log.error("库存服务查询异常：",e);
        }


        Map<Long, Boolean> finalSkusHasStock=hasStockMap;


        //2.封装每个sku的信息
        List<SkuEsModel> upProducts = skus.stream().map(sku -> {
            //组装需要的数据
            SkuEsModel esModel = new SkuEsModel();
            try {
                BeanUtils.copyProperties(sku,esModel);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            //skuPrice   skuImg    字段名不一致，单独设置
            esModel.setSkuPrice(sku.getPrice());
            esModel.setSkuImg(sku.getSkuDefaultImg());



            //hasStrock hotScore
            //TODO 2.热度评分 0
            esModel.setHotScore(0L);

            if (finalSkusHasStock==null){
                esModel.setHasStock(true);
            }else{
                esModel.setHasStock(finalSkusHasStock.get(sku.getSkuId()));
            }


           //查询品牌和分类的名字信息
            /**
             * private String brandName;
             *
              private String brandImg;
             *
             * private String catalogName;
             */
            BrandEntity brand = brandService.getById(esModel.getBrandId());
            esModel.setBrandName(brand.getName());
            esModel.setBrandImg(brand.getLogo());

            CategoryEntity category = categoryService.getById(esModel.getCatalogId());
            esModel.setCatalogName(category.getName());


            /**
             * private List<Attrs> attrs;
             *
             *     @Data
             *     public static class Attrs{
             *         private Long attrId;
             *         private String attrName;
             *         private String attrValue;
             *     }
             *
             * */
            //设置检索属性
            esModel.setAttrs(attrsList);

            return esModel;
        }).collect(Collectors.toList());


        //TODO 将数据发送给es，gulimall-search
        R r = searchFeignService.productStatusUp(upProducts);

        if (r.getCode()==0){
            //远程调用成功
            // TODO 修改当前spu的状态
            this.baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        }else {
            //远程调用失败
            //TODO 重复调用? 接口幂等性 重试机制？
        }








    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        SkuInfoEntity byId = skuInfoService.getById(skuId);
        Long spuId = byId.getSpuId();
        SpuInfoEntity spuInfoEntity = this.getById(spuId);
        return spuInfoEntity;
    }


}