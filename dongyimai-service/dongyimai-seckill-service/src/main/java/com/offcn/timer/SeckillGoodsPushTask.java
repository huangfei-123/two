package com.offcn.timer;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.offcn.dao.SeckillGoodsMapper;
import com.offcn.pojo.SeckillGoods;
import com.offcn.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 定时器
 * 定时将秒杀商品存入redis缓存
 */
@Component
public class SeckillGoodsPushTask {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /****
     * 从第零秒开始，每过30秒执行一次
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void loadGoodsPushRedis(){


        //获得时间段集合
        List<Date> dateMenus = DateUtil.getDateMenus();
        //循环时间段集合
        for (Date startTime : dateMenus) {

            //提取开始时间，转换为年月日格式的字符串
            String extName = DateUtil.date2Str(startTime);//10-12
            //创建查询条件对象
            QueryWrapper<SeckillGoods> queryWrapper = new QueryWrapper<>();
            //设置查询条件 1)商品必须审核通过  status=1
            queryWrapper.eq("status","1");
            //2)库存大于0
            queryWrapper.gt("stock_count",0);
            //3)开始时间startTime<=活动开始时间（数据库）
            queryWrapper.ge("start_time",DateUtil.date2StrFull(startTime));
            //4)活动结束时间<开始时间+2小时
            queryWrapper.lt("end_time",DateUtil.date2StrFull(DateUtil.addDateHour(startTime,2)));
            //5)读取redis中存在的当天的秒杀商品
            Set keys = redisTemplate.boundHashOps("SeckillGoods_"+extName).keys();
            //判断keys不为空，就设置排除条件
            if(keys!=null&&keys.size()>0){
                //防止秒杀商品
                queryWrapper.notIn("id",keys);
            }
            //查询符合条件的商品数据库
            List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectList(queryWrapper);
            //遍历查询到数据集合,存储数据到redis
            for (SeckillGoods seckillGoods : seckillGoodsList) {
                redisTemplate.boundHashOps("SeckillGoods_"+extName).put(seckillGoods.getId(),seckillGoods);
                //高级 2.防止超卖  给每个商品做一个队列,用于记录该商品的个数 值没有实际意义  标记即可！
                redisTemplate.boundListOps("SeckillGoodsCountList_"+seckillGoods.getId()).leftPushAll(putAllIds(seckillGoods.getStockCount(),seckillGoods.getId()));
                //设置超时时间2小时
                redisTemplate.expireAt("SeckillGoods_"+extName,DateUtil.addDateHour(startTime,2));
            }

        }
    }

    //创建数组

    /**
     * @param num 商品库存量
     * @param id  商品id
     * @return
     */
    public Long[] putAllIds(Integer num,Long id){
        Long[] ids = new Long[num];
        for (int i = 0; i<ids.length ; i++) {
            ids[i] = id;
        }
        return ids;
    }
}
