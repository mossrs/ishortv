package com.mossflower.ishortv_common.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;
import java.util.Map;

/**
 * @author z's'b
 */
public class JwtUtil {

    /**
     * 获取token
     *
     * @param payload 自定义载荷
     * @param expire  过期时间
     * @return token
     */
    public static String getToken(Map<String, String> payload, Long expire, String sign) {
        // 获取JWT构造器
        JWTCreator.Builder builder = JWT.create();
        // 设置自定义payload载荷
        payload.forEach(builder::withClaim);
        // 设置签发时间
        builder.withIssuedAt(new Date());
        // 设置过期时间
        builder.withExpiresAt(new Date(System.currentTimeMillis() + expire));
        // 设置签名算法后得到token并返回
        return builder.sign(Algorithm.HMAC256(sign));
    }


    /**
     * 验证token
     * 如果有任何验证异常，此处都会抛出异常
     */
    public static DecodedJWT verifyToken(String token, String sign) {
        return JWT.require(Algorithm.HMAC256(sign)).build().verify(token);
    }


}
