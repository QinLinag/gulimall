package com.atguigu.gulimall.thirdparty.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class OssController {

    @Autowired
    OSS ossClient;

    @Value("${spring.cloud.alicloud.oss.endpoint}")
    private String endpoint;

    @Value("${spring.cloud.alicloud.oss.bucket}")
    private String bucket;

    @Value("${spring.cloud.alicloud.access-key}")
    private String accessId;


    @RequestMapping("/oss/policy")
    public Map<String,String> policy(){

        String host="https://"+bucket+"."+endpoint;

        String format=new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String dir=format+"/";  //用户上传文件时指定的前缀

        Map<String,String> respMap=null;

        try{
            long expireTime=30;
            long expirEndTime=System.currentTimeMillis()+expireTime*1000;
            Date expiration=new Date(expirEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE,0,1);
            policyConds.addConditionItem(MatchMode.StartWith,PolicyConditions.COND_KEY,dir);

            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = ossClient.calculatePostSignature(postPolicy);

            respMap=new LinkedHashMap<String,String>();
            respMap.put("accessid",accessId);
            respMap.put("policy",encodedPolicy);
            respMap.put("signature",postSignature);
            respMap.put("dir",dir);
            respMap.put("host",host);
            respMap.put("expire",String.valueOf(expirEndTime/1000));


        }catch (Exception e){
            e.printStackTrace();
        }
        return respMap;
    }
























}
