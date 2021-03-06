package com.offcn.pojo;

import java.io.Serializable;
import java.util.List;

/**
 * 购物车列表里的购物车
 */
public class Cart implements Serializable {
    private String sellerId;//商家ID
    private String sellerName;//商家名称
    private List<OrderItem> orderItemList;//购物车明细

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public List<OrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List<OrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }
}
