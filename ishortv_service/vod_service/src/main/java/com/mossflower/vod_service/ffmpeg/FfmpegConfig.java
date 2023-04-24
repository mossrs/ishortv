package com.mossflower.vod_service.ffmpeg;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author z's'b
 */
@Configuration
@Data
public class FfmpegConfig {

    @Value("${ffmpeg.path}")
    private String path;

}
