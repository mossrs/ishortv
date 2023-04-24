package com.mossflower.ishortv_common.util;

import com.mossflower.ishortv_common.constant.CosConstant;
import com.mossflower.ishortv_common.constant.QcloudConstant;
import com.mossflower.ishortv_common.dto.ResCosSecretDto;
import com.mossflower.ishortv_common.exception.SystemException;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.BasicSessionCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.TransferManagerConfiguration;
import com.tencent.cloud.CosStsClient;
import com.tencent.cloud.Response;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author z's'b
 * @date 2023/3/4 星期六 17:24
 * @description
 */
public class CosUtil {


    private static COSClient createCOSClient() {
        COSCredentials cred = new BasicCOSCredentials(QcloudConstant.SECRET_ID, QcloudConstant.SECRET_KEY);
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setRegion(new Region(CosConstant.REGION));
        clientConfig.setHttpProtocol(HttpProtocol.https);
        // 设置 socket 读取超时，默认 30s
        clientConfig.setSocketTimeout(30 * 1000);
        // 设置建立连接超时，默认 30s
        clientConfig.setConnectionTimeout(30 * 1000);
        return new COSClient(cred, clientConfig);
    }

    private static COSClient createTmpCOSClient() throws IOException {
        ResCosSecretDto cosSecret = getCosSecret();
        BasicSessionCredentials cred = new BasicSessionCredentials(
                cosSecret.getCredentials().getTmpSecretId(),
                cosSecret.getCredentials().getTmpSecretKey(),
                cosSecret.getCredentials().getSessionToken());
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setRegion(new Region(CosConstant.REGION));
        clientConfig.setHttpProtocol(HttpProtocol.https);
        // 设置 socket 读取超时，默认 30s
        clientConfig.setSocketTimeout(30 * 1000);
        // 设置建立连接超时，默认 30s
        clientConfig.setConnectionTimeout(30 * 1000);
        return new COSClient(cred, clientConfig);
    }

    private static void closeCOSClient(COSClient cosClient) {
        cosClient.shutdown();
    }

    // 创建 TransferManager 实例，这个实例用来后续调用高级接口
    private static TransferManager createTransferManager() {
        COSClient cosClient = createCOSClient();
        ExecutorService threadPool = Executors.newFixedThreadPool(32);
        TransferManager transferManager = new TransferManager(cosClient, threadPool);
        // 分块上传阈值和分块大小分别为 5MB 和 1MB
        TransferManagerConfiguration transferManagerConfiguration = new TransferManagerConfiguration();
        transferManagerConfiguration.setMultipartUploadThreshold(5 * 1024 * 1024);
        transferManagerConfiguration.setMinimumUploadPartSize(1024 * 1024);
        transferManager.setConfiguration(transferManagerConfiguration);
        return transferManager;
    }

    private static void closeTransferManager(TransferManager transferManager) {
        transferManager.shutdownNow();
    }

    public static ResCosSecretDto getCosSecret() throws IOException {
        TreeMap<String, Object> config = new TreeMap<>();
        config.put("secretId", QcloudConstant.SECRET_ID);
        config.put("secretKey", QcloudConstant.SECRET_KEY);
        config.put("durationSeconds", QcloudConstant.TMP_SECRET_EXPIRE);
        config.put("bucket", CosConstant.BUCKET_NAME);
        config.put("region", CosConstant.REGION);
        config.put("allowPrefixes", new String[]{"*"});
        String[] allowActions = new String[]{"*"};
        config.put("allowActions", allowActions);
        Response response = CosStsClient.getCredential(config);
        ResCosSecretDto resCosSecretVo = new ResCosSecretDto();
        resCosSecretVo.setStartTime(response.startTime);
        resCosSecretVo.setExpiredTime(response.expiredTime);
        ResCosSecretDto.Credentials credentials = new ResCosSecretDto.Credentials();
        credentials.setTmpSecretId(response.credentials.tmpSecretId);
        credentials.setTmpSecretKey(response.credentials.tmpSecretKey);
        credentials.setSessionToken(response.credentials.sessionToken);
        resCosSecretVo.setCredentials(credentials);
        return resCosSecretVo;
    }

    /**
     * 返回预签名url 用来转码
     *
     * @param key    文件key
     * @param expire 过期时间 单位毫秒
     * @return 预签名url
     */
    public static String getOriginSignUrl(String key, Long expire) throws IOException {
        COSClient cosClient = createTmpCOSClient();
        Date expirationDate = new Date(System.currentTimeMillis() + expire);
        HttpMethodName method = HttpMethodName.GET;
        URL url = cosClient.generatePresignedUrl(CosConstant.BUCKET_NAME, key, expirationDate, method);
        cosClient.shutdown();
        return url.toString();
    }

    public static String getOriginUrl(String key) {
        return CosConstant.DOMAIN + "/" + key;
    }

    /**
     * 获取cos存储桶某目录下里的全部文件
     * 存储桶结构
     * video 目录 存放视频文件
     * 每个视频是一个文件夹 包含一个.m3u8文件和多个.ts文件
     * 返回的是每个视频的.m3u8文件key
     * image 目录 存放图片文件
     * 返回的是每个图片的key
     * <p>
     *
     * @return List<String> 文件列表
     */
    public static List<COSObjectSummary> getFileList(String prefix) throws CosClientException, IOException {
        COSClient cosClient = createTmpCOSClient();
        ArrayList<COSObjectSummary> list = new ArrayList<>();
        // 获取 bucket 下成员（设置 delimiter）
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        listObjectsRequest.setBucketName(CosConstant.BUCKET_NAME);
        // 设置 list 的 prefix, 表示 list 出来的文件 key 都是以这个 prefix 开始
        listObjectsRequest.setPrefix(prefix);//注意，这里是一个其实目录，可以是根目录/也可以是自定义目录前缀
        // 设置 delimiter 为/, 即获取的是直接成员，不包含目录下的递归子成员
        listObjectsRequest.setDelimiter("");
        // 设置最多 list 100个成员,（如果不设置, 默认为1000个，最大允许一次 list 1000个 key）
        listObjectsRequest.setMaxKeys(1000);
        ObjectListing objectListing;
        do {
            objectListing = cosClient.listObjects(listObjectsRequest);
            List<COSObjectSummary> objectSummaries = objectListing.getObjectSummaries();
            list.addAll(objectSummaries);
            // 设置 marker, (marker 由上一次 list 获取到, 或者第一次 list marker 为空)
            listObjectsRequest.setMarker(objectListing.getNextMarker());
        } while (objectListing.isTruncated());
        closeCOSClient(cosClient);
        return list;
    }

    /**
     * @param key 就是在存储桶里的相对路径
     * @throws CosClientException  COS客户端异常
     * @throws CosServiceException COS服务端异常
     */
    public static void deleteFile(String key) throws CosClientException, IOException {
        COSClient cosClient = createTmpCOSClient();
        cosClient.deleteObject(CosConstant.BUCKET_NAME, key);
        closeCOSClient(cosClient);
    }

    /**
     * @param prefix 就是在存储桶里的相对路径 即删除的目录
     */
    public static void deleteDir(String prefix) throws CosClientException, IOException {
        COSClient cosClient = createTmpCOSClient();
        List<COSObjectSummary> cosObjectSummaries = getFileList(prefix);
        ArrayList<DeleteObjectsRequest.KeyVersion> delObjects = new ArrayList<>();
        for (COSObjectSummary cosObjectSummary : cosObjectSummaries) {
            delObjects.add(new DeleteObjectsRequest.KeyVersion(cosObjectSummary.getKey()));
        }
        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(CosConstant.BUCKET_NAME);
        deleteObjectsRequest.setKeys(delObjects);
        DeleteObjectsResult deleteObjectsResult = cosClient.deleteObjects(deleteObjectsRequest);
        closeCOSClient(cosClient);
        List<DeleteObjectsResult.DeletedObject> deletedObjects = deleteObjectsResult.getDeletedObjects();
        if (deletedObjects.size() != cosObjectSummaries.size()) {
            throw new SystemException("删除失败");
        }
    }

    /**
     * @param key 就是在存储桶里的相对路径
     * @throws CosClientException  COS客户端异常
     * @throws CosServiceException COS服务端异常
     */
    public static Boolean isExist(String key) throws CosClientException, IOException {
        COSClient cosClient = createTmpCOSClient();
        boolean exist = cosClient.doesObjectExist(CosConstant.BUCKET_NAME, key);
        closeCOSClient(cosClient);
        return exist;
    }

    /**
     * 上传分块 视频文件
     *
     * @param file 视频文件
     * @param key  文件路径
     */
    public static void uploadPart(File file, String key) throws IOException, InterruptedException {
        COSClient cosClient = createTmpCOSClient();
        // 读取文件字节
        byte[] fileByte = Files.readAllBytes(file.toPath());
        // 计算文件总大小
        long totalSize = fileByte.length;
        // 设置分块大小：1M
        byte[] data = new byte[1024 * 1024 * 10];
        int batchSize = data.length;
        // 计算分块数
        long batch = totalSize / batchSize + (totalSize % batchSize > 0 ? 1 : 0);
        //初始化分块上传
        InitiateMultipartUploadRequest initiateMultipartUploadRequest = new InitiateMultipartUploadRequest(CosConstant.BUCKET_NAME, key);
        InitiateMultipartUploadResult initiateMultipartUploadResult = cosClient.initiateMultipartUpload(initiateMultipartUploadRequest);
        //文件分块
        List<PartETag> partETagList = new ArrayList<>();
        Map<Integer, InputStream> uploadPart = new HashMap<>();
        for (int i = 0; i < batch; i++) {
            // 如果是最后一个分块，需要重新计算分块大小
            long partSize = batchSize;
            if (i == batch - 1) {
                partSize = totalSize - (long) i * batchSize;
            }
            int from = i * batchSize;
            int to = (int) partSize + (i * batchSize);
            //文件分块
            byte[] partByte = Arrays.copyOfRange(fileByte, from, to);
            InputStream input = new ByteArrayInputStream(partByte);
            uploadPart.put(i + 1, input);
        }
        final CountDownLatch latch = new CountDownLatch((int) batch);
        //多线程上传分块文件
        ExecutorService pool = Executors.newFixedThreadPool(2);
        uploadPart.forEach((k, v) -> pool.submit(() -> {
            PartETag partETag = uploadPartProcess(initiateMultipartUploadResult.getUploadId(), key, v, k);
            partETagList.add(partETag);
            //让latch中的数值减一
            latch.countDown();
            return true;
        }));
        //主线程
        //阻塞当前线程直到latch中数值为零才执行
        latch.await();
        //实现完成整个分块上传
        CompleteMultipartUploadRequest completeMultipartUploadRequest = new CompleteMultipartUploadRequest(CosConstant.BUCKET_NAME, key, initiateMultipartUploadResult.getUploadId(), partETagList);
        cosClient.completeMultipartUpload(completeMultipartUploadRequest);
        closeCOSClient(cosClient);
    }

    private static PartETag uploadPartProcess(String uploadId, String key, InputStream input, int partNumber) throws IOException {
        if (partNumber > 10000) {
            throw new RuntimeException("分块上传失败");
        }
        COSClient cosClient = createTmpCOSClient();
        //实现将对象按照分块的方式上传到 COS。最多支持10000分块，每个分块大小为1MB - 5GB，最后一个分块可以小于1MB。
        UploadPartRequest uploadPartRequest = new UploadPartRequest();
        uploadPartRequest.setUploadId(uploadId);
        uploadPartRequest.setInputStream(input);
        uploadPartRequest.setKey(key);
        uploadPartRequest.setPartSize(input.available());
        uploadPartRequest.setBucketName(CosConstant.BUCKET_NAME);
        uploadPartRequest.setPartNumber(partNumber);
        UploadPartResult uploadPartResult = cosClient.uploadPart(uploadPartRequest);
        closeCOSClient(cosClient);
        return new PartETag(partNumber, uploadPartResult.getETag());
    }

}
