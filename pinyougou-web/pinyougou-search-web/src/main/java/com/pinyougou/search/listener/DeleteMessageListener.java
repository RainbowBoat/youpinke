package com.pinyougou.search.listener;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.service.ItemSearchService;
import org.springframework.jms.listener.SessionAwareMessageListener;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;

public class DeleteMessageListener implements SessionAwareMessageListener<ObjectMessage> {

    @Reference
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(ObjectMessage objectMessage, Session session) throws JMSException {
        Long[] ids = (Long[]) objectMessage.getObject();

        itemSearchService.delete(ids);
    }
}
