package com.cosupload.cos;

import android.util.Log;
import android.widget.Toast;

import com.tencent.qcloud.core.auth.BasicLifecycleCredentialProvider;
import com.tencent.qcloud.core.auth.QCloudLifecycleCredentials;
import com.tencent.qcloud.core.auth.SessionQCloudCredentials;
import com.tencent.qcloud.core.common.QCloudClientException;

public class MySessionCredentialProvider
        extends BasicLifecycleCredentialProvider {

    public static String tmpSecretId;
    public static String tmpSecretKey;
    public static String sessionToken;
    public static long startTime;
    public static long expiredTime;

    public MySessionCredentialProvider() {

    }

    public void init(String tmpSecretId, String tmpSecretKey, String sessionToken, long startTime, long expiredTime){
        MySessionCredentialProvider.tmpSecretId = tmpSecretId;
        MySessionCredentialProvider.tmpSecretKey = tmpSecretKey;
        MySessionCredentialProvider.sessionToken = sessionToken;
        MySessionCredentialProvider.startTime = startTime;
        MySessionCredentialProvider.expiredTime = expiredTime;
    }

    @Override
    protected QCloudLifecycleCredentials fetchNewCredentials()
            throws QCloudClientException {

        // 首先从您的临时密钥服务器获取包含了密钥信息的响应

        // 然后解析响应，获取临时密钥信息
        String tmpSecretId = MySessionCredentialProvider.tmpSecretId; // 临时密钥 SecretId
//        Log.e("tmpSecretId",tmpSecretId);
        String tmpSecretKey = MySessionCredentialProvider.tmpSecretKey; // 临时密钥 SecretKey
        String sessionToken = MySessionCredentialProvider.sessionToken; // 临时密钥 Token
        long expiredTime = MySessionCredentialProvider.expiredTime;//临时密钥有效截止时间戳，单位是秒

        //建议返回服务器时间作为签名的开始时间，避免由于用户手机本地时间偏差过大导致请求过期
        // 返回服务器时间作为签名的起始时间
        long startTime = MySessionCredentialProvider.startTime; //临时密钥有效起始时间，单位是秒

        // 最后返回临时密钥信息对象
        return new SessionQCloudCredentials(tmpSecretId, tmpSecretKey,
                sessionToken, startTime, expiredTime);
    }
}
