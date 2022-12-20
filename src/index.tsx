import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-cos-upload' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const CosUpload = NativeModules.CosUpload
  ? NativeModules.CosUpload
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

// export function multiply(a: number, b: number): Promise<number> {
//   return CosUpload.multiply(a, b);
// }

interface ITempSecretInfo {
  tmpSecretId: string;
  tmpSecretKey: string;
  sessionToken: string;
  startTime: number;
  expiredTime: number;
}

export class CosUploadServer {
  private static isInit = false;
  static init(regionName: string, bucket: string, baseDir: string) {
    CosUpload.init(regionName, bucket, baseDir);
    this.isInit = true;
  }

  static uploadFile(
    // cos临时签名
    tempSecretInfo: ITempSecretInfo,
    fileUri: string,
    fileName: string
  ) {
    return new Promise<string>((resolve, reject) => {
      if (!this.isInit) {
        return reject('请调用 init 方法初始化cos配置');
      }
      CosUpload.setTempSecret(
        tempSecretInfo.tmpSecretId,
        tempSecretInfo.tmpSecretKey,
        tempSecretInfo.sessionToken,
        tempSecretInfo.startTime,
        tempSecretInfo.expiredTime
      );
      CosUpload.uploadFile(fileUri, fileName)
        .then((res: string) => {
          resolve(res);
        })
        .catch((err: any) => {
          reject(err);
        });
    });
  }
}
