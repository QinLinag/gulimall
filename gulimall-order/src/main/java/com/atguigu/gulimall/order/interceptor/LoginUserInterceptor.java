package com.atguigu.gulimall.order.interceptor;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.vo.MemberRespVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


//拦截器，没登录就先登录，登陆了保存一下已经登录了的用户信息，

@Component  //拦截器加入到组件中，
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberRespVo> threadLocal=new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {


        //这里回出问题，其他服务远程调用这里的时候应该不需要登入，所以我们要放行其他服务通过feign远程调用这里的请求
        String uri = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/order/order/status/**", uri);
        if (match){
            return true;
        }


        MemberRespVo attribute = (MemberRespVo)request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute!=null){
            threadLocal.set(attribute);
            return true;
        }else{
            //没登录就去登录
            request.getSession().setAttribute("msg","请先登录");
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }
    }
}
