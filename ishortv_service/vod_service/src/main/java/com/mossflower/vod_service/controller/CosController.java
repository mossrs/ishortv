package com.mossflower.vod_service.controller;

import com.mossflower.ishortv_common.result.R;
import com.mossflower.vod_service.service.CosService;
import com.mossflower.ishortv_common.dto.ResCosSecretDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author z's'b
 * @date 2023/3/2 星期四 18:01
 * @description
 */
@RestController
@Slf4j
@RequestMapping("/vod/admin/cos")
public class CosController {

    @Autowired
    private CosService cosService;

    @PostMapping("/getCosSecret")
    public R<ResCosSecretDto> getCosSecret() {
        return R.ok(cosService.getCosSecret());
    }
}
