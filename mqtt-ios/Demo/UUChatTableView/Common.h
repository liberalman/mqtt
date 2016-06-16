//
//  Common.h
//  UUChatTableView
//
//  Created by Liberty on 15/10/18.
//  Copyright © 2015年 uyiuyao. All rights reserved.
//
#import <UIKit/UIKit.h>
#import "MQTTKit/MQTTKit.h"
#import "Common.h"

#ifndef Common_h
#define Common_h

@protocol NetReqCallbackProtocol;

@interface Common : NSObject {
    //NSString *host;
    //NSString *unencrypt;
}
// create a property for the MQTTClient that is used to send and receive the message
//@property (nonatomic, strong) MQTTClient *mqttclient;
@property(nonatomic,assign) id<NetReqCallbackProtocol> delegate;
-(void)Login:(NSString*)username :(NSString*)password :(UINavigationController*)nav;
-(void)GET1:(NSString*)params;
-(int)GET:(NSString*)requestData :(NSDictionary*)ret;
-(int)GETNoTok:(NSString*)requestData :(NSDictionary*)ret;

+(void)sendMsg :(NSData*)data :(NSString*)topicOthers;
+(MQTTClient*) getMqtt;
+(NSNumber*) getUserId;
+(NSString*) getNickName;
+(NSString*) getApplyImageUrl;
+(NSString*) getAccessToken;
+(NSString*) getUnencrypt;
+(NSString*) getHost;

+ (NSString *) randFileName;
+ (NSString *) getFilePath : (NSString *) filename;
+ (UIImage *) imageCompressForWidth:(UIImage *)sourceImage targetWidth:(CGFloat)defineWidth;
@end

@protocol NetReqCallbackProtocol<NSObject>
@optional
-(void)loginCallback:(UINavigationController*)nav;
@end

#endif /* Common_h */
