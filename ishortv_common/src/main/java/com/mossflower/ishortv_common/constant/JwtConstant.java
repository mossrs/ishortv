package com.mossflower.ishortv_common.constant;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

/**
 * @author z's'b
 * @date 2023/3/7 星期二 17:02
 * @description
 */
public class JwtConstant {

    public static final String COOKIE_ACCESS_TOKEN = "access_token";
    public static final String COOKIE_REFRESH_TOKEN = "refresh_token";
    public static final String TOKEN_PREFIX = "";
    public static final Long ACCESS_TOKEN_EXPIRE = 0L;
    public static final Long REFRESH_TOKEN_EXPIRE = 0L;
    public static final String ACCESS_TOKEN_SECRET = "";
    public static final String REFRESH_TOKEN_SECRET = "";

    public static final List<String> IGNORE_URLS = Arrays.asList(
            "/user/login",
            "/user/register",
//            "/user/refreshToken",
            "/vod/getVideoUrl",
            "/vod/getBannerVideos",
            "/vod/getCategoryVideos",
            "/vod/getMoreVideos",
            "/vod/getAllVideos"
    );
    public static final String IGNORE_URL_PREFIX = "/ishortv";
    public static final String ADMIN_URL_PREFIX = "/admin";
    public static final String USER_ID = "userId";
    public static final String IS_ADMIN = "isAdmin";

}
