package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.BrandMapper;
import com.pinyougou.mapper.SpecificationMapper;
import com.pinyougou.service.ItemSearchService;
import com.pinyougou.solr.SolrItem;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(interfaceName = "com.pinyougou.service.ItemSearchService")
public class ItemSearchServiceImpl2 implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public Map<String, Object> search(Map<String, Object> params) {
        Map<String, Object> map = new HashMap<>();

        //1. 查询列表
        map.putAll(searchList(params));

        //2. 查询分类列表
        map.put("categoryList", searchCategoryList(params));

        //3. 查询品牌和规格
        if (!StringUtils.isEmpty((String) params.get("category"))) {
            map.putAll(searchBrandAndSpecList((String) params.get("category")));
        }

        return map;
    }

    @Override
    public Map<String, Object> searchList(Map<String, Object> params) {
        Map<String, Object> data = new HashMap<>();

        String keywords = (String) params.get("keywords");

        if (!StringUtils.isEmpty(keywords)) {

            Integer page = (Integer) params.get("page");
            if (page == null) page = 1;

            Integer rows = (Integer) params.get("rows");
            if (rows == null) rows = 20;

            SimpleHighlightQuery highlightQuery = new SimpleHighlightQuery();

            /* 根据分类过滤 */
            if (params.get("category") != null && !"".equals(params.get("category"))) {
                Criteria criteria = new Criteria("category").is(params.get("category"));
                highlightQuery.addFilterQuery(new SimpleFacetQuery(criteria));
            }

            /*根据品牌过滤*/
            if (params.get("brand") != null && !"".equals(params.get("brand"))) {
                Criteria criteria = new Criteria("brand").is(params.get("brand"));
                highlightQuery.addFilterQuery(new SimpleFacetQuery(criteria));
            }

            /*根据规格过滤*/
            if (params.get("spec") != null && !"".equals(params.get("spec"))) {
                Map<String, String> map = (Map) params.get("spec");
                for (String key : map.keySet()) {
                    Criteria criteria = new Criteria("spec_" + key).is(map.get(key));
                    highlightQuery.addFilterQuery(new SimpleFacetQuery(criteria));
                }
            }

            /*根据价格过滤*/
            if (params.get("price") != null && !"".equals(params.get("price"))) {
                String price = (String) params.get("price");
                String[] priceArr = price.split("-");
                if (!priceArr[0].equals("0")) {
                    Criteria criteria = new Criteria("price").greaterThanEqual(priceArr[0]);
                    highlightQuery.addFilterQuery(new SimpleFacetQuery(criteria));
                }
                if (!priceArr[priceArr.length - 1].equals("*")) {
                    Criteria criteria = new Criteria("price").lessThanEqual(priceArr[priceArr.length - 1]);
                    highlightQuery.addFilterQuery(new SimpleFacetQuery(criteria));
                }
            }

            highlightQuery.setOffset((page - 1) * rows);
            highlightQuery.setRows(rows);

            /* 按关键词查找 */
            Criteria criteria = new Criteria("keywords").is(keywords);
            highlightQuery.addCriteria(criteria);

            /* 创建高亮选项 */
            HighlightOptions highlightOptions = new HighlightOptions();
            highlightOptions.addField("title");

            /* 设置高亮前后缀 */
            highlightOptions.setSimplePrefix("<font color='red'>");
            highlightOptions.setSimplePostfix("</font>");

            /* 设置高亮选项 */
            highlightQuery.setHighlightOptions(highlightOptions);

            /* 设置排序 */
            String sortField = (String) params.get("sortField");
            String sortValue = (String) params.get("sortValue");

            if (!StringUtils.isEmpty(sortField) && !StringUtils.isEmpty(sortValue))
                highlightQuery.addSort(new Sort("ASC".equalsIgnoreCase(sortValue) ? Sort.Direction.ASC : Sort.Direction.DESC, sortField));

            HighlightPage<SolrItem> highlightPage = solrTemplate.queryForHighlightPage(highlightQuery, SolrItem.class);

            List<HighlightEntry<SolrItem>> highlighted = highlightPage.getHighlighted();

            /* 将高亮部分设置到查询结果 */
            for (HighlightEntry<SolrItem> solrItemHighlightEntry : highlighted) {
                SolrItem entity = solrItemHighlightEntry.getEntity();
                List<HighlightEntry.Highlight> highlights = solrItemHighlightEntry.getHighlights();
                if (highlights != null && highlights.size() > 0 && highlights.get(0).getSnipplets().size() > 0) {
                    entity.setTitle(highlights.get(0).getSnipplets().get(0));
                }
            }

            data.put("rows", highlightPage.getContent());

            data.put("total", highlightPage.getTotalElements());
            data.put("totalPages", highlightPage.getTotalPages());

        }
        return data;
    }

    @Override
    public List<String> searchCategoryList(Map<String, Object> params) {

        List list = new ArrayList();

        Query query = new SimpleQuery("*:*");

        Criteria criteria = new Criteria().is(params.get("keywords"));
        query.addCriteria(criteria);

        GroupOptions groupOptions = new GroupOptions().addGroupByField("category");
        query.setGroupOptions(groupOptions);

        GroupPage<SolrItem> page = solrTemplate.queryForGroupPage(query, SolrItem.class);

        GroupResult<SolrItem> result = page.getGroupResult("category");
        Page<GroupEntry<SolrItem>> groupEntries = result.getGroupEntries();
        List<GroupEntry<SolrItem>> entryList = groupEntries.getContent();
        for (GroupEntry<SolrItem> entry : entryList) {
            list.add(entry.getGroupValue());
        }

        return list;
    }

    @Override
    public void saveOrUpdate(List<SolrItem> solrItems) {
        UpdateResponse updateResponse = solrTemplate.saveBeans(solrItems);
        if (updateResponse.getStatus() == 0) {
            solrTemplate.commit();
        } else {
            solrTemplate.rollback();
        }

    }

    @Override
    public void delete(Long[] ids) {
        Query query = new SimpleQuery();

        Criteria criteria = new Criteria("goodsId").in(ids);
        query.addCriteria(criteria);

        UpdateResponse updateResponse = solrTemplate.delete(query);
        if (updateResponse.getStatus() == 0) {
            solrTemplate.commit();
        } else {
            solrTemplate.rollback();
        }
    }

    private Map<String, Object> searchBrandAndSpecList(String categoryName) {
        Map<String, Object> map = new HashMap<>();
        Long typeTemplateId = (Long) redisTemplate.boundHashOps("itemCat").get(categoryName);
        List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeTemplateId);
        map.put("brandList", brandList);

        List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeTemplateId);
        map.put("specList", specList);
        return map;
    }



}
