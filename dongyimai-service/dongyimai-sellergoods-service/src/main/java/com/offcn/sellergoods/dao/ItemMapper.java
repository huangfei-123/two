package com.offcn.sellergoods.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.offcn.sellergoods.pojo.Item;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/****
 * @Author:ujiuye
 * @Description:Item的Dao
 * @Date 2021/2/1 14:19
 *****/
public interface ItemMapper extends BaseMapper<Item> {
    /**
     * 递减库存  mysql自带行级锁 保证线程安全
     * @param
     * @return
     */
    @Update("UPDATE tb_item SET num=num-#{num} WHERE id=#{itemId} AND num>=#{num}")
    int decrCount(@Param(value = "itemId") String itemId, @Param(value = "num") String num);
}
