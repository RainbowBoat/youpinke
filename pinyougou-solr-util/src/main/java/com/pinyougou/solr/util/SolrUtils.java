package com.pinyougou.solr.util;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.ItemMapper;
import com.pinyougou.pojo.Item;
import com.pinyougou.solr.SolrItem;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SolrUtils {

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    public void importItemData() {
        try {

            Item item = new Item();
            item.setStatus("1");
            List<Item> items = itemMapper.select(item);

            List<SolrItem> solrItems = SolrUtils.convertItem2SolrItem(items);

            UpdateResponse updateResponse = solrTemplate.saveBeans(solrItems);
            if (updateResponse.getStatus() == 0) {
                solrTemplate.commit();
            } else {
                solrTemplate.rollback();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<SolrItem> convertItem2SolrItem(List<Item> items) {
        List<SolrItem> solrItems = new ArrayList<>();

        try {
            Class itemClass = Class.forName("com.pinyougou.pojo.Item");
            // 与文档域关联的实体类的class
            Class solrItemClass = Class.forName("com.pinyougou.solr.SolrItem");
            Field[] solrItemFields = solrItemClass.getDeclaredFields();

            // 遍历数据库查到的SKU集合
            items.forEach(eachItem -> {
                try {
                    SolrItem solrItem = new SolrItem();

                    // 遍历SolrItem的所有属性
                    for (Field field : solrItemFields) {
                        if ("price".equals(field.getName())) {
                            solrItem.setPrice(eachItem.getPrice());
                            continue;
                        }
                        if ("specMap".equals(field.getName())) {
                            Map specMap = JSON.parseObject(eachItem.getSpec(), Map.class);
                            solrItem.setSpecMap(specMap);
                            continue;
                        }

                        field.setAccessible(true);

                        // 获得跟当前遍历到的SoleItem的属性同名的Item的属性
                        Field itemField = itemClass.getDeclaredField(field.getName());
                        itemField.setAccessible(true);

                        // 使用直接赋值field.set()方法无法给price赋值, 因为类型不匹配, 遂希望通过属性描述器赋值
                        // 创建属性描述器对象
                        // 问题就出在这里, 无法获得price这个属性的描述器
                        // 会抛出异常 java.beans.IntrospectionException: Method not found: setPrice
                        // 猜测原因是price的set方法的形参类型跟price的属性类型不同, 所以这个setPrice方法不算是一个setter方法?
                        PropertyDescriptor descriptor = new PropertyDescriptor(field.getName(), solrItemClass);

                        // 赋值
                        Method writeMethod = descriptor.getWriteMethod();
                        writeMethod.invoke(solrItem, itemField.get(eachItem));
                    }

                    solrItems.add(solrItem);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return solrItems;
    }


    public static void main(String[] args) {
        ApplicationContext ac = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        SolrUtils solrUtils = ac.getBean(SolrUtils.class);
        solrUtils.importItemData();
    }
}
