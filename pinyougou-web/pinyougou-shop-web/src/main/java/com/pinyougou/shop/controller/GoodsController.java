package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.pojo.Goods;
import com.pinyougou.service.GoodsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference(timeout = 100000)
    private GoodsService goodsService;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private Destination solrQueue;

    @Autowired
    private Destination solrDeleteQueue;

    @Autowired
    private Destination pageTopic;

    @Autowired
    private Destination pageDeleteTopic;


    @RequestMapping("/save")
    public boolean save(@RequestBody Goods goods) {
        try {
            goods.setSellerId(SecurityContextHolder.getContext().getAuthentication().getName());
            goodsService.save(goods);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @RequestMapping("/update")
    public boolean update(@RequestBody Goods goods) {
        try {

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("/findAsPage2")
    public PageResult findAsPage2(Goods goods, Integer pageNum, Integer rows) {

        goods.setSellerId(SecurityContextHolder.getContext().getAuthentication().getName());

        try {
            if (StringUtils.isNoneBlank(goods.getGoodsName())) {
                try {
                    goods.setGoodsName(new String(goods
                            .getGoodsName().getBytes("ISO8859-1"), "UTF-8"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            PageResult byPage = goodsService.findByPage(goods, pageNum, rows);
            return byPage;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/findAsPage")
    public PageResult findAsPage(Goods goods, Integer pageNum, Integer rows) {

        goods.setSellerId(SecurityContextHolder.getContext().getAuthentication().getName());

        if (StringUtils.isNoneBlank(goods.getGoodsName())) {
            try {
                goods.setGoodsName(new String(goods.getGoodsName().getBytes("ISO8859-1"), "UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        PageResult pageResult = goodsService.findAsPage(goods, pageNum, rows);

        return pageResult;
    }

    @GetMapping("/remove")
    public boolean remove(Long[] ids) {
        try {
            goodsService.deleteAll(ids);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("/up")
    public boolean up(Long[] ids) {
        try {
            goodsService.upAll(ids);
            jmsTemplate.send(solrQueue, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createObjectMessage(ids);
                }
            });
            for (Long id : ids) {
                jmsTemplate.send(pageTopic, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        return session.createTextMessage(id.toString());
                    }
                });
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("/down")
    public boolean down(Long[] ids) {
        try {
            goodsService.downAll(ids);
            jmsTemplate.send(solrDeleteQueue, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createObjectMessage(ids);
                }
            });
            jmsTemplate.send(pageDeleteTopic, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createObjectMessage(ids);
                }
            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
