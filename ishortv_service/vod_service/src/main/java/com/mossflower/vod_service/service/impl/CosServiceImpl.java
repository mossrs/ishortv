package com.mossflower.vod_service.service.impl;

import com.mossflower.ishortv_common.exception.SystemException;
import com.mossflower.vod_service.service.CosService;
import com.mossflower.ishortv_common.util.CosUtil;
import com.mossflower.ishortv_common.dto.ResCosSecretDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author z's'b
 * @date 2023/3/2 星期四 17:25
 * @description
 */
@Service
@Transactional
@Slf4j
public class CosServiceImpl implements CosService {

    @Override
    public ResCosSecretDto getCosSecret() {
        try {
            return CosUtil.getCosSecret();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取cos临时密钥失败====" + e.getMessage());
            throw new SystemException("获取cos临时密钥失败");
        }
    }
}
