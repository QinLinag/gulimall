package com.atguigu.gulimall.search.service;


import com.atguigu.common.to.es.SkuEsModel;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public interface ProductSaveService {


    Boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
