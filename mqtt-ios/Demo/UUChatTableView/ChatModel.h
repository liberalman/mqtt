//
//  ChatModel.h
//  UUChatTableView
//
//  Created by shake on 15/1/6.
//  Copyright (c) 2015年 uyiuyao. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <sqlite3.h>

@interface ChatModel : NSObject

@property (nonatomic, strong) NSMutableArray *dataSource;
@property (nonatomic) BOOL isGroupChat;


+ (sqlite3 *)sharedInstance : (NSString *)dbname;
+ (void)execSql:(NSString *)sql;
+ (NSMutableArray *) getChatList;

-(id)initWithToUserId : (NSNumber *) toUserId;
- (void)addRandomItemsToDataSource;
- (void)addSpecifiedItem:(NSDictionary *)dic;
- (sqlite3 *)getDB;
- (void)resetPage;
-(void) loadProfile;

@end
