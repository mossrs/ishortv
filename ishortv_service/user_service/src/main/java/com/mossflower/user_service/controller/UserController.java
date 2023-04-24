package com.mossflower.user_service.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.mossflower.ishortv_common.constant.JwtConstant;
import com.mossflower.ishortv_common.result.R;
import com.mossflower.ishortv_common.util.JwtUtil;
import com.mossflower.ishortv_common.util.RedisUtil;
import com.mossflower.user_service.entity.User;
import com.mossflower.user_service.service.UserService;
import com.mossflower.user_service.vo.ResUserInfoVo;
import com.mossflower.user_service.vo.TokenUserInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.SameSiteCookies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;


/**
 * @author z's'b
 */
@RestController
@Slf4j
@RequestMapping("/user")
@Validated
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private RedisUtil redisUtil;

    private void setCookie(HttpServletResponse response, String name, String value) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .sameSite(SameSiteCookies.NONE.getValue())
                .path("/")
                .domain("")
                .maxAge(Duration.ofDays(7))
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void removeCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(JwtConstant.COOKIE_ACCESS_TOKEN, "")
                .httpOnly(true)
                .secure(true)
                .sameSite(SameSiteCookies.NONE.getValue())
                .path("/")
                .domain("")
                .maxAge(Duration.ofDays(0))
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @PostMapping("/register")
    public R<Boolean> register(@RequestBody User user, HttpServletResponse response) {
        TokenUserInfoVo tokenUserInfoVo = userService.register(user);
        setCookie(response, JwtConstant.COOKIE_ACCESS_TOKEN, tokenUserInfoVo.getAccessToken());
//        setCookie(response, "refresh_token", tokenUserInfoVo.getRefreshToken());
//        return R.ok(tokenUserInfoVo.getAccessToken());
        return R.ok(true);
    }

    @PostMapping("/login")
    public R<Boolean> login(@RequestBody User user, HttpServletResponse response) {
        TokenUserInfoVo tokenUserInfoVo = userService.login(user);
        setCookie(response, JwtConstant.COOKIE_ACCESS_TOKEN, tokenUserInfoVo.getAccessToken());
//        setCookie(response, "refresh_token", tokenUserInfoVo.getRefreshToken());
//        return R.ok(tokenUserInfoVo.getAccessToken());
        return R.ok(true);
    }

    @PostMapping("/logout")
    public R<Boolean> logout(@RequestHeader(JwtConstant.USER_ID) String userId, HttpServletResponse response) {
//        response.setHeader(JwtConstant.COOKIE_ACCESS_TOKEN, "");
        removeCookie(response);
        return R.ok(userService.logout(userId));
    }

    @Deprecated
    @PostMapping("/refreshToken")
    public R<Boolean> refreshToken(HttpServletResponse response, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        Cookie cookie = null;
        for (Cookie c : cookies) {
            if (c.getName().equals("refresh_token")) {
                cookie = c;
                break;
            }
        }
        if (cookie == null) {
            response.setStatus(401);
            return R.err();
        }
        String refreshToken = cookie.getValue();
        try {
            DecodedJWT decodedJWT = JwtUtil.verifyToken(refreshToken, JwtConstant.REFRESH_TOKEN_SECRET);
            String userId = decodedJWT.getClaim("userId").asString();
            Object redisRefreshToken = redisUtil.getStringValue("refresh_token_" + userId);
            if (!refreshToken.equals(redisRefreshToken)) {
                response.setStatus(401);
                return R.err();
            }
            TokenUserInfoVo tokenUserInfoVo = userService.refreshToken(userId);
            setCookie(response, "access_token", tokenUserInfoVo.getAccessToken());
            setCookie(response, "refresh_token", tokenUserInfoVo.getRefreshToken());
            return R.ok(true);
        } catch (Exception e) {
            response.setStatus(401);
            return R.err();
        }
    }

    @GetMapping("/getUserInfo")
    public R<ResUserInfoVo> getUserInfo(@RequestHeader(JwtConstant.USER_ID) String userId) {
        return R.ok(userService.getUserInfo(userId));
    }

    @PutMapping("/updateUserInfo")
    public R<Boolean> updateUserInfo(@RequestBody User user) {
        return R.ok(userService.updateUserInfo(user));
    }


}
