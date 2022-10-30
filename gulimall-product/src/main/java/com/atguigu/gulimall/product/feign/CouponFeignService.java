package com.atguigu.gulimall.product.feign;


import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient("gulimall-coupon")
public interface CouponFeignService {


    //1.CouponFeignService.saveSpuBounds(spuBoundTo);
        //1)@RequestBody将这个对象spuBoundTo转为json
        //2）找到gulimall-coupon服务，给coupoon/spubounds/save发送请求
           //将上一步转的json放在请求体位置，发送请求
        //3)对方服务收到请求，请求体里有json数据，如果对方服务里接收参数是(@RequestBody SpuBoundsEntity spuBounds);将请求体的json转为SpuBoundsEntity
            //不一定非要我们传的是spuBoundTo对象，对方接收参数也要是spuBoundTo，只要对象中字段名一样就好了，

    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    @PostMapping("/coupon/skufullreduction/saveinfo")
    R saveSkuReduciton(@RequestBody SkuReductionTo skuReductionTo);
}
