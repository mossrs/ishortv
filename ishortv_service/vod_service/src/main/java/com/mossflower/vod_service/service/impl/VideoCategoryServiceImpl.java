package com.mossflower.vod_service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mossflower.vod_service.entity.VideoCategory;
import com.mossflower.vod_service.mapper.VideoCategoryMapper;
import com.mossflower.vod_service.service.VideoCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author z's'b
 */
@Service
public class VideoCategoryServiceImpl implements VideoCategoryService {

    @Autowired
    private VideoCategoryMapper videoCategoryMapper;

    @Override
    public List<String> getVideoCategoryLevelList(Integer level) {
        ArrayList<String> categories = new ArrayList<>();
        videoCategoryMapper.selectList(null).stream().filter(
                videoCategory -> videoCategory.getPath().split("/").length == level
        ).forEach(
                videoCategory -> categories.add(videoCategory.getName())
        );
        return categories.stream().distinct().collect(Collectors.toList());
    }
}
