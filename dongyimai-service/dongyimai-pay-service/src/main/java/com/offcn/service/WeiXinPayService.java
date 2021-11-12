package com.offcn.service;

import java.util.Map;

public interface WeiXinPayService {
    /**
     * 创建二维码(地址)
     */
    Map createNative(Map<String,String> paramMap) throws Exception;

    /**
     * 查询交易状态
     * @param out_trade_no 订单号
     */
    public Map queryPayStatus(String out_trade_no);
}
