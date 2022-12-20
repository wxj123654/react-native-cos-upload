
#import <QCloudCOSXML/QCloudCOSXML.h>
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNCosUploadSpec.h"

@interface CosUpload : NSObject <NativeCosUploadSpec>
#else
#import <React/RCTBridgeModule.h>

@interface CosUpload : NSObject <RCTBridgeModule>
#endif


@end
