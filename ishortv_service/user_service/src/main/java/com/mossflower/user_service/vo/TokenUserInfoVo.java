package com.mossflower.user_service.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author z's'b
 * @date 2023/2/28 星期二 19:32
 * @description
 */
@Data
public class TokenUserInfoVo implements Serializable {

    public static final long serialVersionUID = 1L;

    private String accessToken;

    private String refreshToken;


}
