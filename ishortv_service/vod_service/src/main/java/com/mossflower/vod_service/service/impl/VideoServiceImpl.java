package com.mossflower.vod_service.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mossflower.ishortv_common.constant.CdnConstant;
import com.mossflower.ishortv_common.exception.SystemException;
import com.mossflower.ishortv_common.result.CommonMsgCode;
import com.mossflower.ishortv_common.result.ResPage;
import com.mossflower.ishortv_common.util.CdnUtil;
import com.mossflower.ishortv_common.util.RedisUtil;
import com.mossflower.vod_service.entity.Video;
import com.mossflower.vod_service.entity.VideoCategory;
import com.mossflower.vod_service.entity.VideoInfo;
import com.mossflower.vod_service.entity.VideoTag;
import com.mossflower.vod_service.enums.CdnSignTypeEnum;
import com.mossflower.vod_service.mapper.VideoCategoryMapper;
import com.mossflower.vod_service.mapper.VideoInfoMapper;
import com.mossflower.vod_service.mapper.VideoMapper;
import com.mossflower.vod_service.mapper.VideoTagMapper;
import com.mossflower.vod_service.service.VideoService;
import com.mossflower.vod_service.util.VideoCategoryUtil;
import com.mossflower.vod_service.util.VideoUtil;
import com.mossflower.vod_service.vo.ReqPageVideoVo;
import com.mossflower.vod_service.vo.ReqVideoVo;
import com.mossflower.vod_service.vo.ResBaseVideoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.sql.Date;


/**
 * @author z's'b
 */
@Service
@Slf4j
@Transactional
public class VideoServiceImpl implements VideoService {

    @Autowired
    private VideoMapper videoMapper;
    @Autowired
    private VideoInfoMapper videoInfoMapper;
    @Autowired
    private VideoCategoryMapper videoCategoryMapper;
    @Autowired
    private VideoTagMapper videoTagMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private VideoUtil videoUtil;
    @Autowired
    private VideoCategoryUtil videoCategoryUtil;

    @Override
    public Boolean saveVideoMsg(ReqVideoVo reqVideoVo, String userId) {
        videoUtil.transcode(reqVideoVo.getVideoKey()).handle(((resTranscodeVideoDto, err) -> {
            if (err != null) {
                log.error("转码失败=====>{}", err.getMessage());
                throw new SystemException(CommonMsgCode.MSG_ERR);
            }
            // 保存标签信息
            List<String> tags = reqVideoVo.getTags();
            for (String tag : tags) {
                VideoTag videoTag = new VideoTag();
                videoTag.setName(tag);
                videoTag.setCreateTime(new Date(System.currentTimeMillis()));
                videoTag.setUpdateTime(new Date(System.currentTimeMillis()));
                videoTagMapper.insert(videoTag);
            }
            // 保存视频详情表
            VideoInfo videoInfo = resTranscodeVideoDto.getVideoInfo();
            videoInfoMapper.insert(videoInfo);
            // 保存视频
            Video video = new Video();
            video.setTitle(reqVideoVo.getTitle());
            video.setVideoUrl(resTranscodeVideoDto.getVideoUrl());
            video.setCoverUrl(reqVideoVo.getCoverKey());
            video.setUserId(Long.parseLong(userId));
            // 设置视频的分类path
            VideoCategory category = videoCategoryUtil.getCategory(reqVideoVo.getCategories(),
                    reqVideoVo.getCategories().size() - 1);
            video.setCategoryPath(category.getPath());
            video.setCreateTime(new Date(System.currentTimeMillis()));
            video.setUpdateTime(new Date(System.currentTimeMillis()));
            // 先插入视频信息 获取id 再更新标签信息和视频信息中的videoId
            videoMapper.insert(video);
            // 设置视频信息里的视频id
            videoInfo.setVideoId(video.getId());
            videoInfoMapper.updateById(videoInfo);

            // 设置视频的标签id
            for (String tag : tags) {
                List<VideoTag> videoTags = videoTagMapper.selectList(new LambdaQueryWrapper<VideoTag>()
                                .eq(VideoTag::getName, tag))
                        .stream().filter(videoTag -> videoTag.getVideoId() == null).collect(Collectors.toList());
                for (VideoTag videoTag : videoTags) {
                    videoTag.setVideoId(video.getId());
                    videoTagMapper.updateById(videoTag);
                }
            }
            return null;
        }));
        return true;
    }

    @Override
    public String getVideoUrl(String key) {
        List<Video> videos = videoMapper.selectList(null);
        for (Video video : videos) {
            if (DigestUtil.bcryptCheck(video.getVideoUrl(), key)) {
                return CdnUtil.getSignUrl(CdnSignTypeEnum.A.getValue(),
                        video.getVideoUrl(), CdnConstant.EXPIRE, null, null);
            }
        }
        return null;
    }

    @Override
    public List<ResBaseVideoVo> getBannerVideos(List<String> categories) {
        VideoCategory category = videoCategoryUtil.getCategory(categories, categories.size() - 1);
        if (category == null) {
            throw new SystemException(CommonMsgCode.MSG_ERR);
        }
        List<Video> videos = videoMapper.selectList(new LambdaQueryWrapper<Video>()
                .eq(Video::getCategoryPath, category.getPath())
                .last("limit " + 5));
        List<List<String>> videosTagList = getVideosTagList(videos);
        return getResBaseVideoVoList(videos, videosTagList);
    }

    @Override
    public List<ResBaseVideoVo> getCategoryVideos(List<String> categories) {
        VideoCategory category = videoCategoryUtil.getCategory(categories, categories.size() - 1);
        if (category == null) {
            throw new SystemException(CommonMsgCode.MSG_ERR);
        }
        List<Video> videos = videoMapper.selectList(new LambdaQueryWrapper<Video>()
                .eq(Video::getCategoryPath, category.getPath())
                .last("limit " + 12));
        List<List<String>> videosTagList = getVideosTagList(videos);
        return getResBaseVideoVoList(videos, videosTagList);
    }

    @Override
    public List<ResBaseVideoVo> getMoreVideos(List<String> categories) {
        VideoCategory category = videoCategoryUtil.getCategory(categories, categories.size() - 1);
        if (category == null) {
            throw new SystemException(CommonMsgCode.MSG_ERR);
        }
        List<Video> videoList = videoMapper.selectList(new LambdaQueryWrapper<Video>()
                .eq(Video::getCategoryPath, category.getPath())
                .orderByAsc(Video::getId)
                .last("limit " + Integer.MAX_VALUE + " offset " + (47)));
        return getResBaseVideoVoList(videoList, getVideosTagList(videoList));
    }

    @Override
    public ResPage<Video, ResBaseVideoVo> getAllPageVideos(ReqPageVideoVo reqPageVideoVo) {
        Long page = reqPageVideoVo.getPage();
        Long size = reqPageVideoVo.getSize();
        List<String> categories = reqPageVideoVo.getCategories();
        VideoCategory category = videoCategoryUtil.getCategory(categories, 0);
        if (category == null) {
            throw new SystemException(CommonMsgCode.MSG_ERR);
        }
        Page<Video> pageCategoryFilterVideos = videoMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Video>()
                        .eq(Video::getCategoryPath, category.getPath())
                        .orderByAsc(Video::getId));
        List<Video> videos = pageCategoryFilterVideos.getRecords();
        List<List<String>> videosTagList = getVideosTagList(videos);
        List<ResBaseVideoVo> resBaseVideoVoList = getResBaseVideoVoList(videos, videosTagList);
        return new ResPage<>(pageCategoryFilterVideos, resBaseVideoVoList);
    }

    /**
     * 返回视频的标签列表 用来设置视频的标签数组
     *
     * @param videos 视频列表
     * @return 视频的标签列表
     */
    private List<List<String>> getVideosTagList(List<Video> videos) {
        ArrayList<List<String>> tagList = new ArrayList<>();
        for (Video video : videos) {
            List<String> tags = videoTagMapper.selectList(new LambdaQueryWrapper<VideoTag>()
                            .eq(VideoTag::getVideoId, video.getId()))
                    .stream().map(VideoTag::getName).collect(Collectors.toList());
            tagList.add(tags);
        }
        return tagList;
    }

    private List<ResBaseVideoVo> getResBaseVideoVoList(List<Video> videos, List<List<String>> videosTagList) {
        return videos.stream().map(video -> {
            ResBaseVideoVo resBaseVideoVo = new ResBaseVideoVo();
            resBaseVideoVo.setTitle(video.getTitle());
            String bcrypt = DigestUtil.bcrypt(video.getVideoUrl());
            resBaseVideoVo.setVideoKey(bcrypt);
            resBaseVideoVo.setCoverUrl(CdnUtil.getSignUrl(CdnSignTypeEnum.A.getValue(),
                    video.getCoverUrl(), null, null, null));
            resBaseVideoVo.setTags(videosTagList.get(videos.indexOf(video)));
            resBaseVideoVo.setDuration(videoInfoMapper.selectOne(new LambdaQueryWrapper<VideoInfo>()
                    .eq(VideoInfo::getVideoId, video.getId())).getDuration());
            return resBaseVideoVo;
        }).collect(Collectors.toList());
    }

//    @Resource
//    private VideoMapper videoMapper;
//    @Resource
//    private VideoTagMapper videoTagMapper;
//    @Resource
//    private VideoCategoryMapper videoCategoryMapper;
//    @Resource
//    private CosConfig cosConfig;
//    @Resource
//    private FileBean fileBean;
//    @Resource
//    private ConvertM3U8 convertM3U8;
//    @Resource
//    private RedisUtil redisUtil;
//
//    @Override
//    public String uploadVideo(MultipartFile video) {
//        String resKey = null;
//        // 生成新的文件名 防止与本地文件重复
//        String newFileName = UUID.randomUUID().toString().replace("-", "") + Objects.requireNonNull(video.getOriginalFilename()).substring(video.getOriginalFilename().lastIndexOf("."));
//        // 本地存储临时视频文件夹
//        String videoDir = UUID.randomUUID().toString().replace("-", "").substring(0, 7) + "/";
//        // 本地存储临时视频文件夹绝对路径
//        String localStorageDirPath = System.getProperty("user.dir") + "/m3u8/" + videoDir;
//        File localStorageDirFile = new File(localStorageDirPath);
//        // 如果文件夹不存在则创建
//        if (!localStorageDirFile.exists()) {
//            boolean b = localStorageDirFile.mkdirs();
//            if (!b) {
//                log.error("创建本地文件夹失败");
//                throw new SystemException("上传失败");
//            }
//        }
//        // 本地存储临时视频文件绝对路径
//        String localStorageVideoPath = localStorageDirPath + newFileName;
//        try {
//            video.transferTo(new File(localStorageVideoPath));
//        } catch (IOException e) {
//            e.printStackTrace();
//            log.error("本地存储失败");
//            throw new SystemException("上传失败");
//        }
//        // 转码操作
//        // 转码文件路径
//        String m3u8Path = localStorageVideoPath.substring(0, localStorageVideoPath.lastIndexOf(".")) + ".m3u8";
//        try {
//            convertM3U8.processM3U8(localStorageVideoPath, m3u8Path);
//        } catch (IOException e) {
//            e.printStackTrace();
//            log.error("转码失败");
//            throw new SystemException("上传失败");
//        }
//        // 上传到腾讯云
//        File[] localStorageDirFiles = localStorageDirFile.listFiles();
//        if (localStorageDirFiles == null) {
//            log.error("本地文件夹为空");
//            throw new SystemException("上传失败");
//        }
//        try {
//            for (File file : localStorageDirFiles) {
//                if (!file.isDirectory()) {
//                    String name = file.getName();
//                    String fileSuffix = name.substring(name.lastIndexOf(".") + 1);
//                    String key = "video/" + videoDir + name;
//                    fileBean.uploadPart(file, key);
//                    if ("m3u8".equals(fileSuffix)) {
//                        resKey = key;
//                    }
////                    } else if ("ts".equals(fileSuffix)) {
////                        fileBean.uploadPart(file, key);
////                    }
//                    if (file.exists()) {
//                        boolean delete = file.delete();
//                        if (!delete) {
//                            return "删除文件失败";
//                        }
//                    }
//                }
//            }
//            return resKey;
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error(e.getMessage());
//            throw new SystemException("上传失败");
//        }
//    }
//
//    @Override
//    public String uploadImage(MultipartFile image) {
//        String key = "image/" + UUID.randomUUID().toString().replace("-", "") + Objects.requireNonNull(image.getOriginalFilename()).substring(image.getOriginalFilename().lastIndexOf("."));
//        try {
//            fileBean.upload(image, key);
//            return key;
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//            log.error(e.getMessage());
//            throw new SystemException("上传图片失败");
//        }
//    }
//
//    @Override
//    public Boolean saveVideoMsg(ReqVideoVo reqVideoVo, String userId) {
//        // 保存分类信息
//        List<String> categories = reqVideoVo.getCategories();
//        for (int i = 0; i < categories.size(); i++) {
//            VideoCategory videoCategory = videoCategoryMapper.selectOne(new QueryWrapper<VideoCategory>().eq("name", categories.get(i)));
//            if (videoCategory == null) {
//                // 该分类不存在，需要插入
//                VideoCategory category = new VideoCategory();
//                category.setName(categories.get(i));
//                int insert = videoCategoryMapper.insert(category);
//                if (insert != 1) {
//                    log.error("数据库插入分类失败");
//                    throw new SystemException(CommonMsgCode.MSG_ERR);
//                }
//                // 更新数据库path
//                StringBuilder categoryPath = new StringBuilder();
//                for (int j = 0; j < i + 1; j++) {
//                    VideoCategory tmpCategory = videoCategoryMapper.selectOne(new QueryWrapper<VideoCategory>().eq("name", categories.get(j)));
//                    if (j == i) {
//                        categoryPath.append(tmpCategory.getId());
//                    } else {
//                        categoryPath.append(tmpCategory.getId()).append("/");
//                    }
//                }
//                category = videoCategoryMapper.selectOne(new QueryWrapper<VideoCategory>().eq("name", categories.get(i)));
//                category.setPath(categoryPath.toString());
//                int update = videoCategoryMapper.updateById(category);
//                if (update != 1) {
//                    log.error("数据库更新分类失败");
//                    throw new SystemException(CommonMsgCode.MSG_ERR);
//                }
//            }
//        }
//
//        // 保存标签信息
//        List<String> tags = reqVideoVo.getTags();
//        for (String tag : tags) {
//            VideoTag selectTag = videoTagMapper.selectOne(new QueryWrapper<VideoTag>().eq("name", tag));
//            if (selectTag == null) {
//                VideoTag videoTag = new VideoTag();
//                videoTag.setName(tag);
//                int insert = videoTagMapper.insert(videoTag);
//                if (insert != 1) {
//                    log.error("数据库插入标签失败");
//                    throw new SystemException(CommonMsgCode.MSG_ERR);
//                }
//            }
//        }
//
//        // 保存视频信息
//        Video video = new Video();
//        video.setTitle(reqVideoVo.getTitle());
//        video.setVideoUrl(reqVideoVo.getVideoUrl());
//        video.setCoverUrl(reqVideoVo.getCoverUrl());
//        video.setUserId(Long.parseLong(userId));
//
//        // 设置视频的分类id
//        String lowestCategoryName = categories.get(categories.size() - 1);
//        VideoCategory lowestCategory = videoCategoryMapper.selectOne(new QueryWrapper<VideoCategory>().eq("name", lowestCategoryName));
//        video.setCategoryId(lowestCategory.getId());
//
//        // 先插入视频信息 获取id 再更新标签信息中的videoId
//        int insert = videoMapper.insert(video);
//        if (insert != 1) {
//            log.error("数据库插入视频失败");
//            throw new SystemException(CommonMsgCode.MSG_ERR);
//        }
//        // 获取刚插入的视频id
//        video = videoMapper.selectOne(new QueryWrapper<Video>().eq("video_url", reqVideoVo.getVideoUrl()));
//
//
//        // 设置视频的标签id
//        for (String tag : tags) {
//            VideoTag videoTag = videoTagMapper.selectOne(new QueryWrapper<VideoTag>().eq("name", tag));
//            videoTag.setVideoId(video.getId());
//            int update = videoTagMapper.updateById(videoTag);
//            if (update != 1) {
//                log.error("数据库更新标签失败");
//                throw new SystemException(CommonMsgCode.MSG_ERR);
//            }
//        }
//        return true;
//    }
//
//    @Override
//    public String getVideoUrl(String key) {
//        // DigestUtil.bcryptCheck(user.getPassword(), selectUser.getPassword())
//        List<Video> videoList = videoMapper.selectList(null);
//        for (Video video : videoList) {
//            if (DigestUtil.bcryptCheck(video.getVideoUrl(), key)) {
//                return cosConfig.getBucketCdnDomain() + video.getVideoUrl();
//            }
//        }
//        return null;
//    }
//
//    @Override
//    public List<ResBannerVideoVo> getBannerVideos(List<String> categories) {
//        StringBuilder categoryIdPath = new StringBuilder();
//        for (int i = 0; i < categories.size(); i++) {
//            VideoCategory videoCategory = videoCategoryMapper.selectOne(new QueryWrapper<VideoCategory>().eq("name", categories.get(i)));
//            if (i == categories.size() - 1) {
//                categoryIdPath.append(videoCategory.getId());
//            } else {
//                categoryIdPath.append(videoCategory.getId()).append("/");
//            }
//        }
//        String categoryPath = categoryIdPath.toString();
//        Long categoryId = videoCategoryMapper.selectOne(new QueryWrapper<VideoCategory>().eq("name", categories.get(categories.size() - 1))).getId();
//        List<Video> videos = videoMapper.selectList(new QueryWrapper<Video>().eq("category_id", categoryId).orderByAsc("id"));
//        List<Video> tmpVideos = videos.stream().filter(video -> {
//            String path = videoCategoryMapper.selectById(video.getCategoryId()).getPath();
//            return path.equals(categoryPath);
//        }).limit(5).collect(Collectors.toList());
//        ArrayList<List<String>> tagList = new ArrayList<>();
//        for (Video video : tmpVideos) {
//            List<String> tags = videoTagMapper.selectList(new QueryWrapper<VideoTag>().eq("video_id", video.getId())).stream().map(VideoTag::getName).collect(Collectors.toList());
//            tagList.add(tags);
//        }
//        return tmpVideos.stream().map(video -> {
//            ResBannerVideoVo resBannerVideoVo = new ResBannerVideoVo();
//            resBannerVideoVo.setTitle(video.getTitle());
//            resBannerVideoVo.setVideoUrl(DigestUtil.bcrypt(video.getVideoUrl()));
//            resBannerVideoVo.setCoverUrl(cosConfig.getBucketCdnDomain() + video.getCoverUrl());
//            resBannerVideoVo.setTags(tagList.get(tmpVideos.indexOf(video)));
//            try {
//                resBannerVideoVo.setDuration(VideoUtil.getVideoDuration(fileBean.getOriginUrl(video.getVideoUrl())));
//                resBannerVideoVo.setCoverThemeColor(ColorUtil.getThemeColor(new URL(cosConfig.getBucketOriginDomain() + video.getCoverUrl())));
//            } catch (Exception e) {
//                e.printStackTrace();
//                log.error(e.getMessage());
//                throw new SystemException(CommonMsgCode.MSG_ERR);
//            }
//            return resBannerVideoVo;
//        }).collect(Collectors.toList());
//    }
//
//    @Override
//    public List<ResBaseVideoVo> getCategoryVideos(List<String> categories) {
//        // 通过分类名数组获取分类id路径字符串
//        StringBuilder categoryIdPath = new StringBuilder();
//        for (int i = 0; i < categories.size(); i++) {
//            VideoCategory videoCategory = videoCategoryMapper.selectOne(new QueryWrapper<VideoCategory>().eq("name", categories.get(i)));
//            if (i == categories.size() - 1) {
//                categoryIdPath.append(videoCategory.getId());
//            } else {
//                categoryIdPath.append(videoCategory.getId()).append("/");
//            }
//        }
//        String categoryPath = categoryIdPath.toString();
//        // 获取最低级分类的id
//        Long categoryId = videoCategoryMapper.selectOne(new QueryWrapper<VideoCategory>().eq("name", categories.get(categories.size() - 1))).getId();
//        // 通过最低级分类id获取所有视频
//        List<Video> videos = videoMapper.selectList(new QueryWrapper<Video>().eq("category_id", categoryId).orderByAsc("id"));
//        // 判断所有视频中对应的分类id中的分类路径是否与前端传入的分类路径字符串相同
//        List<Video> tmpVideos = videos.stream().filter(video -> {
//            String path = videoCategoryMapper.selectById(video.getCategoryId()).getPath();
//            return path.equals(categoryPath);
//        }).limit(48).collect(Collectors.toList());
//        ArrayList<List<String>> tagList = new ArrayList<>();
//        for (Video video : tmpVideos) {
//            List<String> tags = videoTagMapper.selectList(new QueryWrapper<VideoTag>().eq("video_id", video.getId())).stream().map(VideoTag::getName).collect(Collectors.toList());
//            tagList.add(tags);
//        }
//        return tmpVideos.stream().map(video -> {
//            ResBaseVideoVo resVideoVo = new ResBaseVideoVo();
//            resVideoVo.setTitle(video.getTitle());
//            resVideoVo.setVideoUrl(DigestUtil.bcrypt(video.getVideoUrl()));
//            resVideoVo.setCoverUrl(cosConfig.getBucketCdnDomain() + video.getCoverUrl());
//            resVideoVo.setTags(tagList.get(tmpVideos.indexOf(video)));
//            try {
//                resVideoVo.setDuration(VideoUtil.getVideoDuration(fileBean.getOriginUrl(video.getVideoUrl())));
//            } catch (Exception e) {
//                e.printStackTrace();
//                log.error(e.getMessage());
//                throw new SystemException(CommonMsgCode.MSG_ERR);
//            }
//            return resVideoVo;
//        }).collect(Collectors.toList());
//    }
//
//    @Override
//    public List<ResBaseVideoVo> getMoreVideos(List<String> categories) {
//        StringBuilder categoryIdPath = new StringBuilder();
//        for (int i = 0; i < categories.size(); i++) {
//            VideoCategory videoCategory = videoCategoryMapper.selectOne(new QueryWrapper<VideoCategory>().eq("name", categories.get(i)));
//            if (i == categories.size() - 1) {
//                categoryIdPath.append(videoCategory.getId());
//            } else {
//                categoryIdPath.append(videoCategory.getId()).append("/");
//            }
//        }
//        String categoryPath = categoryIdPath.toString();
//        Long categoryId = videoCategoryMapper.selectOne(new QueryWrapper<VideoCategory>().eq("name", categories.get(categories.size() - 1))).getId();
//        // 通过最低级分类id获取所有视频
//        List<Video> videos = videoMapper.selectList(new QueryWrapper<Video>().eq("category_id", categoryId).orderByDesc("id"));
//        // 判断所有视频中对应的分类id中的分类路径是否与前端传入的分类路径字符串相同
//        List<Video> tmpVideos = videos.stream().filter(video -> {
//            String path = videoCategoryMapper.selectById(video.getCategoryId()).getPath();
//            return path.equals(categoryPath);
//        }).limit(12).collect(Collectors.toList());
//        ArrayList<List<String>> tagList = new ArrayList<>();
//        for (Video video : tmpVideos) {
//            List<String> tags = videoTagMapper.selectList(new QueryWrapper<VideoTag>().eq("video_id", video.getId())).stream().map(VideoTag::getName).collect(Collectors.toList());
//            tagList.add(tags);
//        }
//        return tmpVideos.stream().map(video -> {
//            ResBaseVideoVo resVideoVo = new ResBaseVideoVo();
//            resVideoVo.setTitle(video.getTitle());
//            resVideoVo.setVideoUrl(DigestUtil.bcrypt(video.getVideoUrl()));
//            resVideoVo.setCoverUrl(cosConfig.getBucketCdnDomain() + video.getCoverUrl());
//            resVideoVo.setTags(tagList.get(tmpVideos.indexOf(video)));
//            try {
//                resVideoVo.setDuration(VideoUtil.getVideoDuration(fileBean.getOriginUrl(video.getVideoUrl())));
//            } catch (Exception e) {
//                e.printStackTrace();
//                log.error(e.getMessage());
//                throw new SystemException("获取视频信息失败");
//            }
//            return resVideoVo;
//        }).collect(Collectors.toList());
//    }
//
//    @Override
//    public ResPage<ResBaseVideoVo> getAllPageVideos(Long page, Long size) {
//        Page<Video> videoPage = videoMapper.selectPage(new Page<>(page, size), null);
//        ArrayList<List<String>> tagList = new ArrayList<>();
//        for (Video video : videoPage.getRecords()) {
//            List<String> tags = videoTagMapper.selectList(new QueryWrapper<VideoTag>().eq("video_id", video.getId())).stream().map(VideoTag::getName).collect(Collectors.toList());
//            tagList.add(tags);
//        }
//        List<ResBaseVideoVo> resItems = videoPage.getRecords().stream().map(video -> {
//            ResBaseVideoVo resBaseVideoVo = new ResBaseVideoVo();
//            resBaseVideoVo.setTitle(video.getTitle());
//            resBaseVideoVo.setVideoUrl(DigestUtil.bcrypt(video.getVideoUrl()));
//            resBaseVideoVo.setCoverUrl(cosConfig.getBucketCdnDomain() + video.getCoverUrl());
//            resBaseVideoVo.setTags(tagList.get(videoPage.getRecords().indexOf(video)));
//            try {
//                resBaseVideoVo.setDuration(VideoUtil.getVideoDuration(fileBean.getOriginUrl(video.getVideoUrl())));
//            } catch (Exception e) {
//                e.printStackTrace();
//                log.error(e.getMessage());
//                throw new SystemException("获取视频信息失败");
//            }
//            return resBaseVideoVo;
//        }).collect(Collectors.toList());
//        ResPage<ResBaseVideoVo> resPage = new ResPage<>();
//        resPage.setCurrentPage(videoPage.getCurrent());
//        resPage.setPageSize(videoPage.getSize());
//        resPage.setTotalPage(videoPage.getTotal());
//        resPage.setHasPrevious(videoPage.hasPrevious());
//        resPage.setHasNext(videoPage.hasNext());
//        resPage.setItems(resItems);
//        return resPage;
//    }
//

}
