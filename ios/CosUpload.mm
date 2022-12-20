#import "CosUpload.h"

@interface CosUpload()<QCloudSignatureProvider>

@property (nonatomic, copy) NSString* regionName; // 区域名称
@property (nonatomic, copy) NSString* bucket; // 桶名称
@property (nonatomic, copy) NSString* baseDir; // baseDir 上传到cos的那个目录下
@property (nonatomic, copy) NSString* tmpSecretId;
@property (nonatomic, copy) NSString* tmpSecretKey;
@property (nonatomic, copy) NSString* sessionToken;
@property (nonatomic, copy) NSNumber* startTime;
@property (nonatomic, copy) NSNumber* expiredTime;
@end
@implementation CosUpload
RCT_EXPORT_MODULE()

// Example method
// See // https://reactnative.dev/docs/native-modules-ios
// RCT_REMAP_METHOD(multiply,
//                  multiplyWithA:(double)a withB:(double)b
//                  withResolver:(RCTPromiseResolveBlock)resolve
//                  withRejecter:(RCTPromiseRejectBlock)reject)
// {
//     NSNumber *result = @(a * b);

//     resolve(result);
// }

RCT_EXPORT_METHOD(setTempSecret:(nonnull NSString *)tmpSecretId tmpSecretKey:(nonnull NSString *)tmpSecretKey sessionToken:( nonnull NSString *)sessionToken startTime:( nonnull NSNumber *)startTime expiredTime:( nonnull NSNumber *)expiredTime)
{
    self.tmpSecretId = tmpSecretId;
    self.tmpSecretKey = tmpSecretKey;
    self.sessionToken = sessionToken;
    self.startTime = startTime;
    self.expiredTime = expiredTime;
    [self initService];
}

RCT_EXPORT_METHOD(init:(nonnull NSString *)regionName bucket:(nonnull NSString *)bucket baseDir:( nonnull NSString *)baseDir )
{
    self.regionName = regionName;
    self.bucket = bucket;
    self.baseDir = baseDir;
}


- (BOOL)initService{

    QCloudServiceConfiguration* configuration = [QCloudServiceConfiguration new];
    QCloudCOSXMLEndPoint* endpoint = [[QCloudCOSXMLEndPoint alloc] init];

    // 替换为用户的 region，已创建桶归属的region可以在控制台查看，https://console.cloud.tencent.com/cos5/bucket
    // COS支持的所有region列表参见https://www.qcloud.com/document/product/436/6224
    // @"ap-nanjing"
    endpoint.regionName = self.regionName;
    // 使用 HTTPS
    endpoint.useHTTPS = true;
    configuration.endpoint = endpoint;
    // 密钥提供者为自己
    configuration.signatureProvider = self;
    // 初始化 COS 服务示例
    [QCloudCOSXMLService registerDefaultCOSXMLWithConfiguration:configuration];
    [QCloudCOSTransferMangerService registerDefaultCOSTransferMangerWithConfiguration:
        configuration];
    return YES;
}


// 获取签名的方法入口，这里演示了获取临时密钥并计算签名的过程
// 您也可以自定义计算签名的过程
- (void) signatureWithFields:(QCloudSignatureFields*)fileds
                     request:(QCloudBizHTTPRequest*)request
                  urlRequest:(NSMutableURLRequest*)urlRequst
                   compelete:(QCloudHTTPAuthentationContinueBlock)continueBlock
{
        //这里同步从后台服务器获取临时密钥，强烈建议将获取临时密钥的逻辑放在这里，最大程度上保证密钥的可用性
    //...
    QCloudCredential* credential = [QCloudCredential new];

    // 临时密钥 SecretId
    // sercret_id替换为用户的 SecretId，登录访问管理控制台查看密钥，https://console.cloud.tencent.com/cam/capi
    credential.secretID = self.tmpSecretId;
    // 临时密钥 SecretKey
    // sercret_key替换为用户的 SecretKey，登录访问管理控制台查看密钥，https://console.cloud.tencent.com/cam/capi
    credential.secretKey = self.tmpSecretKey;
    // 临时密钥 Token
    // 如果使用永久密钥不需要填入token，如果使用临时密钥需要填入，临时密钥生成和使用指引参见https://cloud.tencent.com/document/product/436/14048
    credential.token = self.sessionToken;
    /** 强烈建议返回服务器时间作为签名的开始时间, 用来避免由于用户手机本地时间偏差过大导致的签名不正确(参数startTime和expiredTime单位为秒)
    */
    credential.startDate = [NSDate dateWithTimeIntervalSince1970:[self.startTime doubleValue]]; // 单位是秒
    credential.expirationDate = [NSDate dateWithTimeIntervalSince1970:[self.expiredTime doubleValue]];// 单位是秒

    QCloudAuthentationV5Creator* creator = [[QCloudAuthentationV5Creator alloc]
        initWithCredential:credential];
    // 注意 这里不要对urlRequst 进行copy以及mutableCopy操作
    QCloudSignature *signature = [creator signatureForData:urlRequst];
    continueBlock(signature, nil);
}

RCT_EXPORT_METHOD(uploadFile:(NSString *)url picName:(NSString *)picName
                  resolver: (RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    QCloudCOSXMLUploadObjectRequest* put = [QCloudCOSXMLUploadObjectRequest new];
    // 本地文件路径
    NSURL* url2 = [NSURL fileURLWithPath:url];
    // 存储桶名称，由BucketName-Appid 组成，可以在COS控制台查看 https://console.cloud.tencent.com/cos5/bucket
    // @"jy-1306780729"
    put.bucket = self.bucket;
    // 对象键，是对象在 COS 上的完整路径，如果带目录的话，格式为 "video/xxx/movie.mp4"
    // @"sunny/audio/"
    put.object = [self.baseDir stringByAppendingString:picName];
    //需要上传的对象内容。可以传入NSData*或者NSURL*类型的变量
    put.body = url2;
    //监听上传进度
    [put setSendProcessBlock:^(int64_t bytesSent,
                                int64_t totalBytesSent,
                                int64_t totalBytesExpectedToSend) {
        //      bytesSent                 本次要发送的字节数（一个大文件可能要分多次发送）
        //      totalBytesSent            已发送的字节数
        //      totalBytesExpectedToSend  本次上传要发送的总字节数（即一个文件大小）
    }];

    //监听上传结果
    [put setFinishBlock:^(id outputObject, NSError *error) {
        //可以从 outputObject 中获取 response 中 etag 或者自定义头部等信息
        NSDictionary * result = (NSDictionary *)outputObject;
        if(error) {
          reject([error.userInfo valueForKey:@"Code"],[error.userInfo valueForKey:@"Message"],error);
        } else {
          NSString* url = [result valueForKey:@"_location"];
          resolve(url);
        }
    }];


    [[QCloudCOSTransferMangerService defaultCOSTransferManager] UploadObject:put];
}

// Don't compile this code when we build for the old architecture.
#ifdef RCT_NEW_ARCH_ENABLED
- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeCosUploadSpecJSI>(params);
}
#endif

@end
