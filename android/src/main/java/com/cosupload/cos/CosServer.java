package com.cosupload.cos;

import org.jetbrains.annotations.Nullable;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlProgressListener;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.transfer.COSXMLUploadTask;
import com.tencent.cos.xml.transfer.TransferConfig;
import com.tencent.cos.xml.transfer.TransferManager;
import com.tencent.cos.xml.transfer.TransferState;
import com.tencent.cos.xml.transfer.TransferStateListener;
import com.tencent.qcloud.core.auth.QCloudCredentialProvider;

import java.io.File;

public class CosServer {

    private  MySessionCredentialProvider mySessionCredentialProvider;
    private String region;
    private String bucket;
    private String baseDir;

    public CosServer() {
        this.mySessionCredentialProvider = new MySessionCredentialProvider();
    }
    public void setTempSecret(String tmpSecretId, String tmpSecretKey, String sessionToken, long startTime, long expiredTime ) {
       this.mySessionCredentialProvider.init(tmpSecretId,tmpSecretKey,sessionToken,startTime,expiredTime);
    }
    public void init(String region, String bucket, String baseDir) {
      this.region = region;
      this.bucket = bucket;
      this.baseDir = baseDir;
    }
    public void upload(ReactApplicationContext context, Promise promise,String url,String picName) {
        QCloudCredentialProvider myCredentialProvider = this.mySessionCredentialProvider;
        // 存储桶所在地域简称，例如广州地区是 ap-guangzhou
        String region = this.region;

        // 创建 CosXmlServiceConfig 对象，根据需要修改默认的配置参数
        CosXmlServiceConfig serviceConfig = new CosXmlServiceConfig.Builder()
                .setRegion(region)
                .isHttps(true) // 使用 HTTPS 请求, 默认为 HTTP 请求
                .builder();

        // 初始化 COS Service，获取实例
        CosXmlService cosXmlService = new CosXmlService(context,
                serviceConfig, myCredentialProvider);

        // 初始化 TransferConfig，这里使用默认配置，如果需要定制，请参考 SDK 接口文档
        TransferConfig transferConfig = new TransferConfig.Builder().build();
        // 初始化 TransferManager
        TransferManager transferManager = new TransferManager(cosXmlService,
                transferConfig);

        // 存储桶名称，由bucketname-appid 组成，appid必须填入，可以在COS控制台查看存储桶名称。 https://console.cloud.tencent.com/cos5/bucket
        String bucket = this.bucket;
        String cosPath = this.baseDir + picName; //对象在存储桶中的位置标识符，即称对象键
//        String srcPath = new File(context.getCacheDir(), url)
//                .toString(); //本地文件的绝对路径
        String srcPath = url;
        //若存在初始化分块上传的 UploadId，则赋值对应的 uploadId 值用于续传；否则，赋值 null
        String uploadId = null;

        // 上传文件
        COSXMLUploadTask cosxmlUploadTask = transferManager.upload(bucket, cosPath,
                srcPath, uploadId);

        //设置上传进度回调
        cosxmlUploadTask.setCosXmlProgressListener(new CosXmlProgressListener() {
            @Override
            public void onProgress(long complete, long target) {
                // todo Do something to update progress...
            }
        });
        //设置返回结果回调
        cosxmlUploadTask.setCosXmlResultListener(new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                COSXMLUploadTask.COSXMLUploadTaskResult uploadResult =
                        (COSXMLUploadTask.COSXMLUploadTaskResult) result;
                promise.resolve(uploadResult.accessUrl);
            }

            // 如果您使用 kotlin 语言来调用，请注意回调方法中的异常是可空的，否则不会回调 onFail 方法，即：
            // clientException 的类型为 CosXmlClientException?，serviceException 的类型为 CosXmlServiceException?
            @Override
            public void onFail(CosXmlRequest request,
                               @Nullable CosXmlClientException clientException,
                               @Nullable CosXmlServiceException serviceException) {
                if (clientException != null) {
                    clientException.printStackTrace();
                    promise.reject(clientException);
                } else {
                    promise.reject(serviceException);
                    serviceException.printStackTrace();
                }
            }
        });
        //设置任务状态回调, 可以查看任务过程
        cosxmlUploadTask.setTransferStateListener(new TransferStateListener() {
            @Override
            public void onStateChanged(TransferState state) {
                // todo notify transfer state
            }
        });
    }
}
