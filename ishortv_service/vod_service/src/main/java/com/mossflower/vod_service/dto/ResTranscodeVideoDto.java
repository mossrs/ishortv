package com.mossflower.vod_service.dto;

import com.mossflower.vod_service.entity.VideoInfo;
import lombok.Data;

import java.io.Serializable;

/**
 * @author z's'b
 * @date 2023/3/9 星期四 13:02
 * @description
 */
@Data
public class ResTranscodeVideoDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String videoUrl;

    private VideoInfo videoInfo;
}
