package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.common.util.HttpClientUtils;
import com.pinyougou.service.WeChatPayService;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

@Service(interfaceName = "com.pinyougou.service.WeChatPayService")
public class WeChatPayServiceImpl implements WeChatPayService {

    @Value("${appid}")
    private String appid;
    @Value("${partner}")
    private String partner;
    @Value("${partnerkey}")
    private String partnerkey;
    @Value("${unifiedorder}")
    private String unifiedorder;
    @Value("${orderquery}")
    private String orderquery;
    @Value("${closeorder}")
    private String closeorder;

    @Override
    public Map<String, Object> genPayCode(String outTradeNo, String totalFee) {
        Map<String, String> xmlParam = new HashMap<>();
        xmlParam.put("appid", appid);
        xmlParam.put("mch_id", partner);
        xmlParam.put("nonce_str", WXPayUtil.generateNonceStr());
        xmlParam.put("body", "品优购");
        xmlParam.put("out_trade_no", outTradeNo);
        xmlParam.put("total_fee", totalFee);
        xmlParam.put("spbill_create_ip", "127.0.0.1");
        xmlParam.put("notify_url", "http://test.itcast.cn");
        xmlParam.put("trade_type", "NATIVE");

        try {

            String generateSignedXml = WXPayUtil.generateSignedXml(xmlParam, partnerkey);
            HttpClientUtils httpClientUtils = new HttpClientUtils(true);
            String result = httpClientUtils.sendPost(unifiedorder, generateSignedXml);
            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
            Map<String, Object> data = new HashMap<>();
            data.put("codeUrl", resultMap.get("code_url"));
            data.put("outTradeNo", String.valueOf(outTradeNo));
            data.put("totalFee", totalFee);
            return data;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, String> queryPayStatus(String outTradeNo) {
        Map<String, String> paramMap = new HashMap<>(5);
        paramMap.put("appid", appid);
        paramMap.put("mch_id", partner);
        paramMap.put("out_trade_no", outTradeNo);
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr());

        try {
            String paramXml = WXPayUtil.generateSignedXml(paramMap, partnerkey);
            System.out.println("请求参数: " + paramXml);
            HttpClientUtils httpClientUtils = new HttpClientUtils(true);
            String result = httpClientUtils.sendPost(orderquery, paramXml);
            System.out.println("响应数据: " + result);
            return WXPayUtil.xmlToMap(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, String> closePayTimeOut(String outTradeNo) {
        /** 创建Map集合封装请求参数 */
        Map<String, String> params = new HashMap<>();
        /** 公众账号 */
        params.put("appid", appid);
        /** 商户账号 */
        params.put("mch_id", partner);
        /** 订单交易号 */
        params.put("out_trade_no", outTradeNo);
        /** 随机字符串 */
        params.put("nonce_str", WXPayUtil.generateNonceStr());
        try {
            /** 生成签名的xml参数 */
            String xmlParam = WXPayUtil.generateSignedXml(params, partnerkey);
            System.out.println("请求参数：" + xmlParam);
            /** 创建HttpClientUtils对象 */
            HttpClientUtils client = new HttpClientUtils(true);
            /** 发送post请求，得到响应数据 */
            String result = client.sendPost(closeorder, xmlParam);
            System.out.println("响应数据：" + result);
            /** 将xml响应数据转化成Map */
            return WXPayUtil.xmlToMap(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
