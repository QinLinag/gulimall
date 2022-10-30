package com.atguigu.gulimall.auth.controller;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.auth.feign.MemberFeignService;
import com.atguigu.gulimall.auth.feign.ThirdPartFeignService;
import com.atguigu.gulimall.auth.vo.UserLoginVo;
import com.atguigu.gulimall.auth.vo.UserRegistVo;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    @Autowired
    ThirdPartFeignService thirdPartFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;


    @Autowired
    MemberFeignService memberFeignService;

    /**
     * 发送一个请求直接跳转到一个页面
     * SpringMVC viewcontroller  将请求和页面映射过来，
     * */

    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone){

        //1.接口防刷
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(redisCode)) {
            long time = Long.parseLong(redisCode.split("_")[1]);
            if(System.currentTimeMillis()-time<60000){
                //60秒内不能发
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(),BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }


        //2.验证码效验  redis缓存
        String code = UUID.randomUUID().toString().substring(0, 5);
        String substring = code+"_"+System.currentTimeMillis();
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone,substring,10, TimeUnit.MINUTES);


        thirdPartFeignService.sendCode(phone,code);
        return R.ok();
    }


    /**
     * TODO 重定向携带数据   利用session原理，将数据放在session中 只要跳到下一个页面取出这个数据后，session里面的数据就会被删掉
     *
     * TODO 分布式下的session问题
         *
     * RedirectAttributes redirectAttributes
     *
     * */

    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo vo, BindingResult result, RedirectAttributes redirectAttributes){
        if (result.hasErrors()){   //如果校验出问题
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(fieldError -> {
                return fieldError.getField();
            }, fieldError -> {
                return fieldError.getDefaultMessage();
            }));
            //model.addAttribute("errors",errors);
            redirectAttributes.addFlashAttribute("errors",errors);

            //校验到注册页
            return "redirect:http://auth.gulimall.com/reg.html";
        }

        //1.验证码效验
        String code = vo.getCode();
        String s = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());

        if (!StringUtils.isEmpty(s)){
            if (code.equals(s.split("_")[0])){
                //删除验证；
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
                //验证码通过    //真正注册，调用远程服务进行注册
                R r = memberFeignService.regist(vo);
                if (r.getCode()==0){
                    //成功
                    return "redirect:http://auth.gulimall.com/login.html";
                }else{
                    //失败
                    Map<String,String> errors=new HashMap<>();
                    errors.put("msg",r.getData("msg",new TypeReference<String>(){}));
                    return "redirect:http://auth.gulimall.com/reg.html";
                }
            }else{
                Map<String,String> errors= new HashMap<>();
                errors.put("code","验证码错误");
                redirectAttributes.addFlashAttribute("errors",errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        }else{
           Map<String,String> errors= new HashMap<>();
            errors.put("code","验证码失效");
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
    }



    @GetMapping("/login.html")
    public String loginPage(HttpSession session){
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (null==attribute) {
            return "login";
        }else{
            return "redirect:http://gulimall.com";
        }
    }



    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes,
                        HttpSession session) {

        //远程登录
        R login = memberFeignService.login(vo);
        if (login.getCode()==0){
            //成功
            MemberRespVo data = login.getData("data", new TypeReference<MemberRespVo>() {
            });
            //放到session中，
            session.setAttribute(AuthServerConstant.LOGIN_USER,data);
            return "redirect:http://gulimall.com";
        }else{
            Map<String,String> errors=new HashMap<>();
            errors.put("msg",login.getData("data",new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }

    }



}
