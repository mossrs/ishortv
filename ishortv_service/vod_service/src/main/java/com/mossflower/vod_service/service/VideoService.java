package com.mossflower.vod_service.service;

import com.mossflower.ishortv_common.result.ResPage;
import com.mossflower.vod_service.entity.Video;
import com.mossflower.vod_service.vo.ReqPageVideoVo;
import com.mossflower.vod_service.vo.ReqVideoVo;
import com.mossflower.vod_service.vo.ResBaseVideoVo;

import java.util.List;

/**
 * @author z's'b
 */
public interface VideoService {

    /**
     * 分块上传视频
     * 视频格式不限 统一转换成m3u8格式
     * 视频大小200M以内
     * 视频时长10分钟左右
     *
     * @param video 视频
     * @return 视频url
     */
//    String uploadVideo(MultipartFile video);

    /**
     * 上传视频封面
     * 图片格式不限
     * 图片大小不超过5M
     *
     * @param image 图片
     * @return 图片url
     */
//    String uploadImage(MultipartFile image);


    /**
     * 保存视频信息
     *
     * @param reqVideoVo 视频信息
     * @param userId     用户id
     * @return 视频信息
     */
    Boolean saveVideoMsg(ReqVideoVo reqVideoVo, String userId);

    /**
     * 返回视频url
     *
     * @param key 加密视频key
     * @return 真正播放视频url
     */
    String getVideoUrl(String key);

    /**
     * 返回5个banner轮播视频
     *
     * @return 5轮播视频
     */
    List<ResBaseVideoVo> getBannerVideos(List<String> categories);

    /**
     * 返回视频列表 长度为48
     *
     * @param categories 视频分类数组
     * @return 视频列表
     */
    List<ResBaseVideoVo> getCategoryVideos(List<String> categories);

    /**
     * 返回更多视频列表 固定长度为12
     *
     * @param categories 视频分类
     * @return 视频列表
     */
    List<ResBaseVideoVo> getMoreVideos(List<String> categories);

    /**
     * 返回所有分页视频列表
     * 每页视频数量为12 页数从1开始
     *
     * @param reqPageVideoVo 分页视频信息
     * @return 视频列表
     */
    ResPage<Video, ResBaseVideoVo> getAllPageVideos(ReqPageVideoVo reqPageVideoVo);

}
