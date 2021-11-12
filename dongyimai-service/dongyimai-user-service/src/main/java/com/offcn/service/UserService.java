package com.offcn.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.offcn.pojo.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService extends IService<User> {
    /**
     * 新增用户
     * @param user
     */
    public void add(User user);

    /**
     * 生成短信验证码
     * @return
     */
    public void createSmsCode(String phone);

    /**
     * 判断短信验证码是否存在
     * @param phone
     * @return
     */
    public boolean  checkSmsCode(String phone,String code);

    /**
     * 根据用户名查询用户
     * @param username
     * @return
     */
    User findByUsername(String username);

    /***
     * 查询所有User
     * @return
     */
    List<User> findAll();

    /***
     * 添加用户积分
     * @param username
     * @param points
     * @return
     */
    int addUserPoints(String username,Integer points);

}
