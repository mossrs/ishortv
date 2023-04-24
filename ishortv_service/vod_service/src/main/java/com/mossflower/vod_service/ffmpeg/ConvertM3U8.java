package com.mossflower.vod_service.ffmpeg;


import com.alibaba.cloud.commons.lang.StringUtils;
import com.mossflower.ishortv_common.exception.SystemException;
import com.mossflower.ishortv_common.result.CommonMsgCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * m3u8工具类
 **/
@Slf4j
@Component
public class ConvertM3U8 {

    @Resource
    private FfmpegConfig ffmpegConfig;

    /**
     * ffmPeg程序转换m3u8
     * ffmPeg -i vue.mp4 -c:v libx264 -hls_time 20 -hls_list_size 0 -c:a aac -strict -2 -f hls vue.m3u8
     * ffmPeg能解析的格式：（asx，asf，mpg，wmv，3gp，mp4，mov，avi，flv等）
     *
     * @param inputPath  输入路径
     * @param outPutPath 输出路径
     */
    public void processM3U8(String inputPath, String outPutPath) throws IOException {
        //这里就写入执行语句就可以了
        List<String> commend = new ArrayList<>();
        commend.add(ffmpegConfig.getPath());
        commend.add("-i");
        commend.add(inputPath);
        commend.add("-c:v");
        commend.add("libx264");
        commend.add("-hls_time");
        commend.add("20");
        commend.add("-hls_list_size");
        commend.add("0");
        commend.add("-c:a");
        commend.add("aac");
        commend.add("-strict");
        commend.add("-2");
        commend.add("-f");
        commend.add("hls");
        commend.add(outPutPath);
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(commend);
        Process p = builder.start();
        int i = doWaitFor(p);
        p.destroy();
    }

    /**
     * 监听ffmPeg运行过程
     *
     * @param p 进程
     * @return 直接结果
     */
    private int doWaitFor(Process p) {
        InputStream in = null;
        InputStream err = null;
        // returned to caller when p is finished
        int exitValue = -1;
        try {
            log.debug("***检测ffmPeg运行***");
            in = p.getInputStream();
            err = p.getErrorStream();
            // Set to true when p is finished
            boolean finished = false;
            while (!finished) {
                try {
                    while (in.available() > 0) {
                        Character c = (char) in.read();
                    }
                    while (err.available() > 0) {
                        Character c = (char) err.read();
                    }

                    exitValue = p.exitValue();
                    finished = true;

                } catch (IllegalThreadStateException e) {
                    Thread.sleep(500);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new SystemException(CommonMsgCode.MSG_ERR);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
            if (err != null) {
                try {
                    err.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }
        return exitValue;
    }
}
