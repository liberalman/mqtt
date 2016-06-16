//
//  Common.m
//  UUChatTableView
//
//  Created by Liberty on 15/10/18.
//  Copyright © 2015年 uyiuyao. All rights reserved.
//

#import "Common.h"
#import "ChatListViewController.h"
#import "RootViewController.h"
#import "O2.pb.h"
#import "UUMessage.h"
#import "ChatModel.h"

#define kMQTTServerHost @"123.56.107.158"

static MQTTClient *mqttclient;
static NSString * const host = @"http://interface.imouer.com:9090/o2offical/dallas.do";
static NSString *accessToken = @"";
static NSNumber *userId = 0;
static NSNumber *userType;
static NSNumber *gender;
static NSString *nickName = @"";
static NSString *applyImageUrl = @"";
static NSString *clientID;// = @"mosqpub/100078-iZ256cb32";
static NSString *myTopic;// = @"100078"; 101198-18002571326  116947-18002571322
//"qiniu_url" = "http://7vzu9s.com1.z0.glb.clouddn.com/";
//rongCloudToken = "QNCB4gxMSTGqyKB8KsBfw3CbzCcnu2+dUupBIaKOBt5Z/EqE9oz7SLqCc+1ZwuA2zo0Dtjo+kXb9DV0faRp/8w==";
static NSString *unencrypt;

@implementation Common
@synthesize delegate=_delegate;

-(id) init {
    if(self=[super init]){
        //host = @"http://interface.imouer.com:9090/o2offical/dallas.do";
        unencrypt = @"1";
    }
    return self;
}

+(MQTTClient*)getMqtt {
    return mqttclient;
}

+(NSString*) getNickName {
    return nickName;
}

+(NSString*) getApplyImageUrl {
    return applyImageUrl;
}

+(NSString*) getAccessToken {
    return accessToken;
}

+(NSString*) getUnencrypt {
    return unencrypt;
}

+(NSString*) getHost {
    return host;
}

+(NSNumber*) getUserId {
    return userId;
}

-(void)setHost:(NSString*)ahost setUnencrypt:(NSString*)flag {
    //host = ahost;
    unencrypt = flag;
}

-(NSString*)unencrypt {
    return unencrypt;
}

-(NSString*)host {
    return host;
}

-(NSNumber*)userId {
    return userId;
}

+ (NSString *) randFileName {
    NSDateFormatter* dateFormat = [[NSDateFormatter alloc] init];//实例化一个NSDateFormatter对象
    [dateFormat setDateFormat:@"yyyyMMddHHmmss"];//设定时间格式,这里可以设置成自己需要的格式
    int rand = arc4random() % 10000;
    NSString *filename = [[dateFormat stringFromDate:[NSDate date]] stringByAppendingFormat:(@"%d"), rand];
    return filename;
}

+ (NSString *) getFilePath : (NSString *) filename {
    //保存到对应的沙盒目录中，具体代码如下：
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory,NSUserDomainMask, YES);
    NSString *filePath = [[paths objectAtIndex:0] stringByAppendingPathComponent:filename];   // 保存文件的名称
    return filePath;
}

// 异步
-(void)GET1:(NSString*)requestData {
    NSDictionary *params = @{@"unencrypt" : unencrypt, @"requestData" : requestData};
    AFHTTPRequestOperationManager *manager = [AFHTTPRequestOperationManager manager];
    [manager GET:host parameters:params success:^(AFHTTPRequestOperation *operation, id responseObject) {
        NSLog(@"JSON: %@", responseObject);
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
        NSLog(@"Error: %@", error);
    }];
}

// 同步,无token
-(int)GETNoTok:(NSString*)requestData :(NSDictionary*)ret{
    NSMutableDictionary *requestParms = [[NSMutableDictionary alloc] init];
    [requestParms setObject:@"1" forKey:@"unencrypt"];
    [requestParms setObject:requestData forKey:@"requestData"];
    
    AFJSONRequestSerializer *requestSerializer = [AFJSONRequestSerializer serializer];
    NSMutableURLRequest *request = [requestSerializer requestWithMethod:@"POST" URLString:host parameters:requestParms error:nil];
    
    AFHTTPRequestOperation *requestOperation = [[AFHTTPRequestOperation alloc] initWithRequest:request];
    AFHTTPResponseSerializer *responseSerializer = [AFJSONResponseSerializer serializer];
    
    [requestOperation setResponseSerializer:responseSerializer];
    [requestOperation start];
    [requestOperation waitUntilFinished];
    
    NSDictionary *obj = [requestOperation responseObject];
    NSLog(@"JSON: %@", obj);
    NSString *status = [obj objectForKey:@"status"];
    if([status isEqual:@"0"]){ // success
        ret = [obj objectForKey:@"data"];
    } else {
        ret = [obj objectForKey:@"memo"];
        return [status intValue];
    }
    return 0;
}

// 同步
-(int)GET:(NSString*)data :(NSDictionary*)ret{
    NSString *requestData = [NSString stringWithFormat:@"%@ \"accessToken\":\"%@\"}", data, accessToken];
    NSMutableDictionary *requestParms = [[NSMutableDictionary alloc] init];
    [requestParms setObject:@"1" forKey:@"unencrypt"];
    [requestParms setObject:requestData forKey:@"requestData"];
    
    AFJSONRequestSerializer *requestSerializer = [AFJSONRequestSerializer serializer];
    NSMutableURLRequest *request = [requestSerializer requestWithMethod:@"POST" URLString:host parameters:requestParms error:nil];
    
    AFHTTPRequestOperation *requestOperation = [[AFHTTPRequestOperation alloc] initWithRequest:request];
    AFHTTPResponseSerializer *responseSerializer = [AFJSONResponseSerializer serializer];
    
    [requestOperation setResponseSerializer:responseSerializer];
    [requestOperation start];
    [requestOperation waitUntilFinished];
    
    NSDictionary *obj = [requestOperation responseObject];
    NSLog(@"JSON: %@", obj);
    NSString *status = [obj objectForKey:@"status"];
    if([status isEqual:@"0"]){ // success
        ret = [obj objectForKey:@"data"];
    } else {
        ret = [obj objectForKey:@"memo"];
        return [status intValue];
    }
    return 0;
}

//
-(void)Login:(NSString*)username :(NSString*)password :(UINavigationController*)nav {
    NSString *requestData = [NSString stringWithFormat:@"{\"opType\":\"login\",\"mobile\":\"%@\",\"password\":\"%@\"}", username, password];
    NSDictionary *params = @{@"unencrypt" : unencrypt, @"requestData" : requestData};
    AFHTTPRequestOperationManager *manager = [AFHTTPRequestOperationManager manager];
    [manager GET:host parameters:params success:^(AFHTTPRequestOperation *operation, id responseObject) {
        //NSLog(@"request: %@", operation.request.URL);
        //NSLog(@"response: %@", responseObject);
        NSNumber *status = [responseObject objectForKey:@"status"];
        if([status intValue] == 0){ // success
            ////////////////////////////////// Datas ////////////////////////////////
            NSDictionary *data = [responseObject objectForKey:@"data"];
            //NSLog(@"JSON: %@", data);
            accessToken = [data objectForKey:@"accessToken"];
            NSDictionary *profile = [data objectForKey:@"profile"];
            userId = [profile objectForKey:@"userId"];
            gender = [profile objectForKey:@"gender"];
            userType = [profile objectForKey:@"userType"];
            applyImageUrl = [profile objectForKey:@"applyImageUrl"];
            nickName = [profile objectForKey:@"nickName"];
            
            //初始化db
            [ChatModel sharedInstance:[NSString stringWithFormat:@"user_%@.sqlite", userId]];
            // 初始化测试数据
            NSString *sql = [NSString stringWithFormat:@"\
                             CREATE TABLE IF NOT EXISTS \
                             ChatList(userId bigint primary key, Orders integer default 0);\
                             INSERT INTO 'ChatList' ('userId') VALUES (116947);\
                             INSERT INTO 'ChatList' ('userId') VALUES (101198);\
                             CREATE TABLE IF NOT EXISTS \
                             Chat_116947(TableVer integer default 1, MesLocalID integer primary key autoincrement, MesSvrID bigint default 0, CreateTime integer default 0, Message text, Status integer default 0, ImgStatus integer default 0, Type integer, Des integer, Second integer default 0); \
                             CREATE INDEX IF NOT EXISTS Chat_116947_Index on Chat_116947(MesSvrID); \
                             CREATE INDEX IF NOT EXISTS Chat_116947_Index2 on Chat_116947(CreateTime); \
                             CREATE INDEX IF NOT EXISTS Chat_116947_Index3 on Chat_116947(Status); \
                             CREATE TABLE IF NOT EXISTS \
                             Chat_101198(TableVer integer default 1, MesLocalID integer primary key autoincrement, MesSvrID bigint default 0, CreateTime integer default 0, Message text, Status integer default 0, ImgStatus integer default 0, Type integer, Des integer, Second integer default 0); \
                             CREATE INDEX IF NOT EXISTS Chat_101198_Index on Chat_101198(MesSvrID); \
                             CREATE INDEX IF NOT EXISTS Chat_101198_Index2 on Chat_101198(CreateTime); \
                             CREATE INDEX IF NOT EXISTS Chat_101198_Index3 on Chat_101198(Status);"];
            [ChatModel execSql:sql];
            ////////////////////////////////// Datas ////////////////////////////////
            
            
            
            ////////////////////////////////// Views ////////////////////////////////
            UITabBarController *tabctl = [[UITabBarController alloc] init];
            [nav pushViewController:tabctl animated:YES]; // 跳转到另外一页
            //b.创建子控制器
            ChatListViewController *c1=[[ChatListViewController alloc]init];
            //c1.view.backgroundColor=[UIColor grayColor];
            c1.tabBarItem.title=@"消息";
            c1.tabBarItem.image=[UIImage imageNamed:@"contacts_add_newmessage@3x"];
            c1.tabBarItem.badgeValue=@"12";
            
            UIViewController *c2=[[UIViewController alloc]init];
            c2.view.backgroundColor=[UIColor brownColor];
            c2.tabBarItem.title=@"联系人";
            c2.tabBarItem.image=[UIImage imageNamed:@"barbuttonicon_InfoMulti@3x"];
            
            UIViewController *c3=[[UIViewController alloc]init];
            c3.tabBarItem.title=@"动态";
            c3.tabBarItem.image=[UIImage imageNamed:@"ToolViewEmotion@2x"];
            
            UIViewController *c4=[[UIViewController alloc]init];
            c4.tabBarItem.title=@"我";
            c4.tabBarItem.image=[UIImage imageNamed:@"barbuttonicon_InfoSingle@3x"];
            
            //c.添加子控制器到ITabBarController中
            //c.1第一种方式
            //    [tb addChildViewController:c1];
            //    [tb addChildViewController:c2];
            
            //c.2第二种方式
            tabctl.viewControllers=@[c1,c2,c3,c4];
            ////////////////////////////////// Views ////////////////////////////////
            
            
            ////////////////////////////////// MQTT ////////////////////////////////
            clientID = [NSString stringWithFormat:@"mosqpub/%@-iZ256cb32", userId];
            myTopic = [NSString stringWithFormat:@"%@", userId];
            // 初始化mqtt对象，单例模式
            static dispatch_once_t onceToken;
            dispatch_once(&onceToken, ^{
                // connect the MQTT client
                mqttclient = [[MQTTClient alloc] initWithClientId:clientID];
                mqttclient.port = 26790;
                [mqttclient connectToHost:kMQTTServerHost completionHandler:^(MQTTConnectionReturnCode code) {
                    if (code == ConnectionAccepted) {
                        // The client is connected when this completion handler is called
                        NSLog(@"mqttclient is connected with id %@", clientID);
                        // Subscribe to the topic 设置订阅某个主题
                        [mqttclient subscribe:myTopic withCompletionHandler:^(NSArray *grantedQos) {
                            // The client is effectively subscribed to the topic when this completion handler is called
                            NSLog(@"subscribed to my topic %@", myTopic);
                        }];
                    } else {
                        NSLog(@"failed %lu", code);
                    }
                }];
                
                // 收到消息，异步处理
                // define the handler that will be called when MQTT messages are received by the client
                [mqttclient setMessageHandler:^(MQTTMessage *message) {
                    dispatch_async(dispatch_get_main_queue(), ^{
                        // the MQTTClientDelegate methods are called from a GCD queue.
                        // Any update to the UI must be done on the main queue
                        
                        // extract the switch status from the message payload
                        //BOOL on = [message.payloadString boolValue];
                        O2Msg * o2msg = [O2Msg parseFromData:message.payload];
                        //NSNumber *type = [NSNumber numberWithInt:o2msg.header.type];
                        int type = o2msg.header.type;
                        int Des = 1;
                        
                        RootViewController *curView = [ChatListViewController getCurrView];
                        int curToUserId = [[curView getToUserId] intValue];
                        // 保存数据到沙盒和sqlite中。
                        if([@(UUMessageTypeText) intValue] == type){
                            NSLog(@"%@ recieve from %d: %@", message.topic, o2msg.body.fromUserId, o2msg.body.content);
                            // 保存到数据库中
                            NSString *sql = [NSString stringWithFormat:@"INSERT INTO 'Chat_%d' ('MesSvrID','Message','Type','CreateTime','Status','Des') VALUES (%ld,'%@',%d,DATETIME('now'),%d,%d)", o2msg.body.fromUserId, 3606817135795630481, o2msg.body.content, [@(UUMessageTypeText) intValue], 1, Des];
                            [ChatModel execSql:sql];
                            // 在当前激活的视图上绘制聊天内容
                            if(nil != curView && curToUserId == o2msg.body.fromUserId){
                                [curView saveText:o2msg.body.content :1];
                            }
                        } else if ([@(UUMessageTypePicture) intValue] == type){
                            UIImage *image = [UIImage imageWithData:o2msg.body.buffer];
                            NSString *fileName = [[Common randFileName] stringByAppendingString:@".jpg"];
                            NSString *filePath = [Common getFilePath:fileName];
                            BOOL result = [UIImagePNGRepresentation(image) writeToFile:filePath atomically:YES]; // 保存成功会返回YES
                            if(result){
                                NSString *sql = [NSString stringWithFormat:@"INSERT INTO 'Chat_%d' ('MesSvrID','Message','Type','CreateTime','Status','Des') VALUES (%ld,'%@',%d,DATETIME('now'),%d,%d)", o2msg.body.fromUserId, 3606817135795630481, fileName, [@(UUMessageTypePicture) intValue], 1, Des];
                                [ChatModel execSql:sql];
                            }
                            if(nil != curView && curToUserId == o2msg.body.fromUserId){
                                [curView savePictureByImg:image :1];
                            }
                        } else if ([@(UUMessageTypeVoice) intValue] == type){
                            NSString *fileName = [[Common randFileName] stringByAppendingString:@".voc"];
                            NSString *filePath = [Common getFilePath:fileName];
                            BOOL result = [o2msg.body.buffer writeToFile:filePath atomically:YES];
                            if(result){
                                NSString *sql = [NSString stringWithFormat:@"INSERT INTO 'Chat_%d' ('MesSvrID','Message','Type','CreateTime','Status','Des','Second') VALUES (%ld,'%@',%d,DATETIME('now'),%d,%d,%d)", o2msg.body.fromUserId, 3606817135795630481, fileName, [@(UUMessageTypeVoice) intValue], 1, Des, o2msg.body.second];
                                [ChatModel execSql:sql];
                            }
                            if(nil != curView && curToUserId == o2msg.body.fromUserId){
                                [curView saveVoice:o2msg.body.buffer :o2msg.body.second :1];
                            }
                        }
                    });
                    
                }];
            });
            ////////////////////////////////// MQTT ////////////////////////////////
            
            
        }
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
        NSLog(@"Error: %@", error);
    }];
}

+(void)sendMsg :(NSData*)data :(NSString*)topicOthers{
    NSLog(@"send to %@", topicOthers);
    // use the MQTT client to send a message with the switch status to the topic
    [mqttclient publishData:data
                     toTopic:topicOthers
                     withQos:AtMostOnce
                      retain:YES
           completionHandler:nil];
}

@end

