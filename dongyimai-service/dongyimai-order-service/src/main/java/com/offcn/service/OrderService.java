package com.offcn.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.offcn.entity.PageResult;
import com.offcn.pojo.Order;

import java.util.List;

/****
 * @Author:ujiuye
 * @Description:Order业务层接口
 * @Date 2021/2/1 14:19
 *****/

public interface OrderService extends IService<Order> {

    /**
     * 修改订单状态
     * 1.支付时间 payment_time
     * 2.支付状态（是否支付） status
     * 3.shipping_code 订单流水账号（物流号） shipping_code
     * @param out_trade_no 订单号
     * @param paytime 支付时间
     * @param transactionid 订单流水号（交易流水号）
     */
    void updateStatus(String out_trade_no,String paytime,String transactionid);

    /**支付失败
     * 逻辑删除订单，回滚库存
     * @param out_trade_no
     */
    void deleteOrder(String out_trade_no);

    /***
     * Order多条件分页查询
     * @param order
     * @param page
     * @param size
     * @return
     */
    PageResult<Order> findPage(Order order, int page, int size);

    /***
     * Order分页查询
     * @param page
     * @param size
     * @return
     */
    PageResult<Order> findPage(int page, int size);

    /***
     * Order多条件搜索方法
     * @param order
     * @return
     */
    List<Order> findList(Order order);

    /***
     * 删除Order
     * @param id
     */
    void delete(Long id);

    /***
     * 修改Order数据
     * @param order
     */
    void update(Order order);

    /***
     * 新增Order,购物车总的订单项
     * @param order 新增的订单
     * @Param
     */
    void add(Order order);

    /**
     * 根据ID查询Order
     * @param id
     * @return
     */
     Order findById(Long id);

    /***
     * 查询所有Order
     * @return
     */
    List<Order> findAll();
}
