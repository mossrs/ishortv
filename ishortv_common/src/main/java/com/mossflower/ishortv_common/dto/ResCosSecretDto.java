package com.mossflower.ishortv_common.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author z's'b
 * @date 2023/3/2 星期四 17:30
 * @description
 */
@Data
public class ResCosSecretDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Credentials credentials;

    private Long startTime;

    private Long expiredTime;

    @Data
    public static class Credentials implements Serializable {
        private static final long serialVersionUID = 1L;
        private String tmpSecretId;
        private String tmpSecretKey;
        private String sessionToken;
    }
}
