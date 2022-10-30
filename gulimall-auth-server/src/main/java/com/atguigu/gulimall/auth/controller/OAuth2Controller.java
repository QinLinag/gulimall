package com.atguigu.gulimall.auth.controller;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.MemberFeignService;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.auth.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 处理社交登录请求
 *
 *
 * */

@Controller
public class OAuth2Controller {
    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session) throws Exception {

        Map<String,String> map=new HashMap<>();
        map.put("client_id","2874974475");
        map.put("client_secret","adsflasdhfoasdjfasjdfjasdfojasdf");
        map.put("grant_type","asdlfjlasjdflajsdlfjasl;df");
        map.put("redirect_uri","http://gulimall.com/oauth2.0/weibo/success");
        map.put("code",code);
        //1.根据code换取accessToten;
        HttpResponse response = HttpUtils.doPost("api.weibo.com", "/oauth2/access_token", "post", null, null, map);

        //2.处理
        if (response.getStatusLine().getStatusCode()==200){
            //获取到accessToken
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);

            //知道当前是哪个社交用户
            //1)当前用户如果是第一次进网站，自动注册进来(为当前社交用户生成一个用户信息账号，以后这个社交账号就对应指定的会员)
            R oauthlogin = memberFeignService.oauthlogin(socialUser);
            if (oauthlogin.getCode()==0){
                MemberRespVo data = oauthlogin.getData("data", new TypeReference<MemberRespVo>() {
                });
                session.setAttribute("loginUser",data);
                //2.登录成功就跳回页面
                return "redirect:http://gulimall.com";
            }else{
                return "redirect:http://gulimall.com/login.html";
            }
        }else{
            return "redirect:http://gulimall.com/login.html";
        }
    }
}
