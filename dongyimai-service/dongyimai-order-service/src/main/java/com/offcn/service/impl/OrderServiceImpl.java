package com.offcn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.offcn.dao.OrderItemMapper;
import com.offcn.dao.OrderMapper;
import com.offcn.entity.PageResult;
import com.offcn.pojo.Cart;
import com.offcn.pojo.Order;
import com.offcn.pojo.OrderItem;
import com.offcn.sellergoods.feign.ItemFeign;
import com.offcn.service.OrderService;
import com.offcn.util.IdWorker;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/****
 * @Author:ujiuye
 * @Description:Order业务层接口实现类
 * @Date 2021/2/1 14:19
 *****/
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private RedisTemplate redisTemplate;

    //完善id
    @Autowired
    private IdWorker idWorker;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ItemFeign itemFeign;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 修改订单状态
     * 1.支付时间 payment_time
     * 2.支付状态（是否支付） status
     * 3.shipping_code 订单流水账号（物流号） shipping_code
     * @param out_trade_no 订单号
     * @param paytime 支付时间
     * @param transactionid 订单流水号（交易流水号）
     */
    @Override
    public void updateStatus(String out_trade_no, String paytime, String transactionid) {
        //查询订单
        Order order = orderMapper.selectById(out_trade_no);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date payDate = null;
        try {
            payDate = dateFormat.parse(paytime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //修改订单状态
        order.setPaymentTime(payDate);//支付时间
        order.setShippingCode(transactionid);//交易流水号
        order.setStatus("2");//表示已支付
        orderMapper.updateById(order);//再次存到数据库
    }

    /**
     * 支付失败
     * 逻辑删除订单，回滚库存
     */
    @Override
    public void deleteOrder(String out_trade_no){
        //查询订单
        Order order = orderMapper.selectById(out_trade_no);
        order.setUpdateTime(new Date());//设置更新时间
        order.setStatus("1");//支付状态为未支付
        orderMapper.updateById(order);//再次存到数据库
        //回滚库存（作业）

    }

//    @Autowired
//    private UserFeign userFeign;

//    @Autowired
//    private UserFeign userFeign;

    /**提交订单
     * 增加Order，向tb_order表中添加订单信息
     * @param order
     */
//    @Override
//    public void add(Order order){
//        // 获取该用户的购物车数据
//        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
//        //获取商家的购物车，开始生成该商家订单表，不同的商家对应不同的订单
//        for (Cart cart : cartList) {
//            long orderId = idWorker.nextId();//生成订单id
//            System.out.println("sellerId:" + cart.getSellerId());//商家id
//            System.out.println("orderId:" + orderId);//订单id
//            Order tborder = new Order();// 新创建订单对象对于tb_order，将前端传进来的订单信息也加到里面
//            tborder.setOrderId(orderId);// 订单ID
//            tborder.setUserId(order.getUserId());// 用户名
//            tborder.setPaymentType(order.getPaymentType());// 支付类型
//            tborder.setStatus("1");// 状态：未付款
//            tborder.setCreateTime(new Date());// 订单创建日期
//            tborder.setUpdateTime(new Date());// 订单更新日期
//            tborder.setReceiverAreaName(order.getReceiverAreaName());// 收获地址
//            tborder.setReceiverMobile(order.getReceiverMobile());// 收货人手机号
//            tborder.setReceiver(order.getReceiver());// 收货人
//            tborder.setSourceType(order.getSourceType());// 订单来源
//            tborder.setSellerId(cart.getSellerId());// 商家ID
//            double money = 0;//该订单的总金额（b2b2c模式，一个商家对应一个订单，一个订单对应多个订单明细）
//            // 循环购物车明细，获取orderItem添加一些属性，开始生成订单详情表，并统计该订单的总金额
//            for (OrderItem orderItem : cart.getOrderItemList()) {
//                orderItem.setId(idWorker.nextId());// 添加 生成订单明细id
//                orderItem.setOrderId(orderId);// 添加 订单明细表中设置所属订单ID（一对多）
//                orderItem.setSellerId(cart.getSellerId());// 添加 商家id
//                money += Double.parseDouble(orderItem.getTotalFee());// 金额累加（存到订单表tb_order中）
//                System.out.println("orderItem.getId():"+orderItem.getId());
//
//                //1.保存订单明细到数据库中
//                orderItemMapper.insert(orderItem);
//            }
//            //设置订单总金额
//            tborder.setPayment(money+"");
//            //2.保存订单到数据库
//            orderMapper.insert(tborder);
//        }
//        //增加积分，调用用户微服务的userFeign 增加积分
//       // userFeign.addPoints(10);
//        //3.减少库存
//        itemFeign.decrCount(order.getUserId());
//        //4.删除该用户的购物车数据
//        redisTemplate.boundHashOps("cartList").delete(order.getUserId());
//
//    }

    @Override
    public void add(Order order){
        // 获取该用户的购物车数据
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
        //获取商家的购物车，开始生成该商家订单表，不同的商家对应不同的订单
        for (Cart cart : cartList) {
            long orderId = idWorker.nextId();//生成订单id
            System.out.println("sellerId:" + cart.getSellerId());//商家id
            System.out.println("orderId:" + orderId);//订单id
            Order tborder = new Order();// 新创建订单对象对于tb_order，将前端传进来的订单信息也加到里面
            tborder.setOrderId(orderId);// 订单ID
            tborder.setUserId(order.getUserId());// 用户名
            tborder.setPaymentType(order.getPaymentType());// 支付类型
            tborder.setStatus("1");// 状态：未付款
            tborder.setCreateTime(new Date());// 订单创建日期
            tborder.setUpdateTime(new Date());// 订单更新日期
            tborder.setReceiverAreaName(order.getReceiverAreaName());// 收获地址
            tborder.setReceiverMobile(order.getReceiverMobile());// 收货人手机号
            tborder.setReceiver(order.getReceiver());// 收货人
            tborder.setSourceType(order.getSourceType());// 订单来源
            tborder.setSellerId(cart.getSellerId());// 商家ID
            double money = 0;//该订单的总金额（b2b2c模式，一个商家对应一个订单，一个订单对应多个订单明细）
            // 循环购物车明细，获取orderItem添加一些属性，开始生成订单详情表，并统计该订单的总金额
            for (OrderItem orderItem : cart.getOrderItemList()) {
                for (String itemId: order.getItemIdes()) {
                    if (itemId.equals(orderItem.getItemId().toString())){
                        orderItem.setId(idWorker.nextId());// 添加 生成订单明细id
                        orderItem.setOrderId(orderId);// 添加 订单明细表中设置所属订单ID（一对多）
                        orderItem.setSellerId(cart.getSellerId());// 添加 商家id
                        money += Double.parseDouble(orderItem.getTotalFee());// 金额累加（存到订单表tb_order中）

                        //1.库存数量递减
                        HashMap<String, String> map = new HashMap<>();
                        map.put(orderItem.getItemId()+"",orderItem.getNum()+"");
                        System.out.println(map);
                        itemFeign.decrCount(map);

                        //2.保存订单明细到数据库中
                        orderItemMapper.insert(orderItem);

                        //3.向延时队列发消息
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        System.out.println("订单创建时间："+format.format(new Date()));

                        //1.延时的队列  2.发送的消息  3.延时队列设置
                        rabbitTemplate.convertAndSend("orderDelayQueue",(Object) "jkl666", new MessagePostProcessor() {
                            @Override
                            public Message postProcessMessage(Message message) throws AmqpException {
                                //设置延时读取
                                message.getMessageProperties().setExpiration("10000");
                                return message;
                            }
                        });
                    }
                }

            }
            //设置订单总金额
            tborder.setPayment(money+"");
            //4.保存订单到数据库
            orderMapper.insert(tborder);
            //5.增加用户积分(没做出来，feign报调用出错)
            //userFeign.addPoints(1);
        }
        //增加积分，调用用户微服务的userFeign 增加积分
        // userFeign.addPoints(10);
        //5.删除该用户购物车中已下单的数据（因技术不行，选择全部删除）
        redisTemplate.boundHashOps("cartList").delete(order.getUserId());

    }


    /**
     * Order条件+分页查询
     * @param order 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageResult<Order> findPage(Order order, int page, int size){
         Page<Order> mypage = new Page<>(page, size);
        QueryWrapper<Order> queryWrapper = this.createQueryWrapper(order);
        IPage<Order> iPage = this.page(mypage, queryWrapper);
        return new PageResult<Order>(iPage.getTotal(),iPage.getRecords());
    }

    /**
     * Order分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResult<Order> findPage(int page, int size){
        Page<Order> mypage = new Page<>(page, size);
        IPage<Order> iPage = this.page(mypage, new QueryWrapper<Order>());

        return new PageResult<Order>(iPage.getTotal(),iPage.getRecords());
    }

    /**
     * Order条件查询
     * @param order
     * @return
     */
    @Override
    public List<Order> findList(Order order){
        //构建查询条件
        QueryWrapper<Order> queryWrapper = this.createQueryWrapper(order);
        //根据构建的条件查询数据
        return this.list(queryWrapper);
    }


    /**
     * Order构建查询对象
     * @param order
     * @return
     */
    public QueryWrapper<Order> createQueryWrapper(Order order){
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        if(order!=null){
            // 订单id
            if(!StringUtils.isEmpty(order.getOrderId())){
                 queryWrapper.eq("order_id",order.getOrderId());
            }
            // 实付金额。精确到2位小数;单位:元。如:200.07，表示:200元7分
            if(!StringUtils.isEmpty(order.getPayment())){
                 queryWrapper.eq("payment",order.getPayment());
            }
            // 支付类型，1、在线支付，2、货到付款
            if(!StringUtils.isEmpty(order.getPaymentType())){
                 queryWrapper.eq("payment_type",order.getPaymentType());
            }
            // 邮费。精确到2位小数;单位:元。如:200.07，表示:200元7分
            if(!StringUtils.isEmpty(order.getPostFee())){
                 queryWrapper.eq("post_fee",order.getPostFee());
            }
            // 状态：1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭,7、待评价
            if(!StringUtils.isEmpty(order.getStatus())){
                 queryWrapper.eq("status",order.getStatus());
            }
            // 订单创建时间
            if(!StringUtils.isEmpty(order.getCreateTime())){
                 queryWrapper.eq("create_time",order.getCreateTime());
            }
            // 订单更新时间
            if(!StringUtils.isEmpty(order.getUpdateTime())){
                 queryWrapper.eq("update_time",order.getUpdateTime());
            }
            // 付款时间
            if(!StringUtils.isEmpty(order.getPaymentTime())){
                 queryWrapper.eq("payment_time",order.getPaymentTime());
            }
            // 发货时间
            if(!StringUtils.isEmpty(order.getConsignTime())){
                 queryWrapper.eq("consign_time",order.getConsignTime());
            }
            // 交易完成时间
            if(!StringUtils.isEmpty(order.getEndTime())){
                 queryWrapper.eq("end_time",order.getEndTime());
            }
            // 交易关闭时间
            if(!StringUtils.isEmpty(order.getCloseTime())){
                 queryWrapper.eq("close_time",order.getCloseTime());
            }
            // 物流名称
            if(!StringUtils.isEmpty(order.getShippingName())){
                 queryWrapper.eq("shipping_name",order.getShippingName());
            }
            // 物流单号
            if(!StringUtils.isEmpty(order.getShippingCode())){
                 queryWrapper.eq("shipping_code",order.getShippingCode());
            }
            // 用户id
            if(!StringUtils.isEmpty(order.getUserId())){
                 queryWrapper.eq("user_id",order.getUserId());
            }
            // 买家留言
            if(!StringUtils.isEmpty(order.getBuyerMessage())){
                 queryWrapper.eq("buyer_message",order.getBuyerMessage());
            }
            // 买家昵称
            if(!StringUtils.isEmpty(order.getBuyerNick())){
                 queryWrapper.eq("buyer_nick",order.getBuyerNick());
            }
            // 买家是否已经评价
            if(!StringUtils.isEmpty(order.getBuyerRate())){
                 queryWrapper.eq("buyer_rate",order.getBuyerRate());
            }
            // 收货人地区名称(省，市，县)街道
            if(!StringUtils.isEmpty(order.getReceiverAreaName())){
                 queryWrapper.eq("receiver_area_name",order.getReceiverAreaName());
            }
            // 收货人手机
            if(!StringUtils.isEmpty(order.getReceiverMobile())){
                 queryWrapper.eq("receiver_mobile",order.getReceiverMobile());
            }
            // 收货人邮编
            if(!StringUtils.isEmpty(order.getReceiverZipCode())){
                 queryWrapper.eq("receiver_zip_code",order.getReceiverZipCode());
            }
            // 收货人
            if(!StringUtils.isEmpty(order.getReceiver())){
                 queryWrapper.eq("receiver",order.getReceiver());
            }
            // 过期时间，定期清理
            if(!StringUtils.isEmpty(order.getExpire())){
                 queryWrapper.eq("expire",order.getExpire());
            }
            // 发票类型(普通发票，电子发票，增值税发票)
            if(!StringUtils.isEmpty(order.getInvoiceType())){
                 queryWrapper.eq("invoice_type",order.getInvoiceType());
            }
            // 订单来源：1:app端，2：pc端，3：M端，4：微信端，5：手机qq端
            if(!StringUtils.isEmpty(order.getSourceType())){
                 queryWrapper.eq("source_type",order.getSourceType());
            }
            // 商家ID
            if(!StringUtils.isEmpty(order.getSellerId())){
                 queryWrapper.eq("seller_id",order.getSellerId());
            }
        }
        return queryWrapper;
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(Long id){
        this.removeById(id);
    }

    /**
     * 修改Order
     * @param order
     */
    @Override
    public void update(Order order){
        this.updateById(order);
    }


    /**
     * 根据ID查询Order
     * @param id
     * @return
     */
    @Override
    public Order findById(Long id){
        return  this.getById(id);
    }

    /**
     * 查询Order全部数据
     * @return
     */
    @Override
    public List<Order> findAll() {
        return this.list(new QueryWrapper<Order>());
    }
}
