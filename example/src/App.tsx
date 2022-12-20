import * as React from 'react';
import { useState } from 'react';

import {
  StyleSheet,
  View,
  Text,
  Image,
  TouchableWithoutFeedback,
  Platform,
} from 'react-native';
import { CosUploadServer } from 'react-native-cos-upload';
import * as ImagePicker from 'expo-image-picker';

CosUploadServer.init('ap-nanjing', 'jy-1306780729', 'sunny/audio/');

export default function App() {
  // const [result, setResult] = useState<number | undefined>();
  const [imageUrl, setImageUrl] = useState<string>();
  React.useEffect(() => {
    console.log(CosUploadServer);
  }, []);

  const queryStatus = async (isSelect = false) => {
    if (Platform.OS !== 'web') {
      const { status } = isSelect
        ? await ImagePicker.requestMediaLibraryPermissionsAsync()
        : await ImagePicker.requestCameraPermissionsAsync();
      if (status !== 'granted') {
        let pathName = '请先打开手机“设置”-“隐私”-“照片”-“' + 'AppName' + '”';
        if (Platform.OS === 'android') {
          pathName =
            '请在“设置”-“应用”-“' + 'AppName' + '”-“权限”中开启相机权限';
        }
        console.log(pathName);
        return false;
      }
      return true;
    }
    return true;
  };

  const selectPhone = async () => {
    const isGranted = await queryStatus(true);
    if (!isGranted) return;
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      allowsEditing: true,
      aspect: [1, 1],
      quality: 0.5,
    });
    upload(result);
  };
  const upload = (result: ImagePicker.ImagePickerResult) => {
    if (!result.canceled && result.assets) {
      // setIsUpload(true);
      const picUri = result.assets[0]?.uri.split('file:/')[1];
      const picName = result.assets[0]?.uri.split('/').pop();
      if (!picUri || !picName) return;
      CosUploadServer.uploadFile(
        {
          tmpSecretId:
            'AKIDCT4Gu-W-HMaqcEX3oBzbfnSc7sb3qH9d4K0_BIFbjmOjGWHzxSbmxeF4-SSCph_0',
          tmpSecretKey: 'LwgWRyVuiBUjk3nowiPRwHSf5L/puqEiygsVDakMIJ8=',
          sessionToken:
            '5QcfAlo9dqOsQKzBv6PqmJs23f6hXCda4bd601243effdf647c4868224608dffekIT8EpcGk_PB_RN5yh-iSvdlIlFPuYpjtfyHk1lW02VLY9E8XdCVQVnvKmKsZYI_uxH3IY9EagKQwH7aVSfPNLNXNOlqR-tFWipr1tKIutFFUA-i5bbfcNO1gYTLBq_qGAj22xIs5jGqoQCFSiu2R4ICpjjKRF-8PQPi3_IV4FE2ExVTi1t7ZGRbhRzeNXiJQaxs_4ZifEx8OCI_whwkaLAde7m6GB_tLbfLb6f76RURcYN5Uqscnl5VtwKA4yuoZ5bQFGkSwENILy-M4K-_l_76z6_iek3fwk3r0JnNmVE3ziWv5DtAJP5o78044kU_AtrVeVrsd46iwNTzwk_J4qccsTY22QHXy8X39hRYZON80sISlI9BUDJAROLhvSDrqYYOQWOiYk5wMpqkB7OWLG_afnHXUrsY4p5jkJd7s0Wqd5aH7e0qVTXQgI5AuWfGGtWyK1XZaqMa2NjFvY65lkxVqvgQyG8PlQ6XXz2_l3Ucd-wt0niiEZDNrTi31WdnRyaOaOw7D2JLfWneqKvxjf8vMzMzM9I_u8oSJ0okX0COeLJTbjA2DcOY9YjsXLa3',
          startTime: 1671520844,
          expiredTime: 1671522644,
        },
        picUri,
        picName
      )
        .then((uploadResult: string) => {
          // setIsUpload(false);
          setImageUrl(uploadResult);
        })
        .catch((err: any) => {
          console.log(err);
        });
    }
  };

  const onChangeHeaderImage = () => {
    // showPhotoModal();
    selectPhone().then(() => {});
  };

  return (
    <View style={styles.container}>
      {/* <Text>Result: {result}</Text> */}
      <View
        style={{
          width: '100%',
          height: 320,
          backgroundColor: 'white',
        }}
      >
        <TouchableWithoutFeedback onPress={onChangeHeaderImage}>
          {imageUrl ? (
            <Image
              style={{ width: '100%', height: '100%' }}
              source={{ uri: imageUrl }}
            />
          ) : (
            <View
              style={{
                width: '100%',
                height: '100%',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <Text>+</Text>
              {/* <Image
                style={{ width: tSize(50), height: tSize(50) }}
              /> */}
            </View>
          )}
        </TouchableWithoutFeedback>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
