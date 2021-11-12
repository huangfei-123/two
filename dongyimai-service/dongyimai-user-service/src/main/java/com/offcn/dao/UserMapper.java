package com.offcn.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.offcn.pojo.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;


/****
 * @Author:ujiuye
 * @Description:User的Dao
 * @Date 2021/2/1 14:19
 *****/
public interface UserMapper extends BaseMapper<User> {
    /***
     * 增加用户积分
     * @param username
     * @param points
     * @return
     */
    //这样写的目的是保证数据的原子性，sql语句在执行时只能一个线程执行，其他线程等待
    @Update("UPDATE tb_user SET points=points+#{points} WHERE  username=#{username}")
    int addUserPoints(@Param("username") String username, @Param("points") Integer points);
}
