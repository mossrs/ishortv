package com.mossflower.vod_service.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author z's'b
 */
@Data
public class ResBaseVideoVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String title;

    private String videoKey;

    private String coverUrl;

    /**
     * 视频时长
     */
    private String duration;

    /**
     *
     * 标签列表
     */
    private List<String> tags;

}
