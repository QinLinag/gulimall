package com.atguigu.gulimall.thirdparty.component;


import com.atguigu.common.utils.HttpUtils;
import lombok.Data;
import org.apache.http.HttpResponse;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.HashMap;

@ConfigurationProperties(prefix = "spring.alicloud.sms")
@Component
@Data
public class SmsComponent {
    private String host;
    private String path;
    private String skin;
    private String sign;
    private String appcode;

    public void     sendSmsCode(String phone,String code){
        String method="GET";
        String appcode="93asdflasjdflojadfojasodlfjasdjfl";
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization","APPCODE"+appcode);
        HashMap<String, String> querys = new HashMap<>();
        querys.put("code","6379");
        querys.put("phone","13372761079");
        querys.put("sking",skin);
        querys.put("sign",sign);

        try {
            HttpResponse response=HttpUtils.doGet(host,path,method,headers,querys);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
