package com.offcn.controller;

import com.offcn.entity.Result;
import com.offcn.entity.StatusCode;
import com.offcn.pojo.Address;
import com.offcn.service.AddressService;
import com.offcn.util.TokenDecode;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/****
 * @Author:ujiuye
 * @Description:
 * @Date 2021/2/1 14:19
 *****/
@Api(tags = "AddressController")
@RestController
@RequestMapping("/address")
@CrossOrigin
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private TokenDecode tokenDecode;
    /****
     * 根据用户名查询用户地址，用户收件地址
     */
    @GetMapping(value = "/user/list")
    public Result<List<Address>> findListByUserId(){
        //获取用户登录信息
        Map<String, String> userMap = tokenDecode.getUserInfo();
        String userId = userMap.get("username");
        //查询用户收件地址
        List<Address> addressList = addressService.findListByUserId(userId);
        return new Result(true, StatusCode.OK,"查询成功！",addressList);
    }

}
