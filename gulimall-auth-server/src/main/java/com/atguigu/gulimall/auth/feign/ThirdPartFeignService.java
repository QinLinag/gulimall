package com.atguigu.gulimall.auth.feign;


import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gulimall-third-party")
public interface ThirdPartFeignService {

    //调用gulimall-third-party这个服务中的发短信的接口，

    /**
     * SpringMVC viewcontroller:将请求映射过来
     * */
    @GetMapping("/sms/sendcode")
    R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code);

}
