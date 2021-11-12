package com.offcn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.offcn.dao.UserMapper;
import com.offcn.pojo.User;
import com.offcn.service.UserService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserMapper userMapper;

    /***
     * 添加用户积分
     * @param username
     * @param points
     * @return
     */
    @Override
    public int addUserPoints(String username, Integer points) {
        return userMapper.addUserPoints(username,points);
    }

    @Override
    public void add(User user) {
        user.setCreated(new Date());//创建日期
        user.setUpdated(new Date());//修改日期
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = encoder.encode(user.getPassword());//对密码加密
        user.setPassword(password);
        this.save(user);
    }

    /**
     * 生成短信验证码
     *
     * @param phone
     * @return
     */
    @Override
    public void createSmsCode(String phone) {
        //生成6位随机数
        String code =  (long) (Math.random()*1000000)+"";
        System.out.println("验证码："+code);
        //2.将生成的验证码以手机号为键，以验证码为值存到redis
        //存入缓存
        redisTemplate.boundHashOps("smscode").put(phone, code);
        //发送到RabbitMQ	....
        HashMap<Object, Object> map = new HashMap<>();
        map.put("mobile",phone);
        map.put("code",code);
        //3.将手机号和验证码这俩消息以map方式存到指定的消息队列
        rabbitTemplate.convertAndSend("dongyimai.sms.queue",map);


    }

    /**
     * 判断验证码是否正确
     */
    public boolean  checkSmsCode(String phone,String code){
        //得到缓存中存储的验证码
        String sysCode = (String) redisTemplate.boundHashOps("smscode").get(phone);
        if(sysCode==null){
            return false;
        }
        if(!sysCode.equals(code)){
            return false;
        }
        return true;
    }

    /**
     * 根据用户名查询用户
     * @param username
     * @return
     */
    @Override
    public User findByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",username);
        try {
            return this.list(queryWrapper).get(0);
        } catch (Exception e) {
            return new User();
        }
    }

    /**
     * 查询User全部数据
     * @return
     */
    @Override
    public List<User> findAll() {
        return this.list(new QueryWrapper<User>());
    }

}
