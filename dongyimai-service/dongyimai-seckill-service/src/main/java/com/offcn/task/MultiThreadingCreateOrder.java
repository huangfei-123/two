package com.offcn.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.offcn.dao.SeckillGoodsMapper;
import com.offcn.entity.SeckillStatus;
import com.offcn.pojo.SeckillGoods;
import com.offcn.pojo.SeckillOrder;
import com.offcn.util.IdWorker;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class MultiThreadingCreateOrder {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    /***
     * 多线程下单操作
     */
    @Async
    public void createOrder(){

        try {

            System.out.println("准备睡会再下单！");
            Thread.sleep(10000);

            //从redis的排队队列中获取排队信息 如果能获取就下单
            SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundListOps("SeckillOrderQueue").rightPop();

            if (seckillStatus==null){
                return;
            }

            //时间区间
            String time = seckillStatus.getTime();
            //用户登录名
            String username=seckillStatus.getUsername();
            //用户抢购商品
            Long id = seckillStatus.getGoodsId();

            //高级 2. 解决超卖 先到SeckillGoodsCountList_队列获取一个信息，能获取就说明还有该商品的库存
            //如果不能获取就说明没有库存了 需要删除该用户排队队列中的信息（已经读了，自动消除），1自增队列中的信息 2查询抢单信息的队列  以便其再次排队抢购其他商品
            //goodsid 存到队列中的一个标识  这里是商品的id
            Object goodsid = redisTemplate.boundListOps("SeckillGoodsCountList_" + seckillStatus.getGoodsId()).rightPop();

            if (goodsid==null){
                //清空排队信息
                clearUserQueue(username);
                return ;
            }

            //获取具体的某一个商品数据
            SeckillGoods goods = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_" + time).get(id);
            //如果没有库存，则直接抛出异常
            if(goods==null || goods.getStockCount()<=0){
                throw new RuntimeException("已售罄!");
            }
            //如果有库存，则创建秒杀商品订单
            SeckillOrder seckillOrder = new SeckillOrder();
            seckillOrder.setId(idWorker.nextId());//生成一个秒杀订单的id
            seckillOrder.setSeckillId(id);//设置秒杀商品的id
            seckillOrder.setMoney(goods.getCostPrice());//秒杀的价格
            seckillOrder.setUserId(username);//抢购用户
            seckillOrder.setCreateTime(new Date());//秒杀订单的创建时间
            seckillOrder.setStatus("0");//订单的支付状态 0 未支付 1 已支付 2 支付失败

            //下单 将秒杀订单存入到Redis中（只有支付了或支付失败才往mysql中存储）
            redisTemplate.boundHashOps("SeckillOrder").put(username,seckillOrder);

            //查询到的商品库存减少（我觉得这操作没什么用）
            goods.setStockCount(goods.getStockCount()-1);

            Thread.sleep(10000);//在这里也会出现高并发，线程不安全，上面的库存在redis中还没减少，多个线程拿到的数据还是同一个，所以始终只减少一个商品

            System.out.println(Thread.currentThread().getId() + "操作后剩余库存=" + goods.getStockCount());

//            //判断当前商品是否还有库存
//            if(goods.getStockCount()<=0){
            //高级 3.解决数据库数据同步不精确
            //获取该商品的标记队列SeckillGoodsCountList_是否还有数据（商品），如果没有说明售完了
            Long size = redisTemplate.boundListOps("SeckillGoodsCountList_" + seckillStatus.getGoodsId()).size();
            if(size<=0){

                goods.setStockCount(size.intValue());
                //将商品数据同步到MySQL中（此时的库存量已经为零）
                QueryWrapper<SeckillGoods> seckillGoodsQueryWrapper = new QueryWrapper<>();
                seckillGoodsQueryWrapper.eq("id",goods.getId());
                int update = seckillGoodsMapper.update(goods, seckillGoodsQueryWrapper);
                //同时清空Redis缓存中该商品
                redisTemplate.boundHashOps("SeckillGoods_" + time).delete(id);
            }else{
                //如果有库存，则直数据重置到Reids中，重置的数据不准确，上面有高并发产生
                redisTemplate.boundHashOps("SeckillGoods_" + time).put(id,goods);
            }

            //修改redis中用户的状态
            //抢单成功，更新抢单状态,排队->等待支付
            seckillStatus.setStatus(2);//去查看用户的清单状态，会发现由状态1变为2
            seckillStatus.setOrderId(seckillOrder.getId());
            seckillStatus.setMoney(seckillOrder.getMoney().floatValue());
            redisTemplate.boundHashOps("UserQueueStatus").put(username,seckillStatus);//更新

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println("下单时间为："+format.format(new Date()));

            //发送消息给延时队列 参数1 延时队列名称， 参数2 发送的消息， 参数3 延时时间设置
            // Map map= JSON.parseObject(JSON.toJSONString(seckillStatus), Map.class);
            rabbitTemplate.convertAndSend("delaySeckillQueue", seckillStatus , new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    message.getMessageProperties().setExpiration("10000");
                    return message;
                }
            });

        } catch (InterruptedException e) {
            e.printStackTrace();
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
}
