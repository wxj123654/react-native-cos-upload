package com.cosupload;

import androidx.annotation.NonNull;

import com.cosupload.cos.CosServer;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;

@ReactModule(name = CosUploadModule.NAME)
public class CosUploadModule extends ReactContextBaseJavaModule {
  public static final String NAME = "CosUpload";
  private final ReactApplicationContext mContext;
  private final CosServer cosServer;

  public CosUploadModule(ReactApplicationContext reactContext) {
    super(reactContext);
    mContext = reactContext;
    this.cosServer = new CosServer();
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }


  // // Example method
  // // See https://reactnative.dev/docs/native-modules-android
  // @ReactMethod
  // public void multiply(double a, double b, Promise promise) {
  //   promise.resolve(a * b);
  // }
      /**
     * 上传文件
     *
     */
    @ReactMethod
    public void uploadFile(String url,String picName,Promise promise) {
        this.cosServer.upload(mContext,promise,url,picName);
    }
    /**
     * 初始化
     *
     */
    @ReactMethod
    public void init(String region, String bucket, String baseDir) {
        this.cosServer.init(region,bucket,baseDir);
    }
    /**
     * 设置零时密钥
     *
     */
    @ReactMethod
    public void setTempSecret(String tmpSecretId, String tmpSecretKey, String sessionToken, Integer startTime, Integer expiredTime) {
      this.cosServer.setTempSecret(tmpSecretId,tmpSecretKey,sessionToken,Long.valueOf(startTime),Long.valueOf(expiredTime));
    }
}
