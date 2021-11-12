package com.offcn.search.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.offcn.entity.Result;
import com.offcn.search.dao.SkuEsMapper;
import com.offcn.search.pojo.SkuInfo;
import com.offcn.search.service.SkuService;
import com.offcn.sellergoods.feign.ItemFeign;
import com.offcn.sellergoods.pojo.Item;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private SkuEsMapper skuEsMapper;

    @Autowired
    private ItemFeign itemFeign;

    @Override
    public void importSku() {
        //调用商品微服务，获取sku商品数据
        Result<List<Item>> result = itemFeign.findByStatus("1");
        //把数据转换为搜索实体类数据
        /**
         * 获取sku信息item对象，将item对象的某些信息提取出来封装成SkuInfo对象，此时的SkuInfo的specMap属性是没有值的，遍历该对象，
         * 将spec规格属性复制到SpecMap属性中，
         * 然后将所有的SkuInfo保存到es中
         */
        List<SkuInfo> skuInfoList = JSON.parseArray(JSON.toJSONString(result.getData()), SkuInfo.class);
        //遍历sku集合
        for (SkuInfo skuInfo : skuInfoList) {
            //获取规格
            Map<String,Object> specMap= JSON.parseObject(skuInfo.getSpec());
            //关联设置到specMap
            skuInfo.setSpecMap(specMap);
        }

        //保存sku集合数据到es
        skuEsMapper.saveAll(skuInfoList);
    }
    //===========================导入es完毕==========================================


//    @Override
//    public Map search(Map<String, String> searchMap) {
//        //1.获取关键字的值
//        String keywords = searchMap.get("keywords");
//
//        if (StringUtils.isEmpty(keywords)) {
//            keywords = "华为";//赋值给一个默认的值
//        }
//        //2.创建查询对象 的构建对象
//        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
//
//        //3.设置查询的条件
//        //使用：QueryBuilders.matchQuery("title", keywords) ，搜索华为 ---> 华    为 二字可以拆分查询，
//        //使用：QueryBuilders.matchPhraseQuery("title", keywords) 华为二字不拆分查询
//        nativeSearchQueryBuilder.withQuery(QueryBuilders.matchQuery("title", keywords));
//
//        //4.构建查询对象
//        NativeSearchQuery query = nativeSearchQueryBuilder.build();
//
//        //5.执行查询
//        SearchHits<SkuInfo> searchHits = elasticsearchRestTemplate.search(query, SkuInfo.class);
//        //对搜索searchHits集合进行分页封装
//        SearchPage<SkuInfo> skuPage = SearchHitSupport.searchPageFor(searchHits, query.getPageable());
//        //遍历取出查询的商品信息
//        List<SkuInfo> skuList=new ArrayList<>();
//        for (SearchHit<SkuInfo> searchHit :skuPage.getContent()) { // 获取搜索到的数据
//            SkuInfo content = searchHit.getContent();
//            skuList.add(content);
//        }
//
//        //6.返回结果
//        Map resultMap = new HashMap<>();
//        resultMap.put("rows", skuList);//获取所需SkuInfo集合数据内容
//        resultMap.put("total",searchHits.getTotalHits());//总记录数
//        resultMap.put("totalPages", skuPage.getTotalPages());//总页数
//        return resultMap;
//    }

    /**
     * 更改上面的查询方法，添加分组功能
     * 上面的方法根据关键字查询出了相应的数据，但是这些数据所属的category通可能不相同，这些数据共有多少种类，
     * 将其以categoryList的方式显示出来更好
     */
    @Override
    public Map search(Map<String, String> searchMap) {
        //1.获取关键字的值
        String keywords = searchMap.get("keywords");

        if (StringUtils.isEmpty(keywords)) {
            keywords = "华为";//赋值给一个默认的值
        }
        //2.创建查询对象 的构建对象
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();

        // 设置分组的条件  terms后表示分组查询后的列名
        // 需要按照字段category来进行分组，分组后的列名是skuCategorygroup
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategorygroup").field("category"));
        //3.设置查询的条件，在title属性里面找关键字
        //使用：QueryBuilders.matchQuery("title", keywords) ，搜索华为 ---> 华    为 二字可以拆分查询，
        //使用：QueryBuilders.matchPhraseQuery("title", keywords) 华为二字不拆分查询
        nativeSearchQueryBuilder.withQuery(QueryBuilders.matchQuery("title", keywords));

        //设置分组条件  商品品牌
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuBrandgroup").field("brand").size(101));

        //设置分组条件  商品的规格
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuSpecgroup").field("spec.keyword").size(100));

        //========================过滤查询 开始=====================================
        //3.创建多条件组合查询对象
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //设置品牌查询条件
        if (!StringUtils.isEmpty(searchMap.get("brand"))) {
            boolQueryBuilder.filter(QueryBuilders.matchQuery("brand", searchMap.get("brand")));
        }
        //设置分类查询条件
        if (!StringUtils.isEmpty(searchMap.get("category"))) {
            boolQueryBuilder.filter(QueryBuilders.matchQuery("category", searchMap.get("category")));
        }

        //设置规格过滤查询
        if (searchMap != null) {
            for (String key : searchMap.keySet()) {//{ brand:"",category:"",spec_网络:"电信4G"}
                if (key.startsWith("spec_")) {
                    //截取规格的名称
                    boolQueryBuilder.filter(QueryBuilders.termQuery("specMap." + key.substring(5) + ".keyword", searchMap.get(key)));
                }
            }
        }

        //价格过滤查询
        String price = searchMap.get("price");
        if (!StringUtils.isEmpty(price)) {
            String[] split = price.split("-");
            if (!split[1].equalsIgnoreCase("*")) {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").from(split[0], true).to(split[1], true));
            } else {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(split[0]));
            }
        }
        //构建过滤查询
        nativeSearchQueryBuilder.withFilter(boolQueryBuilder);
        //========================过滤查询 结束=====================================

        //4.构建排序查询
        String sortRule = searchMap.get("sortRule");
        String sortField = searchMap.get("sortField");
        if (!StringUtils.isEmpty(sortRule) && !StringUtils.isEmpty(sortField)) {
            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(sortField).order(sortRule.equals("DESC") ? SortOrder.DESC : SortOrder.ASC));
        }

        //5.分页查询
        if (!StringUtils.isEmpty(searchMap.get("pageNum"))) {
            Integer pageNum = Integer.parseInt(searchMap.get("pageNum"));     //当前页码
            if (pageNum == null) {
                pageNum = 1;
            }
            Integer pageSize = 10;              //每页显示记录数


            nativeSearchQueryBuilder.withPageable(PageRequest.of(pageNum - 1, pageSize));
        }

        //6.设置高亮条件
        nativeSearchQueryBuilder.withHighlightFields(new HighlightBuilder.Field("title"));
        nativeSearchQueryBuilder.withHighlightBuilder(new HighlightBuilder().preTags("<em style=\"color:red\">").postTags("</em>"));
        //设置主关键字查询,修改为多字段的搜索条件，再这三个字段里只要出现了关键字，都会被搜出来，且关键字会高亮显示
        nativeSearchQueryBuilder.withQuery(QueryBuilders.multiMatchQuery(keywords,"title","brand","category"));

        //7.构建查询对象
        NativeSearchQuery query = nativeSearchQueryBuilder.build();

        //8.执行查询
        SearchHits<SkuInfo> searchHits = elasticsearchRestTemplate.search(query, SkuInfo.class);
        //9.对搜索searchHits集合进行分页封装
        SearchPage<SkuInfo> skuPage = SearchHitSupport.searchPageFor(searchHits, query.getPageable());

        //10.获取分组结果
        Terms terms = searchHits.getAggregations().get("skuCategorygroup");
        //获取分组结果 商品品牌
        Terms termsBrand = searchHits.getAggregations().get("skuBrandgroup");
        //获取分组结果 商品规格
        Terms termsSpec = searchHits.getAggregations().get("skuSpecgroup");
        // 获取分类名称集合
        List<String> categoryList = getStringsCategoryList(terms);
        // 获取品牌名称集合
        List<String> brandList = getStringsBrandList(termsBrand);
        //获取规格名称集合
        Map<String, Set<String>> specMap=getStringSetMap(termsSpec);
        //遍历取出查询的商品信息
        List<SkuInfo> skuList=new ArrayList<>();
        for (SearchHit<SkuInfo> searchHit :skuPage.getContent()) { // 获取搜索到的数据
            SkuInfo content = (SkuInfo) searchHit.getContent();
            SkuInfo skuInfo = new SkuInfo();
            BeanUtils.copyProperties(content, skuInfo);
            //=============高亮设置==========================
            Map<String,List<String>>  highlightFields = searchHit.getHighlightFields();
            for(Map.Entry<String,List<String>> stringHighlightFieldEntry :highlightFields.entrySet()){
                String key = stringHighlightFieldEntry.getKey();
                if(StringUtils.equals(key,"title")){
                    List<String> fragments = stringHighlightFieldEntry.getValue();
                    StringBuilder sub = new StringBuilder();
                    for(String fragment:fragments){
                        sub.append(fragment.toString());
                    }
                    skuInfo.setTitle(sub.toString());
                }
            }
            //=============高亮设置完毕=======================
            skuList.add(skuInfo);
        }

        //11.返回结果
        Map resultMap = new HashMap<>();
        resultMap.put("rows", skuList);//获取所需SkuInfo集合数据内容
        resultMap.put("total",searchHits.getTotalHits());//总记录数
        resultMap.put("totalPages", skuPage.getTotalPages());//总页数
        resultMap.put("categoryList",categoryList);
        resultMap.put("brandList", brandList);
        resultMap.put("specMap", specMap);
        //分页数据保存
        //设置当前页码
        //分页数据保存
        //设置当前页码
        /*resultMap.put("pageNum", pageNum);
        resultMap.put("pageSize", 30);*/
        return resultMap;
    }

    /**
     * 获取分类列表数据
     * @param terms
     * @return
     **/
    private List<String> getStringsCategoryList(Terms terms) {
        List<String> categoryList = new ArrayList<>();

        if (terms != null) {
            for (Terms.Bucket bucket : terms.getBuckets()) {
                String keyAsString = bucket.getKeyAsString();// 分组的值（分类名称）
                categoryList.add(keyAsString);
            }
        }
        return categoryList;
    }

    /**
     * 获取品牌列表数据
     * @param termsBrand
     * @return
     **/
    private List<String> getStringsBrandList(Terms termsBrand) {
        List<String> brandList = new ArrayList<>();

        if (termsBrand != null) {
            for (Terms.Bucket bucket : termsBrand.getBuckets()) {
                String keyAsString = bucket.getKeyAsString();// 分组的值（分类名称）
                brandList.add(keyAsString);
            }
        }
        return brandList;
    }

    /**
     * 获取规格列表数据
     *
     * @param termsSpec
     * @return
     */
    private Map<String, Set<String>> getStringSetMap(Terms termsSpec) {
        Map<String, Set<String>> specMap = new HashMap<String, Set<String>>();
        Set<String> specList = new HashSet<>();
        if (termsSpec != null) {
            for (Terms.Bucket bucket : termsSpec.getBuckets()) {
                specList.add(bucket.getKeyAsString());
            }
        }
        for (String specjson : specList) {
            Map<String, String> map = JSON.parseObject(specjson, Map.class);
            for (Map.Entry<String, String> entry : map.entrySet()) {//
                String key = entry.getKey();        //规格名字
                String value = entry.getValue();    //规格选项值
                //获取当前规格名字对应的规格数据
                Set<String> specValues = specMap.get(key);
                if (specValues == null) {
                    specValues = new HashSet<String>();
                }
                //将当前规格加入到集合中
                specValues.add(value);
                //将数据存入到specMap中
                specMap.put(key, specValues);
            }
        }
        return specMap;
    }
}

