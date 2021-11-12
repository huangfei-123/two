package com.offcn.feign;

import com.offcn.entity.Result;
import com.offcn.pojo.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="user")
@RequestMapping("/user")
public interface UserFeign {
    /***
     * 根据username查询用户信息
     * @param username
     * @return
     */
    @GetMapping("/load/{username}")
    public Result<User> findByUsername(@PathVariable String username);

    /***
     * 添加用户积分
     * @param points
     * @return 在feign里面一定要加@RequestParam才行
     */
    @GetMapping(value = "/points/add")
    Result addPoints(@RequestParam(value = "points")Integer points);
}
