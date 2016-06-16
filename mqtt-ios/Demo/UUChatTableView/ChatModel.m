//
//  ChatModel.m
//  UUChatTableView
//
//  Created by shake on 15/1/6.
//  Copyright (c) 2015年 uyiuyao. All rights reserved.
//

#import <CoreData/CoreData.h>
#import "ChatModel.h"
#import "UUMessage.h"
#import "UUMessageFrame.h"
#import "Common.h"

static sqlite3 *_db;
static int page = 1;
static int size = 10;
static NSString *DBNAME;

@interface ChatModel()

@property (strong, nonatomic) NSNumber *toUserId; //这里的用户Id是和我聊天的对方的用户ID
@property (strong, nonatomic) NSString *nickName;
@property (strong, nonatomic) NSString *avatorUrl;

@end

@implementation ChatModel

+ (sqlite3 *) sharedInstance : (NSString *)dbname
{
    static dispatch_once_t oncePredicate;
    dispatch_once(&oncePredicate, ^{
        if(nil != dbname){
            DBNAME = dbname;
        }
        //获取沙盒目录，并创建或打开数据库。
        NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
        NSString *documents = [paths objectAtIndex:0];
        NSString *database_path = [documents stringByAppendingPathComponent:DBNAME];
        NSLog(@"%@", database_path);
        int sqlite_return = sqlite3_open([database_path UTF8String], &_db);
        if (sqlite_return != SQLITE_OK) {
            sqlite3_close(_db);
            NSLog(@"数据库打开失败 %d,%@", sqlite_return, database_path);
            exit(1);
        }
    });
    return _db;
}

-(id)initWithToUserId : (NSNumber *) toUserId {
    self = [super init];
    if (self) {
        self.toUserId = toUserId;
        self.nickName = @"";
        self.avatorUrl = @"";
        NSString *sql = [NSString stringWithFormat:@"\
                         CREATE TABLE IF NOT EXISTS \
                         Chat_%@(TableVer integer default 1, MesLocalID integer primary key autoincrement, MesSvrID bigint default 0, CreateTime integer default 0, Message text, Status integer default 0, ImgStatus integer default 0, Type integer, Des integer, Second integer default 0); \
                         CREATE INDEX IF NOT EXISTS Chat_%@_Index on Chat_%@(MesSvrID); \
                         CREATE INDEX IF NOT EXISTS Chat_%@_Index2 on Chat_%@(CreateTime); \
                         CREATE INDEX IF NOT EXISTS Chat_%@_Index3 on Chat_%@(Status); \
                         ", toUserId, toUserId, toUserId, toUserId, toUserId, toUserId, toUserId];
        [ChatModel execSql:sql];
        [self loadProfile];
    }
    return self;
}

- (sqlite3 *)getDB{
    return _db;
}

- (void)resetPage{
    page = 1;
}

- (NSString *) randFileName {
    NSDateFormatter* dateFormat = [[NSDateFormatter alloc] init];//实例化一个NSDateFormatter对象
    [dateFormat setDateFormat:@"yyyyMMddHHmmss"];//设定时间格式,这里可以设置成自己需要的格式
    int rand = arc4random() % 10000;
    NSString *filename = [[dateFormat stringFromDate:[NSDate date]] stringByAppendingFormat:(@"%d"), rand];
    return filename;
}

- (NSString *) getFilePath : (NSString *) filename {
    //保存到对应的沙盒目录中，具体代码如下：
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory,NSUserDomainMask, YES);
    NSString *filePath = [[paths objectAtIndex:0] stringByAppendingPathComponent:filename];   // 保存文件的名称
    return filePath;
}

- (void)addRandomItemsToDataSource{
    if (nil == self.dataSource) //初始化视图，加载数据
        self.dataSource = [NSMutableArray array];
    unsigned long curOffset = [self.dataSource count];
    int allCount = [self getItemsCount]; //获取聊天内容总数量
    NSLog(@"下拉刷新 allCount %d,curOffset %lu\n", allCount, curOffset);
    if (curOffset < allCount){
        NSArray * arr = [self additems:curOffset/size+1 :size];
        if (nil != arr){
            NSIndexSet *indexes = [NSIndexSet indexSetWithIndexesInRange: NSMakeRange(0,[arr count])];
            [self.dataSource insertObjects:arr atIndexes:indexes];
        }
    }
}

- (void) showItem : (NSDictionary *) dic : (int) Des {
    NSMutableDictionary *dataDic = [NSMutableDictionary dictionaryWithDictionary:dic];
    if (0 == Des){ // 自己发出去的消息
        [dataDic setObject:@(UUMessageFromMe) forKey:@"from"];
        [dataDic setObject:[Common getNickName] forKey:@"strName"];
        [dataDic setObject:[Common getApplyImageUrl] forKey:@"strIcon"];
        // Type 1文本
        // Des 0自己发出去的 1接收别人发来的
    } else if (1 == Des){ // 别人发过来的消息
        [dataDic setObject:@(UUMessageFromOther) forKey:@"from"];
        [dataDic setObject:self.nickName forKey:@"strName"];
        [dataDic setObject:self.avatorUrl forKey:@"strIcon"];
    }
    
    [dataDic setObject:[[NSDate date] description] forKey:@"strTime"];
    UUMessageFrame *messageFrame = [[UUMessageFrame alloc]init];
    UUMessage *message = [[UUMessage alloc] init];
    [message setWithDict:dataDic];
    [message minuteOffSetStart:previousTime end:dataDic[@"strTime"]];
    messageFrame.showTime = message.showDateLabel;
    [messageFrame setMessage:message];
    
    if (message.showDateLabel) {
        previousTime = dataDic[@"strTime"];
    }
    [self.dataSource addObject:messageFrame];
}

// 添加一个item
- (void)addSpecifiedItem:(NSDictionary *)dic
{
    int Des = [[dic objectForKey:@"Des"] intValue];
    int type = [[dic objectForKey:@"type"] intValue];
    
    if([@(UUMessageTypeText) intValue] == type){
        NSString *content = dic[@"strContent"];
        NSString *sql = [NSString stringWithFormat:@"INSERT INTO 'Chat_%@' ('MesSvrID','Message','Type','CreateTime','Status','Des') VALUES (%ld,'%@',%d,DATETIME('now'),%d,%d)", self.toUserId, 3606817135795630481, content, [@(UUMessageTypeText) intValue], 1, Des];
        [ChatModel execSql:sql];
    } else if ([@(UUMessageTypePicture) intValue] == type){
        UIImage *image = [dic objectForKey:@"picture"];
        NSString *fileName = [[self randFileName] stringByAppendingString:@".jpg"];
        NSString *filePath = [self getFilePath:fileName];
        BOOL result = [UIImagePNGRepresentation(image) writeToFile:filePath atomically:YES]; // 保存成功会返回YES
        if(result){
            NSString *sql = [NSString stringWithFormat:@"INSERT INTO 'Chat_%@' ('MesSvrID','Message','Type','CreateTime','Status','Des') VALUES (%ld,'%@',%d,DATETIME('now'),%d,%d)", self.toUserId, 3606817135795630481, fileName, [@(UUMessageTypePicture) intValue], 1, Des];
            [ChatModel execSql:sql];
        }
    } else if ([@(UUMessageTypeVoice) intValue] == type){
        NSData *voice = [[dic objectForKey:@"voice"] data];
        int second = [[dic objectForKey:@"strVoiceTime"] intValue];
        NSString *fileName = [[self randFileName] stringByAppendingString:@".voc"];
        NSString *filePath = [self getFilePath:fileName];
        BOOL result = [voice writeToFile:filePath atomically:YES];
        if(result){
            NSString *sql = [NSString stringWithFormat:@"INSERT INTO 'Chat_%@' ('MesSvrID','Message','Type','CreateTime','Status','Des','Second') VALUES (%ld,'%@',%d,DATETIME('now'),%d,%d,%d)", self.toUserId, 3606817135795630481, fileName, [@(UUMessageTypeVoice) intValue], 1, Des, second];
            [ChatModel execSql:sql];
        }
    }
    
    [self showItem:dic :Des];
}

- (int) getItemsCount{
    if (nil == _db){
        [ChatModel sharedInstance:nil];
    }
    NSString *sqlQuery = [NSString stringWithFormat:@"SELECT COUNT(MesLocalID) FROM Chat_%@", self.toUserId];
    sqlite3_stmt * statement;
    int count = 0;
    if (sqlite3_prepare_v2(_db, [sqlQuery UTF8String], -1, &statement, nil) == SQLITE_OK) {
        sqlite3_step(statement);
        count = sqlite3_column_int(statement, 0);
    }
    return count;
}

// 添加聊天item（一个cell内容）
static NSString *previousTime = nil;
- (NSArray *)additems: (NSInteger)page :(NSInteger)size
{
    if (nil == _db){
        [ChatModel sharedInstance:nil];
    }
    NSMutableArray *result = [NSMutableArray array];
    NSString *sqlQuery = [NSString stringWithFormat:@"SELECT TableVer,MesLocalID,MesSvrID,datetime(CreateTime, 'unixepoch', 'localtime') CreateTime,Message,Status,ImgStatus,Type,Des,Second FROM (SELECT * FROM Chat_%@ ORDER BY MesLocalID desc limit %ld,%ld) ORDER BY MesLocalID asc", self.toUserId, (page-1)*size, (long)size];
    sqlite3_stmt * statement;
    NSTimeZone *zone = [NSTimeZone systemTimeZone];
    
    if (sqlite3_prepare_v2(_db, [sqlQuery UTF8String], -1, &statement, nil) == SQLITE_OK) {
        while (sqlite3_step(statement) == SQLITE_ROW) {
            //printf("num:%d page:%ld\n", num++, (long)page);
            int TableVer = sqlite3_column_int(statement, 0);
            int MesLocalID = sqlite3_column_int(statement, 1);
            int64_t MesSvrID = sqlite3_column_int64(statement, 2);
            time_t CreateTime = sqlite3_column_int(statement, 3);
            char *cMessage = (char*)sqlite3_column_text(statement, 4);
            NSString *Message = [[NSString alloc]initWithUTF8String:cMessage];
            int Status = sqlite3_column_int(statement, 5);
            int ImgStatus = sqlite3_column_int(statement, 6);
            int Type = sqlite3_column_int(statement, 7);
            int Des = sqlite3_column_int(statement, 8);
            int Second = sqlite3_column_int(statement, 9);
            
            
            NSDate *creDate = [NSDate dateWithTimeIntervalSinceNow:CreateTime];
            NSInteger interval = [zone secondsFromGMTForDate:creDate];
            NSDate *localeDate = [creDate  dateByAddingTimeInterval: interval];
            //NSLog(@"TableVer:%d  MesLocalID:%d  MesSvrID:%llu CreateTime:%@ Message:%@ Status:%d ImgStatus:%d Type:%d Des:%d",TableVer, MesLocalID, MesSvrID, [localeDate description], Message, Status, ImgStatus, Type, Des);
            
            NSNumber *type = [NSNumber numberWithInt:Type];
            NSMutableDictionary *dic = [[NSMutableDictionary alloc] init];
            if([@(UUMessageTypeText)  isEqual:type]){
                [dic setObject:Message forKey:@"strContent"];
                [dic setObject:@(UUMessageTypeText) forKey:@"type"];
            } else if ([@(UUMessageTypePicture)  isEqual:type]){
                //NSData转换为UIImage
                NSData *data = [NSData dataWithContentsOfFile: [self getFilePath:Message]];
                if(nil != data){
                    UIImage *image = [UIImage imageWithData: data];
                    [dic setObject:image forKey:@"picture"];
                    [dic setObject:@(UUMessageTypePicture) forKey:@"type"];
                }
            } else if ([@(UUMessageTypeVoice)  isEqual:type]){
                NSData *data = [NSData dataWithContentsOfFile: [self getFilePath:Message]];
                if(nil != data){
                    [dic setObject:data forKey:@"voice"]; // load voice data
                    [dic setObject:[NSString stringWithFormat:@"%d", Second] forKey:@"strVoiceTime"];
                    [dic setObject:@(UUMessageTypeVoice) forKey:@"type"];
                }
            }
            [dic setObject:[localeDate description] forKey:@"strTime"]; //NSTimeInterval

            [self showItem:dic :Des];
        }
    }
    
    return result;
}

+ (void) execSql:(NSString *)sql{
    if (nil == _db){
        [ChatModel sharedInstance:nil];
    }
    //NSLog(@"sql:%@", sql);
    char *err;
    int ret = sqlite3_exec(_db, [sql UTF8String], NULL, NULL, &err);
    if (ret != SQLITE_OK) {
        //sqlite3_close(_db);
        //_db = nil;
        NSLog(@"数据库操作失败! %d,%s", ret, err);
    }
}

+ (NSMutableArray *) getChatList
{
    if (nil == _db){
        [ChatModel sharedInstance:nil];
    }
    NSMutableArray *result = [NSMutableArray array];
    NSString *sql = [NSString stringWithFormat:@"select userId from ChatList limit %d,%d", 0, 100];
    sqlite3_stmt * statement;
    int ret = sqlite3_prepare_v2(_db, [sql UTF8String], -1, &statement, nil);
    if (ret == SQLITE_OK) {
        while (sqlite3_step(statement) == SQLITE_ROW) {
            int userId = sqlite3_column_int(statement, 0);
            //[result addObject:[NSNumber numberWithInt:userId]]; //这个Array会报错，默认将NSNumber转NSString出错
            [result addObject:[NSString stringWithFormat:@"%d", userId]];
        }
    } else {
        NSLog(@"数据库操作失败! %d", ret);
    }
    return result;
}

-(void) loadProfile {
    if(self.nickName.length <= 0 && nil != self.toUserId){ //空值
        NSString *requestData = [NSString stringWithFormat:@"{\"opType\":\"getSomeOneProfile\",\"accessToken\":\"%@\",\"toUserId\":%@}", [Common getAccessToken], self.toUserId];
        NSDictionary *params = @{@"unencrypt" : [Common getUnencrypt], @"requestData" : requestData};
        AFHTTPRequestOperationManager *manager = [AFHTTPRequestOperationManager manager];
        [manager GET:[Common getHost] parameters:params success:^(AFHTTPRequestOperation *operation, id responseObject) {
            //NSLog(@"request: %@", operation.request.URL);
            //NSLog(@"response: %@", responseObject);
            NSNumber *status = [responseObject objectForKey:@"status"];
            if([status intValue] == 0){ // success
                NSDictionary *data = [responseObject objectForKey:@"data"];
                self.avatorUrl = [data objectForKey:@"avatorUrl"];
                self.nickName = [data objectForKey:@"nickName"];
                //NSLog(@"JSON getSomeOneProfile %@: %@", self.toUserId, data);
            }
        } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
            NSLog(@"Error: %@", error);
        }];
    }
}

@end
