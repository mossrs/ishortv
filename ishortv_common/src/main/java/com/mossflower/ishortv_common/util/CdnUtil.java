package com.mossflower.ishortv_common.util;

import cn.hutool.core.convert.Convert;
import com.mossflower.ishortv_common.constant.CdnConstant;
import com.qcloud.cos.utils.Md5Utils;

import java.util.UUID;

/**
 * @author z's'b
 * @date 2023/3/5 星期日 10:31
 * @description
 */
public class CdnUtil {

    /**
     * 选用A类型 传入 type key expire 其他都为null
     * 若expire 为null 时间戳很大 一般不会过期 用来返回图片链接
     * 若传入expire 则返回的链接会在expire秒后过期 用来返回视频链接
     *
     * @param type   类型
     * @param key    文件名
     * @param expire 过期时间
     * @param suffix 后缀
     * @param ttl    进制
     *
     * @return 签名的cdn链接
     */
    public static String getSignUrl(String type, String key, Long expire, String suffix, Integer ttl) {
        String path = "/" + key;
        // 单位是秒 如果不输入就基本永久不会过期
        String time = Convert.toStr(System.currentTimeMillis());
        if (expire != null) {
            time = Convert.toStr(Convert.toLong(time) / 1000 + expire);
        }
        switch (type) {
            case "A": {
                String randomStr = UUID.randomUUID().toString().replace("-", "");
                String md5Str = String.format("%s-%s-%s-%s-%s", path, time, randomStr, 0, CdnConstant.SIGN_KEY);
                String sign = Md5Utils.md5Hex(md5Str);
                return String.format("%s%s?%s=%s", CdnConstant.DOMAIN, path, CdnConstant.SIGN_PARAM,
                        String.format("%s-%s-%s-%s", time, randomStr, 0, sign));
            }
            case "B": {
                String sign = Md5Utils.md5Hex(String.format("%s%s%s", CdnConstant.SIGN_KEY, time, path));
                if (suffix == null) {
                    return String.format("%s/%s/%s%s", CdnConstant.DOMAIN, sign, time, path);
                } else {
                    return String.format("%s/%s/%s%s%s", CdnConstant.DOMAIN, sign, time, path, suffix);
                }
            }
            case "C": {
                String hexUnixTime = getHexCurrentTimeUnix4byte(expire);
                String sign = Md5Utils.md5Hex(String.format("%s%s%s", CdnConstant.SIGN_KEY, path, hexUnixTime));
                if (suffix == null) {
                    return String.format("%s/%s/%s%s", CdnConstant.DOMAIN, sign, hexUnixTime, path);
                } else {
                    return String.format("%s/%s/%s%s%s", CdnConstant.DOMAIN, sign, hexUnixTime, path, suffix);
                }
            }
            case "D": {
                String unixTime;
                if (ttl == 16) {
                    unixTime = getHexCurrentTimeUnix4byte(expire);
                } else {
                    unixTime = time;
                }
                String sign = Md5Utils.md5Hex(String.format("%s%s%s", CdnConstant.SIGN_KEY, path, unixTime));
                return String.format("%s%s?%s=%s&%s=%s", CdnConstant.DOMAIN, path, CdnConstant.SIGN_PARAM,
                        sign, CdnConstant.TIME_PARAM, unixTime);
            }
            default: {
                return null;
            }
        }
    }

    private static String getHexCurrentTimeUnix4byte(Long expire) {
        String six = Long.toHexString(System.currentTimeMillis() / 1000 + expire);
        StringBuilder zero = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            if (six.length() < 8) {
                zero.append("0");
            }
        }
        return zero + six;
    }

    public String getUrl(String key) {
        return CdnConstant.DOMAIN + key;
    }
}
