package com.offcn.controller;

import com.offcn.entity.Result;
import com.offcn.entity.StatusCode;
import com.offcn.pojo.Cart;
import com.offcn.service.CartService;
import com.offcn.util.TokenDecode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping(value = "/cart")
public class CartController {
    @Autowired
    private CartService cartService;


    /**
     * 购物车列表
     *
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        String username = userInfo.get("username");
        return cartService.findCartListFromRedis(username);//从redis中提取
    }


    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId, Integer num) {
       // String username = "huangfei"; //购买人的姓名
        //用工具类获取用户信息！，来自令牌中，用户的登录名
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        String username = userInfo.get("username");
        try {
            List<Cart> cartList = findCartList();//获取username的购物车
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);//向购物车里添加商品
            cartService.saveCartListToRedis(username, cartList);//将该购物车重新以该名字为键，以购物车列表为值存入redis
            return new Result(true, StatusCode.OK, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, StatusCode.ERROR, "添加失败");
        }

    }
}
