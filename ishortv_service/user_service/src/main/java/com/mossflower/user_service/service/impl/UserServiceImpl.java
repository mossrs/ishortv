package com.mossflower.user_service.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mossflower.ishortv_common.constant.JwtConstant;
import com.mossflower.ishortv_common.constant.RedisConstant;
import com.mossflower.ishortv_common.enums.CdnSignTypeEnum;
import com.mossflower.ishortv_common.exception.ClientException;
import com.mossflower.ishortv_common.util.CdnUtil;
import com.mossflower.ishortv_common.util.RedisUtil;
import com.mossflower.ishortv_common.util.JwtUtil;
import com.mossflower.user_service.entity.User;
import com.mossflower.user_service.mapper.UserMapper;
import com.mossflower.user_service.service.UserService;
import com.mossflower.user_service.vo.TokenUserInfoVo;
import com.mossflower.user_service.vo.ResUserInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * @author z's'b
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisUtil redisUtil;

    private TokenUserInfoVo getTokenUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        Map<String, String> map = new HashMap<>();
        map.put(JwtConstant.USER_ID, userId.toString());
        map.put(JwtConstant.IS_ADMIN, user.getIsAdmin().toString());
        String accessToken = JwtUtil.getToken(map, JwtConstant.ACCESS_TOKEN_EXPIRE,
                JwtConstant.ACCESS_TOKEN_SECRET);
        TokenUserInfoVo tokenUserInfoVo = new TokenUserInfoVo();
        Object redisAccessToken = redisUtil.getStringValue(RedisConstant.ACCESS_TOKEN_PREFIX + userId);
        if (redisAccessToken != null) {
            tokenUserInfoVo.setAccessToken(redisAccessToken.toString());
        } else {
            tokenUserInfoVo.setAccessToken(accessToken);
            redisUtil.setStringValue(RedisConstant.ACCESS_TOKEN_PREFIX + userId, accessToken, JwtConstant.ACCESS_TOKEN_EXPIRE,
                    TimeUnit.MILLISECONDS);
        }
        //        String refreshToken = JwtUtil.getToken(map, JwtConstant.REFRESH_TOKEN_EXPIRE,
//                JwtConstant.REFRESH_TOKEN_SECRET);
//        Object redisRefreshToken = redisUtil.getStringValue("refresh_token_" + userId);
//        if (redisRefreshToken != null) {
//            tokenUserInfoVo.setRefreshToken(redisRefreshToken.toString());
//        } else {
//            tokenUserInfoVo.setRefreshToken(refreshToken);
//            redisUtil.setStringValue("refresh_token_" + userId, refreshToken, JwtConstant.REFRESH_TOKEN_EXPIRE,
//                    TimeUnit.MILLISECONDS);
//        }
        return tokenUserInfoVo;
    }


    @Override
    public TokenUserInfoVo register(User user) {
        String username = user.getUsername();
        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.eq(User::getUsername, username);
        User selectUser = userMapper.selectOne(userQueryWrapper);
        if (selectUser != null) {
            log.warn("用户名已存在，请重新输入");
            throw new ClientException("用户名已存在，请重新输入");
        }
        user.setPassword(DigestUtil.bcrypt(user.getPassword()));
        user.setCreateTime(new Date(System.currentTimeMillis()));
        userMapper.insert(user);
        return getTokenUserInfo(user.getId());
    }

    @Override
    public TokenUserInfoVo login(User user) {
        User selectUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, user.getUsername()));
        if (selectUser == null) {
            log.warn("用户名不存在，请注册后重新登陆");
            throw new ClientException("用户名不存在，请注册后重新登陆");
        }
        if (!DigestUtil.bcryptCheck(user.getPassword(), selectUser.getPassword())) {
            log.warn("用户密码输入错误");
            throw new ClientException("密码错误");
        }
        return getTokenUserInfo(selectUser.getId());
    }

    @Override
    public Boolean logout(String userId) {
//        Long batch = redisUtil.deleteBatch(Arrays.asList("access_token_" + userId, "refresh_token_" + userId));
//        return batch == 2;
        return redisUtil.delete(RedisConstant.ACCESS_TOKEN_PREFIX + userId);
    }

    @Override
    public TokenUserInfoVo refreshToken(String userId) {
        return getTokenUserInfo(Long.valueOf(userId));
    }

    @Override
    public ResUserInfoVo getUserInfo(String userId) {
        User user = userMapper.selectById(userId);
        ResUserInfoVo resUserInfoVo = new ResUserInfoVo();
        resUserInfoVo.setId(user.getId());
        resUserInfoVo.setUsername(user.getUsername());
        resUserInfoVo.setNickname(user.getNickname());
        resUserInfoVo.setEmail(user.getEmail());
        if (user.getAvatar() != null) {
            String avatar = CdnUtil.getSignUrl(CdnSignTypeEnum.A.getValue(), user.getAvatar(), null, null, null);
            resUserInfoVo.setAvatar(avatar);
        } else {
            resUserInfoVo.setAvatar(null);
        }
        return resUserInfoVo;
    }

    @Override
    public Boolean updateUserInfo(User user) {
        if (user.getPassword() != null) {
            user.setPassword(DigestUtil.bcrypt(user.getPassword()));
        }
        return userMapper.updateById(user) == 1;
    }

}
