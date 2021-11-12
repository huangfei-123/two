package com.offcn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.offcn.dao.SeckillGoodsMapper;
import com.offcn.dao.SeckillOrderMapper;
import com.offcn.entity.PageResult;
import com.offcn.entity.SeckillStatus;
import com.offcn.pojo.SeckillGoods;
import com.offcn.pojo.SeckillOrder;
import com.offcn.service.SeckillOrderService;
import com.offcn.task.MultiThreadingCreateOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/****
 * @Author:ujiuye
 * @Description:SeckillOrder业务层接口实现类
 * @Date 2021/2/1 14:19
 *****/
@Service
public class SeckillOrderServiceImpl extends ServiceImpl<SeckillOrderMapper, SeckillOrder> implements SeckillOrderService {

    @Autowired
    private MultiThreadingCreateOrder multiThreadingCreateOrder;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;


    /***
     * 删除订单redis中的订单，回滚库存
     * @param username 用户名
     */
    @Override
    public void deleteOrder(String username) {

        //1.删除订单
        redisTemplate.boundHashOps("SeckillOrder").delete(username);
        //查询用户的排队状态
        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundHashOps("UserQueueStatus").get(username);
        //2.删除排队信息
        clearUserQueue(username);
        //3.库存回滚
        //获取商品数据
        SeckillGoods goods = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_" + seckillStatus.getTime()).get(seckillStatus.getGoodsId());
        //如果商品为空（可能是最后一个抢的）
        if (goods==null){
            //数据库中查询（此时数据库中的数量一定为零）
            goods = seckillGoodsMapper.selectById(seckillStatus.getGoodsId());
            //更新数据库库存
            goods.setStockCount(1);
            seckillGoodsMapper.updateById(goods);
        }else {
            //增加一个
            goods.setStockCount(goods.getStockCount()+1);
        }
        //同步到redis缓存
        redisTemplate.boundHashOps("SeckillGoods_" + seckillStatus.getTime()).put(goods.getId(),goods);

        //将该商品加入计数队列中
        redisTemplate.boundListOps("SeckillGoodsCountList_"+goods.getId()).leftPush(goods.getId());
    }

    /***
     * 更新订单状态
     * @param endtime 支付完成时间
     * @param transaction_id  交易流水号
     * @param username  用户名
     */
    @Override
    public void updatePayStatus(String endtime, String transaction_id, String username) {
        //查询订单
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("SeckillOrder").get(username);

        if (seckillOrder!=null){
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
            Date payTime = null;
            try {
                payTime = format.parse(endtime);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //修改订单状态信息
            seckillOrder.setStatus("1");//1已支付
            seckillOrder.setPayTime(payTime);
            seckillOrder.setTransactionId(transaction_id);

            //将已支付、支付失败的订单都存到mysql数据库，注意待支付的订单存到redis的
            this.add(seckillOrder);

            //清除redis中该用户的抢单信息
            redisTemplate.boundHashOps("SeckillOrder").delete(username);

            //删除用户的排队信息
            clearUserQueue(username);
        }

    }

    /**
     * 清理用户排队、抢单信息
     */
    public void clearUserQueue(String username){
        //删除用户排队信息
        redisTemplate.boundHashOps("UserQueueCount").delete(username);
        //删除用户排队状态查询信息
        redisTemplate.boundHashOps("UserQueueStatus").delete(username);
    }

    /***
     * 抢单状态查询
     * @param username
     * @return
     */
    @Override
    public SeckillStatus queryStatus(String username) {
        return (SeckillStatus) redisTemplate.boundHashOps("UserQueueStatus").get(username);
    }

    /**
     * 秒杀下单-排队
     * @param id:商品ID
     * @param time:商品秒杀开始时间
     * @param username:用户登录名
     * @return
     */
    @Override
    public Boolean add(Long id, String time, String username) {
        /**
         * 高级 1.（创建用户排队队列）解决一个用户多次排队  记录用户排队次数
         * 用户名为键 自增值为1,第一次登录为1 第二次登录抢购为2  第三次为3 ....
         * 有可能会清理
         */
        Long userQueueCount = redisTemplate.boundHashOps("UserQueueCount").increment(username, 1);

        if(userQueueCount>1){
            //该用户之前已经有过排队
            throw new RuntimeException("100");
        }

        //创建排队对象
        SeckillStatus seckillStatus = new SeckillStatus(username, new Date(), 1, id, time);

        // 用户抢单排队 实现先来后到  将秒杀抢单信息存入到Redis中,这里采用List方式存储,List本身是一个队列
        redisTemplate.boundListOps("SeckillOrderQueue").leftPush(seckillStatus);

        //用于用户抢单状态查询 将抢单状态存入到Redis中  1 正在排队  2 抢单成功
        //有可能会清理
        redisTemplate.boundHashOps("UserQueueStatus").put(username,seckillStatus);

        //多线程抢单
        multiThreadingCreateOrder.createOrder();
        return true;
    }

    /**
     * SeckillOrder条件+分页查询
     * @param seckillOrder 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageResult<SeckillOrder> findPage(SeckillOrder seckillOrder, int page, int size){
         Page<SeckillOrder> mypage = new Page<>(page, size);
        QueryWrapper<SeckillOrder> queryWrapper = this.createQueryWrapper(seckillOrder);
        IPage<SeckillOrder> iPage = this.page(mypage, queryWrapper);
        return new PageResult<SeckillOrder>(iPage.getTotal(),iPage.getRecords());
    }

    /**
     * SeckillOrder分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResult<SeckillOrder> findPage(int page, int size){
        Page<SeckillOrder> mypage = new Page<>(page, size);
        IPage<SeckillOrder> iPage = this.page(mypage, new QueryWrapper<SeckillOrder>());

        return new PageResult<SeckillOrder>(iPage.getTotal(),iPage.getRecords());
    }

    /**
     * SeckillOrder条件查询
     * @param seckillOrder
     * @return
     */
    @Override
    public List<SeckillOrder> findList(SeckillOrder seckillOrder){
        //构建查询条件
        QueryWrapper<SeckillOrder> queryWrapper = this.createQueryWrapper(seckillOrder);
        //根据构建的条件查询数据
        return this.list(queryWrapper);
    }


    /**
     * SeckillOrder构建查询对象
     * @param seckillOrder
     * @return
     */
    public QueryWrapper<SeckillOrder> createQueryWrapper(SeckillOrder seckillOrder){
        QueryWrapper<SeckillOrder> queryWrapper = new QueryWrapper<>();
        if(seckillOrder!=null){
            // 主键
            if(!StringUtils.isEmpty(seckillOrder.getId())){
                 queryWrapper.eq("id",seckillOrder.getId());
            }
            // 秒杀商品ID
            if(!StringUtils.isEmpty(seckillOrder.getSeckillId())){
                 queryWrapper.eq("seckill_id",seckillOrder.getSeckillId());
            }
            // 支付金额
            if(!StringUtils.isEmpty(seckillOrder.getMoney())){
                 queryWrapper.eq("money",seckillOrder.getMoney());
            }
            // 用户
            if(!StringUtils.isEmpty(seckillOrder.getUserId())){
                 queryWrapper.eq("user_id",seckillOrder.getUserId());
            }
            // 商家
            if(!StringUtils.isEmpty(seckillOrder.getSellerId())){
                 queryWrapper.eq("seller_id",seckillOrder.getSellerId());
            }
            // 创建时间
            if(!StringUtils.isEmpty(seckillOrder.getCreateTime())){
                 queryWrapper.eq("create_time",seckillOrder.getCreateTime());
            }
            // 支付时间
            if(!StringUtils.isEmpty(seckillOrder.getPayTime())){
                 queryWrapper.eq("pay_time",seckillOrder.getPayTime());
            }
            // 状态
            if(!StringUtils.isEmpty(seckillOrder.getStatus())){
                 queryWrapper.eq("status",seckillOrder.getStatus());
            }
            // 收货人地址
            if(!StringUtils.isEmpty(seckillOrder.getReceiverAddress())){
                 queryWrapper.eq("receiver_address",seckillOrder.getReceiverAddress());
            }
            // 收货人电话
            if(!StringUtils.isEmpty(seckillOrder.getReceiverMobile())){
                 queryWrapper.eq("receiver_mobile",seckillOrder.getReceiverMobile());
            }
            // 收货人
            if(!StringUtils.isEmpty(seckillOrder.getReceiver())){
                 queryWrapper.eq("receiver",seckillOrder.getReceiver());
            }
            // 交易流水
            if(!StringUtils.isEmpty(seckillOrder.getTransactionId())){
                 queryWrapper.eq("transaction_id",seckillOrder.getTransactionId());
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
     * 修改SeckillOrder
     * @param seckillOrder
     */
    @Override
    public void update(SeckillOrder seckillOrder){
        this.updateById(seckillOrder);
    }

    /**
     * 增加SeckillOrder
     * @param seckillOrder
     */
    @Override
    public void add(SeckillOrder seckillOrder){
        this.save(seckillOrder);
    }

    /**
     * 根据ID查询SeckillOrder
     * @param id
     * @return
     */
    @Override
    public SeckillOrder findById(Long id){
        return  this.getById(id);
    }

    /**
     * 查询SeckillOrder全部数据
     * @return
     */
    @Override
    public List<SeckillOrder> findAll() {
        return this.list(new QueryWrapper<SeckillOrder>());
    }
}
