package com.mossflower.vod_service.service;

import com.mossflower.vod_service.entity.VideoCategory;

import java.util.List;

/**
 * @author z's'b
 */
public interface VideoCategoryService {

    List<String> getVideoCategoryLevelList(Integer level);

}
