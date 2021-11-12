package com.offcn.controller;

import com.offcn.entity.Result;
import com.offcn.entity.StatusCode;
import com.offcn.pojo.User;
import com.offcn.service.UserService;
import com.offcn.util.PhoneFormatCheckUtils;
import com.offcn.util.TokenDecode;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private TokenDecode tokenDecode;

    /***
     * 增加用户积分
     * @param points:要添加的积分
     */
    @GetMapping(value = "/points/add")
    public Result addPoints(Integer points){
        //获取用户名
        Map<String, String> userMap = tokenDecode.getUserInfo();
        String username = userMap.get("username");

        //添加积分
        userService.addUserPoints(username,points);
        return new Result(true,StatusCode.OK,"添加积分成功！");
    }

    /**
     * 发送短信验证码
     * @param phone
     * @return
     */
    //1.前端填入信息（必须有手机号）后，击获取验证码，将手机号传入后端，
    // 判断手机号格式是否正确，如果正确则生成一个验证码
    @GetMapping("/sendCode")
    public Result sendCode(String phone){
        //判断手机号格式
        if(!PhoneFormatCheckUtils.isPhoneLegal(phone)){
            return new Result(false, StatusCode.ERROR,"手机号格式不正确");
        }
        try {
            userService.createSmsCode(phone);//生成验证码
            return new Result(true,StatusCode.OK, "验证码发送成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,StatusCode.ERROR, "验证码发送失败");
        }
    }

    /**
     * 增加（注册）
     * @param user
     * @return
     */
    @PostMapping("/add")
    public Result add(@RequestBody User user, String smscode){
        boolean checkSmsCode = userService.checkSmsCode(user.getPhone(), smscode);
        if(checkSmsCode==false){
            return new Result(false,StatusCode.ERROR ,"验证码输入错误！");
        }
        try {
            userService.add(user);
            return new Result(true,StatusCode.OK, "增加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,StatusCode.ERROR, "增加失败");
        }
    }

    /***
     *用户登录
     */
//    @GetMapping(value = "/login")
//    public Result login(String username, String password, HttpServletResponse response){
//        //查询用户信息
//        User user =  userService.findByUsername(username);
//        //注意，以前登录成功以后直接将user存入session中，在其他页面访问的时候先判断session里面是否有user用户。
//        // 从现在开始就是要jwt令牌来处理
//        if(user!=null && BCrypt.checkpw(password,user.getPassword())){
//            //设置令牌信息
//            Map<String,Object> info = new HashMap<String,Object>();
//            info.put("role","USER");
//            info.put("success","SUCCESS");
//            info.put("username",username);
//            //生成令牌
//            String jwt = JwtUtil.createJWT(UUID.randomUUID().toString(), JSON.toJSONString(info),null);
//            //将令牌信息存入Cookie中
//            Cookie cookie = new Cookie("Authorization123", jwt);
//            response.addCookie(cookie);
//            return new Result(true,StatusCode.OK,"登录成功！",jwt);
//        }
//        return  new Result(false,StatusCode.LOGINERROR,"账号或者密码错误！");
//    }

    @GetMapping({"/load/{username}","/{username}"})
    public Result<User> findByUsername(@PathVariable String username){
        //调用UserService实现根据主键查询User
        User user = userService.findByUsername(username);
        return new Result<User>(true,StatusCode.OK,"查询成功",user);
    }

    /***
     * 查询User全部数据
     * @return
     */
    @ApiOperation(value = "查询所有User",notes = "查询所User有方法详情",tags = {"UserController"})
    @GetMapping
    @PreAuthorize("hasAnyAuthority('admin')")
    public Result<List<User>> findAll(){
        //调用UserService实现查询所有User
        List<User> list = userService.findAll();
        return new Result<List<User>>(true, StatusCode.OK,"查询成功",list) ;
    }

    @GetMapping("/test")
    public String test(){
        return "123";
    }
}
