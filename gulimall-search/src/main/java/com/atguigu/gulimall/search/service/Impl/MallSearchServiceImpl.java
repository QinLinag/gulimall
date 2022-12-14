package com.atguigu.gulimall.search.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.feign.ProductFeignService;
import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.AttrResponseVo;
import com.atguigu.gulimall.search.vo.BrandVo;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    RestHighLevelClient client;

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam param) {
        //1.??????????????????????????????DSL??????
        SearchResult result=null;

        //2.?????????????????????
        SearchRequest searchRequest = buildSearchRequest(param);
        try{
            //2.??????????????????
            SearchResponse response = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

            //3.????????????????????????????????????????????????
            result=builSearchResult(response,param);
        }catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }



    private SearchResult builSearchResult(SearchResponse response,SearchParam param) {
        SearchResult result=new SearchResult();

        //1.?????????????????????????????????
        SearchHits hits = response.getHits();
        List<SkuEsModel> esModels=new ArrayList<>();
        if (hits.getHits()!=null&&hits.getHits().length>0){
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();   //??????json?????????
                SkuEsModel esModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                //????????????
                if (!StringUtils.isEmpty(param.getKeyword())){
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(string);
                }
                esModels.add(esModel);
            }
        }
        result.setProducts(esModels);

        //2.????????????????????????????????????????????????
        List<SearchResult.AttrVo> attrVos =new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        if (attr_agg!=null){
            ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
            if (attr_id_agg!=null){
                List<? extends Terms.Bucket> buckets = attr_id_agg.getBuckets();
                if (buckets!=null&buckets.size()>0){
                    for (Terms.Bucket bucket : buckets) {
                        //???????????????id
                        SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
                        long arrId = bucket.getKeyAsNumber().longValue();
                        attrVo.setAttrId(arrId);
                        //?????????????????????
                        String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
                        attrVo.setAttrName(attrName);
                        //????????????????????????
                        List<String> attrValues = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(item -> {
                            String keyAsString = ((Terms.Bucket) item).getKeyAsString();
                            return keyAsString;
                        }).collect(Collectors.toList());
                        attrVo.setAttrValue(attrValues);

                        attrVos.add(attrVo);
                    }
                }
            }
        }

        result.setAttrs(attrVos);

        //3.????????????????????????????????????????????????
        List<SearchResult.BrandVo> brandVos=new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        if (brand_agg!=null){
            List<? extends Terms.Bucket> buckets1 = brand_agg.getBuckets();
            if (buckets1!=null&&buckets1.size()>0){
                for (Terms.Bucket bucket : buckets1) {
                    SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
                    //1.???????????????id
                    long brandId = bucket.getKeyAsNumber().longValue();
                    brandVo.setBrandId(brandId);
                    //2.?????????????????????
                    String brandName = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();
                    brandVo.setBrandName(brandName);
                    //3.?????????????????????
                    String brandImg= ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
                    brandVo.setBrandImg(brandImg);
                    brandVos.add(brandVo);
                }
            }
        }
        result.setBrands(brandVos);

        //4.????????????????????????????????????????????????
        List<SearchResult.CatalogVo> catalogVos=new ArrayList<>();
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        if (catalog_agg!=null){
            List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
            if (buckets!=null&&buckets.size()>0){
                for (Terms.Bucket bucket : buckets) {
                    SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
                    //????????????id
                    String keyAsString = bucket.getKeyAsString();
                    catalogVo.setCatalogId(Long.parseLong(keyAsString));

                    //???????????????   ????????????????????????
                    ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
                    String catalog_name = catalog_name_agg.getBuckets().get(0).getKeyAsString();  //??????????????????
                    catalogVo.setCatalogName(catalog_name);
                    catalogVos.add(catalogVo);
                }
            }
        }

        result.setCatalogs(catalogVos);


        //6.???????????????????????????
        if (param.getAttrs()!=null&&param.getAttrs().size()>0){
            List<SearchResult.NavVo> collect=param.getAttrs().stream().map(attr->{
                //1.????????????attrs???????????????????????????
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                //attr=2_5???:6???
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R r = productFeignService.attrinfo(Long.parseLong(s[0]));
                if(r!=null&&r.getCode()==0){
                    AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(data.getAttrName());
                }else{
                    navVo.setNavName(s[0]);
                }

                //?????????????????????????????????????????????????????????????????????url?????????????????????
                String replace = replaceQueryString(param, attr,"attrs");
                navVo.setLink("http://search.gulimall.com/list.html?"+replace);
                return navVo;
            }).collect(Collectors.toList());

            //????????? ??????,
            if (param.getBrandId()!=null&&param.getBrandId().size()>0){
                List<SearchResult.NavVo> navs = result.getNavs();
                SearchResult.NavVo navVo = new SearchResult.NavVo();

                navVo.setNavName("??????");
                //TODO ????????????????????????
                R r = productFeignService.brandsInfo(param.getBrandId());
                if (r.getCode()==0){
                    List<BrandVo> brand = r.getData("brand", new TypeReference<List<BrandVo>>() {
                    });
                    StringBuffer buffer = new StringBuffer();
                    String replace =null;
                    for (BrandVo brandVo : brand) {
                        buffer.append(brandVo.getBrandName()+";");
                        replace = replaceQueryString(param, brandVo.getBrandId() + "", "brandId");
                    }
                    navVo.setNavValue(buffer.toString());
                    navVo.setLink("http://search.gulimall.com/list.html?"+replace);
                }
                navs.add(navVo);
            }


            result.setNavs(collect);
        }






        return result;
    }

    @NotNull
    private String replaceQueryString(SearchParam param, String attr,String key) {
        String encode=null;
        try {
            encode= URLEncoder.encode(attr,"UTF-8");
            encode=encode.replace("+","%20"); //???????????????????????????java?????????
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String replace = param.get_queryString().replace("&"+key + encode, "");
        return replace;
    }


    //?????????????????????
    private SearchRequest buildSearchRequest(SearchParam param){
        SearchSourceBuilder sourceBuider = new SearchSourceBuilder();

        //1.??????bool - query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        //1.1must-???????????????
        if (!StringUtils.isEmpty(param.getKeyword())){

            boolQuery.must(QueryBuilders.matchQuery("skuTitle",param.getKeyword()));
        }

        //1.2 bool -filter ??????????????????id?????????
        if (param.getCatalog3Id()!=null){
            boolQuery.filter(QueryBuilders.termQuery("catalogId",param.getCatalog3Id()));
        }

        //1.2 bool -filter ????????????id?????????
        if(param.getBrandId()!=null&&param.getBrandId().size()>0){
            boolQuery.filter(QueryBuilders.termQuery("brandId",param.getBrandId()));
        }

        //1.2 bool-filter ??????????????????????????????????????????
        if(param.getAttrs()!=null&&param.getAttrs().size()>0){
            for (String attrStr : param.getAttrs()) {
                //attrs=1_5???:8???&attrs=2_16G:8G
                BoolQueryBuilder nestedboolQuery = QueryBuilders.boolQuery();
                //attr=1_5???:8???
                String[] s = attrStr.split("_");
                String attrId=s[0];   //???????????????id
                String[] attrValues=s[1].split(":");  //???????????????
                nestedboolQuery.must(QueryBuilders.termQuery("attrs.attrId",attrId));
                nestedboolQuery.must(QueryBuilders.termQuery("attrs.attrValue",attrValues));
                //???????????????????????????nested??????
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedboolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }

        //1.2 bool-filter ?????????????????????????????????
        boolQuery.filter(QueryBuilders.termQuery("hasStock",param.getHasStrock()==1));

        //1.2 bool-filter  ??????????????????
        if(!StringUtils.isEmpty(param.getSkuPrice())){
            //1_500/_500/500_
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");   //???_?????????
            if(s.length==2){
                //??????
                rangeQuery.gte(s[0]).lte(s[1]);
            } else if (s.length==1) {
                if (param.getSkuPrice().startsWith("_")){
                    rangeQuery.gte(s[0]);
                }else if (param.getSkuPrice().endsWith("_")){
                    rangeQuery.lte(s[0]);
                }
            }
        }



        //2.1??????
        if(!StringUtils.isEmpty(param.getSort())){
            String sort = param.getSort();
            //sort=hotScore_asc/dec
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            sourceBuider.sort(s[0],order);
        }

        //2.2??????
        sourceBuider.from((param.getPageNum()-1)*EsConstant.PRODUCT_PAGESIZE);
        sourceBuider.size(EsConstant.PRODUCT_PAGESIZE);

        //2.3??????
        if(!StringUtils.isEmpty(param.getKeyword())){
            HighlightBuilder builder = new HighlightBuilder();
            builder.field("skuTitle");
            builder.preTags("<b style='color:red'");
            builder.postTags("</b>");
            sourceBuider.highlighter(builder);
        }


        //3????????????
        //3.1????????????
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);

        //????????????????????????
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        sourceBuider.aggregation(brand_agg);

        //3.2????????????
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        sourceBuider.aggregation(catalog_agg);

        //3.3????????????
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        //?????????????????????attrId
        NestedAggregationBuilder attr_id_agg = AggregationBuilders.nested("attr_id_agg", "attrs.attrId");
        //????????????attr_id???????????????
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        //????????????attr_id?????????????????????????????????attrValue
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_valur_agg").field("attrs.attrValue").size(50));


        attr_agg.subAggregation(attr_id_agg);

        sourceBuider.aggregation(attr_agg);


        sourceBuider.query(boolQuery);
        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuider);
        return searchRequest;
    }


















}
