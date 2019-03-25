package com.pinyougou.search.listener;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.Item;
import com.pinyougou.service.GoodsService;
import com.pinyougou.service.ItemSearchService;
import com.pinyougou.solr.SolrItem;
import com.pinyougou.solr.util.SolrUtils;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.jms.listener.SessionAwareMessageListener;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import java.util.List;

public class ItemMessageListener implements SessionAwareMessageListener<ObjectMessage> {

    @Reference(timeout = 100000)
    private GoodsService goodsService;

    @Reference(timeout = 100000)
    private ItemSearchService ItemSearchService;

    @Override
    public void onMessage(ObjectMessage objectMessage, Session session) throws JMSException {
        Long[] ids = (Long[]) objectMessage.getObject();
        List<Item> items = goodsService.findItemByGoodsId(ids);
        List<SolrItem> solrItems = SolrUtils.convertItem2SolrItem(items);
        ItemSearchService.saveOrUpdate(solrItems);
    }
}
