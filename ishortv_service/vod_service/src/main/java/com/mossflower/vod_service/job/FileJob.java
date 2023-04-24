package com.mossflower.vod_service.job;

import com.mossflower.ishortv_common.exception.SystemException;
import com.mossflower.vod_service.entity.Video;
import com.mossflower.vod_service.mapper.VideoMapper;
import com.mossflower.ishortv_common.util.CosUtil;
import com.qcloud.cos.model.COSObjectSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * @author z's'b
 * @date 2023/2/21 星期二 22:30
 * @description
 */
@Component
@Slf4j
public class FileJob {

    @Autowired
    private VideoMapper videoMapper;

    // 每星期一凌晨1点执行一次
    @Scheduled(cron = "0 0 1 ? * MON")
    public void deleteVideo() {
        List<Video> videos = videoMapper.selectList(null);
        List<COSObjectSummary> cosObjectSummaries;
        try {
            cosObjectSummaries = CosUtil.getFileList("");
            cosObjectSummaries.forEach(cosObjectSummary -> {
                if (videos.stream().noneMatch(video -> video.getVideoUrl().equals(cosObjectSummary.getKey()))) {
                    String dir = cosObjectSummary.getKey().split("/")[1];
                    try {
                        CosUtil.deleteDir("" + dir);
                    } catch (IOException e) {
                        throw new SystemException("删除文件失败");
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            log.error("获取文件列表失败");
            throw new SystemException("获取文件列表失败");
        }

    }

    // 每星期一凌晨2点执行一次
    @Scheduled(cron = "0 0 2 ? * MON")
    public void deleteImage() {
        List<Video> videos = videoMapper.selectList(null);
        List<COSObjectSummary> cosObjectSummaries;
        try {
            cosObjectSummaries = CosUtil.getFileList("");
        } catch (IOException e) {
            throw new SystemException("获取文件列表失败");
        }
        cosObjectSummaries.forEach(cosObjectSummary -> {
            if (videos.stream().noneMatch(video -> video.getCoverUrl().equals(cosObjectSummary.getKey()))) {
                try {
                    CosUtil.deleteFile(cosObjectSummary.getKey());
                } catch (IOException e) {
                    throw new SystemException("删除文件失败");
                }
            }
        });
    }
}
