package com.mossflower.vod_service;

import cn.hutool.core.net.URLEncodeUtil;
import com.mossflower.ishortv_common.constant.CdnConstant;
import com.mossflower.ishortv_common.constant.CosConstant;
import com.mossflower.ishortv_common.util.CdnUtil;
import com.mossflower.ishortv_common.util.CosUtil;
import com.mossflower.vod_service.entity.VideoInfo;
import com.mossflower.vod_service.enums.CdnSignTypeEnum;
import com.mossflower.vod_service.util.VideoUtil;
import org.bouncycastle.util.encoders.UTF8;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ws.schild.jave.EncoderException;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

@SpringBootTest
class VodServiceApplicationTests {


    @Test
    void contextLoads() throws Exception {
        // java的编码类用哪个
//        String url = CosUtil.getOriginSignUrl("temp/bandicam 2023-04-03 09-19-32-683.mp4", CosConstant.SIGN_EXPIRE);
//        String url = CdnUtil.getSignUrl(CdnSignTypeEnum.A.getValue(),
//                "images/3.jpg", CdnConstant.EXPIRE, null, null);
//        System.out.println(url);
    }

}
