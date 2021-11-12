package com.offcn.filter;


import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {
    //令牌头名字
    private static final String AUTHORIZE_TOKEN = "Authorization";

    //用户登陆地址
    private static final String USER_LOGIN_URL = "http://localhost:9100/userAuth/login";

    /***
     *全局过滤器
     *@param exchange
     *@param chain
     *@return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain){
        //获取Request、Response对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        //如果是登录、注册，等一些不需要权限的，就放行
        String uri = request.getPath().toString();
        if (URLFilter.letGo(uri)) {
            return chain.filter(exchange);
        }

        //1.获取头文件中的令牌信息
        String tokent = request.getHeaders().getFirst(AUTHORIZE_TOKEN);

        //2.如果头文件中没有，则从请求参数中获取
        if (StringUtils.isEmpty(tokent)){
            tokent = request.getQueryParams().getFirst(AUTHORIZE_TOKEN);
        }

        //3.从Cookie中获取令牌
        HttpCookie cookie = request.getCookies().getFirst("Authorization");
        if (cookie!=null){
            tokent= cookie.getValue();
        }

        //如果为空，则输出错误代码
        if (StringUtils.isEmpty(tokent)){
            //设置方法不允许被访问，405错误代码
            response.setStatusCode(HttpStatus.METHOD_NOT_ALLOWED);
            return response.setComplete();

        }

        //解析令牌数据
        try {
            //将令牌存储到头文件中
            request.mutate().header(AUTHORIZE_TOKEN,"Bearer "+tokent);
        } catch (Exception e){
            e.printStackTrace();
            //解析失败，响应401错误
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        //放行
        return chain.filter(exchange);
    }

    /***
     *过滤器执行顺序
     *@return
     */
    @Override
    public int getOrder(){
        return 0;
    }


}
