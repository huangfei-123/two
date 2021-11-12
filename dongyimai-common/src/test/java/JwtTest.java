import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;

public class JwtTest {
    /****
     *创建Jwt令牌
     */
    @Test
    public void testCreateJwt(){
        JwtBuilder builder= Jwts.builder()
                .setId("888")//设置唯一编号
                .setSubject("小白")//设置主题可以是JSON数据
                .setIssuedAt(new Date())//设置签发日期
                .setExpiration(new Date())//设置令牌的过期时间（此处是一创建就过期）
                .signWith(SignatureAlgorithm.HS256,"ujiuye");//设置签名使用HS256算法，并设置SecretKey(字符串)
        //构建并返回一个字符串
        System.out.println( builder.compact());
    }

    /***
     *解析Jwt令牌数据
     */
    @Test
    public void testParseJwt(){
        String 	compactJwt="eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI4ODgiLCJzdWIiOiLlsI_nmb0iLCJpYXQiOjE2Mjk3MDY1NTgsImFkZHJlc3MiOiLljJfkuqwiLCJuYW1lIjoi5Lit5YWs5pyJ5bCx5LiaIn0.L4inU74aGPS0CUdS_GsU6c-qZMOOWRcCPVOz4zUiCf8";
        Claims claims = Jwts.parser().
                setSigningKey("ujiuye").
                parseClaimsJws(compactJwt).
                getBody();
        System.out.println(claims);
    }

    /****
     *创建Jwt令牌
     */
    @Test
    public void testCreateJwt02(){
        JwtBuilder builder= Jwts.builder()
                .setId("888")//设置唯一编号
                .setSubject("小白")//设置主题可以是JSON数据
                .setIssuedAt(new Date())//设置签发日期
                .signWith(SignatureAlgorithm.HS256,"ujiuye");//设置签名使用HS256算法，并设置SecretKey(字符串)
        //自定义数据
        HashMap<String, Object> map = new HashMap<>();
        map.put("name","中公有就业");
        map.put("address","北京");
        builder.addClaims(map);
        //构建并返回一个字符串
        System.out.println( builder.compact());
    }
}
