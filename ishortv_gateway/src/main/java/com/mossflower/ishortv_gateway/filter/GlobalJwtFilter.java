package com.mossflower.ishortv_gateway.filter;


import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.mossflower.ishortv_common.constant.JwtConstant;
import com.mossflower.ishortv_common.util.RedisUtil;
import com.mossflower.ishortv_common.util.JwtUtil;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 将登录用户的JWT转化成用户信息的全局过滤器
 * 过滤器级别的权限控制 使用全局异常处理器拦截不到（只能拦截进入servlet容器 通过aop拦截）
 *
 * @author z's'b
 */
@Component
public class GlobalJwtFilter implements GlobalFilter, Ordered {
    @Resource
    private RedisUtil redisUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath().replace(JwtConstant.IGNORE_URL_PREFIX, "");
        ServerHttpResponse response = exchange.getResponse();
        PathMatcher pathMatcher = new AntPathMatcher();
        List<String> ignoreUrls = JwtConstant.IGNORE_URLS;
        for (String ignoreUrl : ignoreUrls) {
            if (pathMatcher.match(ignoreUrl, path)) {
                return chain.filter(exchange);
            }
        }

        // 获取全部cookie
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
//        String refreshToken = cookies.get(JwtConstant.COOKIE_REFRESH_TOKEN).get(0).getValue();
//        // 如果没有refreshToken 说明前端没传过来 直接返回登录
//        if (StrUtil.isEmpty(refreshToken)) {
//            response.setStatusCode(HttpStatus.UNAUTHORIZED);
//            return response.setComplete();
//        }
//        try {
//            DecodedJWT decodedJwt = JwtUtil.verifyToken(refreshToken, JwtConstant.REFRESH_TOKEN_SECRET);
//            String userId = decodedJwt.getClaims().get(JwtConstant.USER_ID).asString();
//            // 获取redis中的refreshToken 不存在说明已经过期 或错误 强制重新登录
//            Object redisRefreshToken = redisUtil.getStringValue("refresh_token_" + userId);
//            if (redisRefreshToken == null) {
//                response.setStatusCode(HttpStatus.UNAUTHORIZED);
//                return response.setComplete();
//            }
//            // 如果redis中的refreshToken和前端传过来的refreshToken不一致 直接强制重新登录 说明前端传的refreshToken是伪造的
//            if (!Objects.equals(refreshToken, redisRefreshToken.toString())) {
//                response.setStatusCode(HttpStatus.UNAUTHORIZED);
//                return response.setComplete();
//            }
//        } catch (Exception e) {
//            response.setStatusCode(HttpStatus.UNAUTHORIZED);
//            return response.setComplete();
//        }
        // 获取accessToken 如果没有accessToken 说明前端没传过来 直接返回登录
        try {
            String accessToken = cookies.get(JwtConstant.COOKIE_ACCESS_TOKEN).get(0).getValue();
//            String accessToken = response.getHeaders().getFirst(JwtConstant.COOKIE_ACCESS_TOKEN);
            if (StrUtil.isEmpty(accessToken)) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
            // 先验证accessToken是否正确 如果不正确 直接返回登录 过期返回409 请求刷新token
            DecodedJWT decodedJwt = JwtUtil.verifyToken(accessToken, JwtConstant.ACCESS_TOKEN_SECRET);
            String userId = decodedJwt.getClaims().get(JwtConstant.USER_ID).asString();
            // 获取redis中的accessToken 不存在说明已经过期或者是错误的 强制重新登录
            Object redisAccessToken = redisUtil.getStringValue("access_token_" + userId);
            if (redisAccessToken == null) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
            // 如果redis中的accessToken和前端传过来的accessToken不一致 直接强制重新登录 说明前端传的accessToken是伪造的
            if (!Objects.equals(accessToken, redisAccessToken.toString())) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
            String isAdmin = decodedJwt.getClaims().get(JwtConstant.IS_ADMIN).asString();
            if (path.contains(JwtConstant.ADMIN_URL_PREFIX)) {
                if (!Boolean.parseBoolean(isAdmin)) {
                    response.setStatusCode(HttpStatus.FORBIDDEN);
                    return response.setComplete();
                }
            }
            request = exchange.getRequest().mutate().header(JwtConstant.USER_ID, userId).build();
            exchange = exchange.mutate().request(request).build();
            return chain.filter(exchange);
        } catch (TokenExpiredException e) {
//            response.setStatusCode(HttpStatus.CONFLICT);
//            return response.setComplete();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        } catch (Exception e) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
