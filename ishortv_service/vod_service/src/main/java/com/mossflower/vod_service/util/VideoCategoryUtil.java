package com.mossflower.vod_service.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mossflower.vod_service.entity.Video;
import com.mossflower.vod_service.entity.VideoCategory;
import com.mossflower.vod_service.mapper.VideoCategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author z's'b
 * @date 2023/4/3 星期一 11:52
 * @description
 */
@Component
public class VideoCategoryUtil {

    @Autowired
    private VideoCategoryMapper videoCategoryMapper;

    public VideoCategory getCategory(List<String> categories, Integer level) {
        List<VideoCategory> idxCategories = videoCategoryMapper.selectList(
                new LambdaQueryWrapper<VideoCategory>().eq(VideoCategory::getName, categories.get(level)));
        for (VideoCategory videoCategory : idxCategories) {
            boolean flag = true;
            String path = videoCategory.getPath();
            String[] ids = path.split("/");
            for (int i = 0; i < ids.length; i++) {
                VideoCategory category = videoCategoryMapper.selectById(ids[i]);
                if (!category.getName().equals(categories.get(i))) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                return videoCategory;
            }
        }
        return null;
    }
}
