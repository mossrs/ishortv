package com.mossflower.vod_service.util;

import com.mossflower.ishortv_common.constant.CosConstant;
import com.mossflower.ishortv_common.exception.SystemException;
import com.mossflower.ishortv_common.result.CommonMsgCode;
import com.mossflower.ishortv_common.util.CosUtil;
import com.mossflower.vod_service.dto.ResTranscodeVideoDto;
import com.mossflower.vod_service.entity.VideoInfo;
import com.mossflower.vod_service.ffmpeg.ConvertM3U8;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaInfo;
import ws.schild.jave.MyMultimediaInfo;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author z's'b
 */
@Component
@Slf4j
public class VideoUtil {

    @Autowired
    private ConvertM3U8 convertM3U8;

    /**
     * 上传视频，获取视频时长，返回时分秒字符串
     *
     * @return 时长，单位：时分秒
     */
    public VideoInfo getVideoInfo(String url) throws Exception {
        // 获取视频时长，返回毫秒
        File file = new File(url);
        MultimediaInfo info = new MyMultimediaInfo(file).getInfo(url);
        VideoInfo videoInfo = new VideoInfo();
        videoInfo.setDuration(durationFormat(info.getDuration()));
        videoInfo.setWidth(info.getVideo().getSize().getWidth());
        videoInfo.setHeight(info.getVideo().getSize().getHeight());
        videoInfo.setFrameRate(info.getVideo().getFrameRate());
        videoInfo.setBitRate(info.getVideo().getBitRate());
        videoInfo.setDecoder(info.getVideo().getDecoder());
        videoInfo.setFormat(info.getFormat());
        return videoInfo;
    }

    private String durationFormat(long duration) {
        // 日期格式化对象，给时分秒格式
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        // 这里很重要，如果不设置时区的话，输出结果就会是几点钟，而不是毫秒值对应的时分秒数量了。
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        // 毫秒转化为字符串
        return formatter.format(duration);
    }

    public CompletableFuture<ResTranscodeVideoDto> transcode(String key) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ResTranscodeVideoDto resTranscodeVideoDto = new ResTranscodeVideoDto();
                // 本地存储临时视频文件夹
                String videoDir = UUID.randomUUID().toString().replace("-", "").substring(0, 7) + "/";
                // 本地存储临时视频文件夹绝对路径
                String localStorageDirPath = System.getProperty("user.dir") + "/m3u8/" + videoDir;
                File localStorageDirFile = new File(localStorageDirPath);
                // 如果文件夹不存在则创建
                if (!localStorageDirFile.exists()) {
                    boolean b = localStorageDirFile.mkdirs();
                    if (!b) {
                        log.error("创建本地文件夹失败");
                        throw new SystemException("上传失败");
                    }
                }
                // 将客户端上传的视频文件下载到本地 并保存原视频信息到本地
                // 1 获取视频文件的下载链接
                String signUrl = CosUtil.getOriginSignUrl(key, CosConstant.SIGN_EXPIRE);
                resTranscodeVideoDto.setVideoInfo(getVideoInfo(signUrl));
                // 2 更改文件名 避免重复
                String suffix = key.substring(key.lastIndexOf("."));
                String newFileName = UUID.randomUUID().toString().replace("-", "") + suffix;
                String videoFilePath = localStorageDirPath + newFileName;
                // 3 下载文件到本地
                FileUtils.copyURLToFile(new URL(signUrl), new File(videoFilePath));
                // 转码操作
                // 1 获取转码后的视频文件名
                String m3u8Path = videoFilePath.substring(0, videoFilePath.lastIndexOf(".")) + ".m3u8";
                // 2 转码
                convertM3U8.processM3U8(videoFilePath, m3u8Path);
                // 上传到腾讯云
                // 1 获取上传文件的文件名 除了原始文件 其他文件都是转码后的文件
                File[] files = localStorageDirFile.listFiles(pathname -> !pathname.getName().endsWith(suffix));
                if (files == null || files.length == 0) {
                    log.error("获取转码后的文件失败");
                    throw new SystemException(CommonMsgCode.MSG_ERR);
                }
                // 2 上传文件
                for (File file : files) {
                    if (!file.isDirectory()) {
                        String cosKey = "video/" + videoDir + file.getName();
                        if (file.getName().endsWith(".m3u8")) {
                            resTranscodeVideoDto.setVideoUrl(cosKey);
                        }
                        CosUtil.uploadPart(file, cosKey);
                    }
                }
                // 删除本地文件
                FileUtils.deleteDirectory(localStorageDirFile);
                CosUtil.deleteFile(key);
                return resTranscodeVideoDto;
            } catch (Exception e) {
                throw new SystemException(CommonMsgCode.MSG_ERR);
            }
        });
    }

}
