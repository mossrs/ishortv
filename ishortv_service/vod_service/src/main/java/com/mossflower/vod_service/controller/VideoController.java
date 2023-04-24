package com.mossflower.vod_service.controller;

import com.mossflower.ishortv_common.result.R;
import com.mossflower.ishortv_common.result.ResPage;
import com.mossflower.vod_service.entity.Video;
import com.mossflower.vod_service.service.VideoService;
import com.mossflower.vod_service.vo.ReqPageVideoVo;
import com.mossflower.vod_service.vo.ReqVideoVo;
import com.mossflower.vod_service.vo.ResBaseVideoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author z's'b
 */
@RestController
@Slf4j
@RequestMapping("/vod")
public class VideoController {

    @Resource
    private VideoService videoService;

//    @PostMapping("/admin/uploadVideo")
//    public R<String> uploadVideo(MultipartFile video) {
//        return R.ok(videoService.uploadVideo(video));
//    }
//
//    @PostMapping("/admin/uploadImage")
//    public R<String> uploadImage(MultipartFile image) {
//        return R.ok(videoService.uploadImage(image));
//    }

    @PostMapping("/admin/saveVideoMsg")
    public R<Boolean> saveVideoMsg(@RequestBody ReqVideoVo reqVideoVo, @RequestHeader("userId") String userId) {
        return R.ok(videoService.saveVideoMsg(reqVideoVo, userId));
    }

    @GetMapping("/getVideoUrl")
    public R<String> getVideoUrl(String key) {
        String url = videoService.getVideoUrl(key);
        return url == null ? R.err("获取视频失败失败") : R.ok(url);
    }

    @PostMapping("/getBannerVideos")
    public R<List<ResBaseVideoVo>> getBannerVideos(@RequestBody List<String> categories) {
        return R.ok(videoService.getBannerVideos(categories));
    }

    @PostMapping("/getCategoryVideos")
    public R<List<ResBaseVideoVo>> getCategoryVideos(@RequestBody List<String> categories) {
        return R.ok(videoService.getCategoryVideos(categories));
    }

    @PostMapping("/getMoreVideos")
    public R<List<ResBaseVideoVo>> getMoreVideos(@RequestBody List<String> categories) {
        return R.ok(videoService.getMoreVideos(categories));
    }

    @PostMapping("/getAllVideos")
    public R<ResPage<Video, ResBaseVideoVo>> getAllPageVideos(@RequestBody ReqPageVideoVo reqPageVideoVo) {
        return R.ok(videoService.getAllPageVideos(reqPageVideoVo));
    }

}
