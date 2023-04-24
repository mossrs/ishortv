package com.mossflower.user_service.service;

import com.mossflower.user_service.entity.User;
import com.mossflower.user_service.vo.TokenUserInfoVo;
import com.mossflower.user_service.vo.ResUserInfoVo;

/**
 * @author z's'b
 */
public interface UserService {

    /**
     * 注册
     *
     * @param user 用户信息
     * @return 布尔值
     */
    TokenUserInfoVo register(User user);

    /**
     * 登录
     * <p>
     * accessToken 有效期 30分钟 键值都为accessToken
     * refreshToken 有效期 7天  键值为refreshToken
     *
     * @param user 用户信息
     * @return token
     */
    TokenUserInfoVo login(User user);

    /**
     * 登出
     * 删掉redis中的accessToken
     *
     * @return 是否成功
     */
    Boolean logout(String userId);

    /**
     * 返回新的token信息
     *
     * @param userId  用户id
     * @return token信息
     */
    TokenUserInfoVo refreshToken(String userId);

    /**
     * 获取用户信息
     *
     * @param userId 用户id
     * @return 用户信息
     */
    ResUserInfoVo getUserInfo(String userId);

    /**
     * 修改用户信息
     *
     * @param user 用户信息
     * @return 是否成功
     */
    Boolean updateUserInfo(User user);
}
