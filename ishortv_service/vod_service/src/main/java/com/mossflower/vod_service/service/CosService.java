package com.mossflower.vod_service.service;

import com.mossflower.ishortv_common.dto.ResCosSecretDto;

/**
 * @author z's'b
 * @date 2023/3/2 星期四 17:25
 * @description
 */
public interface CosService {

    /**
     * 获取cos临时密钥
     *
     * @return cos临时密钥
     */
    ResCosSecretDto getCosSecret();
}
