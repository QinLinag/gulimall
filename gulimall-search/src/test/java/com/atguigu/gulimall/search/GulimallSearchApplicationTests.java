package com.atguigu.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
class GulimallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;



    @Test
    public void searchData() throws IOException {
        //1.创建检索请求
        SearchRequest searchRequest = new SearchRequest();
        //2.指定索引
        searchRequest.indices("bank");
        //指定DSL,检索条件
        //SearchSourceBuilder sourceBuilder
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //1) 构造检索条件
        sourceBuilder.query(QueryBuilders.matchQuery("address","mill"));

        //2)按照年龄进行聚合，
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        sourceBuilder.aggregation(ageAgg);
        //按照平均工资进行聚合
        AvgAggregationBuilder avgAgg = AggregationBuilders.avg("balanceAvg").field("balance");
        sourceBuilder.aggregation(avgAgg);

        searchRequest.source(sourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

        //3.分析结果searchResponse
        Map map = JSON.parseObject(searchResponse.toString(), Map.class);

        //3.1）获取所有查到的数据
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();

        //3.2)获取这次检索到的分析信息
        Aggregations aggregations = searchResponse.getAggregations();

        Terms ageAgg1 = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : ageAgg1.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            System.out.println("年龄 "+keyAsString+"==>"+bucket.getDocCount());
        }

        Avg balanceAvg = aggregations.get("avgAgg");
        System.out.println("平均薪资 "+balanceAvg.getValue());
    }



    @Test
    public void indexData(){    //插入操作
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");   //数据id
        //indexRequest.source("username","qinliang","age",18,"gender","man");
        User user = new User();
        user.setAge(10);
        user.setName("qinlaing");
        String jsonString = JSON.toJSONString(user);
        indexRequest.source(jsonString, XContentType.JSON);    //要保存的内容
    }

    @Data
    class User{
        private String name;
        private int age;
    }










    @Test
    void contextLoads() {
    }

}
