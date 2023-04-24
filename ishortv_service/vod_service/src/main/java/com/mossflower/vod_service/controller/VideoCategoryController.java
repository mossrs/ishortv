package com.mossflower.vod_service.controller;

import com.mossflower.ishortv_common.result.R;
import com.mossflower.vod_service.service.VideoCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author z's'b
 * @date 2023/3/22 星期三 16:23
 * @description
 */
@RestController
@RequestMapping("/vod")
@Slf4j
public class VideoCategoryController {

    @Autowired
    private VideoCategoryService videoCategoryService;

    @GetMapping("/admin/getVideoCategoryLevelList")
    public R<List<String>> getVideoCategoryLevelList(Integer level) {
        return R.ok(videoCategoryService.getVideoCategoryLevelList(level));
    }
}
