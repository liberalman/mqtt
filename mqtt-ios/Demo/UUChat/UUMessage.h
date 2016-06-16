//
//  UUMessage.h
//  UUChatDemoForTextVoicePicture
//
//  Created by shake on 14-8-26.
//  Copyright (c) 2014年 uyiuyao. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef NS_ENUM(NSInteger, MessageType) {
    UUMessageTypeText       = 0 , // 文字
    UUMessageTypePicture    = 1 , // 图片
    UUMessageTypeVoice      = 2 , // 语音
    UUMessageTypeVisitCard  = 42, // 名片
    UUMessageTypeVideo      = 43, // 视频
    UUMessageTypeEmoji      = 47, // 动画表情
    UUMessageTypeLocation   = 48, // 非实时位置信息
    UUMessageTypeShare      = 49, // 分享链接
    UUMessageTypePhone      = 50, // 电话记录
    UUMessageTypeSmallVideo = 62, // 小视频
};
/*
 其中Type字段若为1，则该信息是文本信息
 Type为3，则该信息是图片

 若字段Type为34，则该信息是语音片段

 */

typedef NS_ENUM(NSInteger, MessageFrom) {
    UUMessageFromMe    = 0,   // 自己发的
    UUMessageFromOther = 1    // 别人发得
};


@interface UUMessage : NSObject

@property (nonatomic, copy) NSString *strIcon;
@property (nonatomic, copy) NSString *strId;
@property (nonatomic, copy) NSString *strTime;
@property (nonatomic, copy) NSString *strName;

@property (nonatomic, copy) NSString *strContent;
@property (nonatomic, copy) UIImage  *picture;
@property (nonatomic, copy) NSData   *voice;
@property (nonatomic, copy) NSString *strVoiceTime;

@property (nonatomic, assign) MessageType type;
@property (nonatomic, assign) MessageFrom from;

@property (nonatomic, assign) BOOL showDateLabel;

- (void)setWithDict:(NSDictionary *)dict;

- (void)minuteOffSetStart:(NSString *)start end:(NSString *)end;

@end
