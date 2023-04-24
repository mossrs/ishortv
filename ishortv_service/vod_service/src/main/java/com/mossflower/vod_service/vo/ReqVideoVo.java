package com.mossflower.vod_service.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author z's'b
 * @date 2021/1/26 14:19
 * @description 前端传入的视频信息体
 */
@Data
public class ReqVideoVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String title;

    private String videoKey;

    private String coverKey;

    /**
     * 分类数组
     */
    private List<String> categories;

    /**
     *
     * 标签列表
     */
    private List<String> tags;

}
