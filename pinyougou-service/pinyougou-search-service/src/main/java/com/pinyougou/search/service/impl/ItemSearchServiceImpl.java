package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.service.ItemSearchService;
import com.pinyougou.solr.SolrItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemSearchServiceImpl {

    @Autowired
    private SolrTemplate solrTemplate;


    public Map<String, Object> search(Map<String, Object> params) {
        Map<String, Object> data = new HashMap<>();

        String keywords = (String)params.get("keywords");

        if (!StringUtils.isEmpty(keywords)) {

            Integer page = (Integer)params.get("page");
            if (page == null) page = 1;

            Integer rows = (Integer)params.get("rows");
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
                Map<String, String> map = (Map)params.get("spec");
                for (String key : map.keySet()) {
                    Criteria criteria = new Criteria("spec_" + key).is(map.get(key));
                    highlightQuery.addFilterQuery(new SimpleFacetQuery(criteria));
                }
            }

            /*根据价格过滤*/
            if (params.get("price") != null && !"".equals(params.get("price"))) {
                String price = (String)params.get("price");
                String[] priceArr = price.split("-");
                if (!priceArr[0].equals("0")) {
                    Criteria criteria = new Criteria("price").greaterThanEqual(priceArr[0]);
                    highlightQuery.addFilterQuery(new SimpleFacetQuery(criteria));
                }
                if (!priceArr[priceArr.length-1].equals("*")) {
                    Criteria criteria = new Criteria("price").lessThanEqual(priceArr[priceArr.length-1]);
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
            String sortField = (String)params.get("sortField");
            String sortValue = (String)params.get("sortValue");

            if (!StringUtils.isEmpty(sortField) && !StringUtils.isEmpty(sortValue))
                highlightQuery.addSort(new Sort("ASC".equalsIgnoreCase(sortValue) ? Sort.Direction.ASC: Sort.Direction.DESC, sortField));

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

        } else {

            Integer page = (Integer)params.get("page");
            if (page == null) page = 1;

            Integer rows = (Integer)params.get("rows");
            if (rows == null) rows = 20;

            Query query = new SimpleQuery("*:*");

            query.setOffset((page - 1) * rows);
            query.setRows(rows);

            ScoredPage<SolrItem> scoredPage = solrTemplate.queryForPage(query, SolrItem.class);

            data.put("rows", scoredPage.getContent());
            data.put("total", scoredPage.getTotalElements());
            data.put("totalPages", scoredPage.getTotalPages());

        }

        return data;
    }
}
