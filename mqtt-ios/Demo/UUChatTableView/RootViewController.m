//
//  RootViewController.m
//  UUChatTableView
//
//  Created by shake on 15/1/4.
//  Copyright (c) 2015年 uyiuyao. All rights reserved.
//

#import "RootViewController.h"
#import "UUInputFunctionView.h"
#import "MJRefresh.h"
#import "UUMessageCell.h"
#import "ChatModel.h"
#import "UUMessageFrame.h"
#import "UUMessage.h"
#import "MQTTKit/MQTTKit.h"
#import "O2.pb.h"
#import "Common.h"

// true machine
//#define kMyTopic @"100078"
//#define kTopicOthers @"101198"
//NSString *clientID = @"mosqpub/100078-iZ256cb32";

// moni
//#define kMyTopic @"101198"
//#define kTopicOthers @"100078"
//NSString *clientID = @"mosqpub/101198-iZ256cb32";

// create the MQTT client with an unique identifier
//NSString *clientID = [@"mosqpub/" stringByAppendingString:[UIDevice currentDevice].identifierForVendor.UUIDString];

@interface RootViewController ()<UUInputFunctionViewDelegate,UUMessageCellDelegate,UITableViewDataSource,UITableViewDelegate>

@property (strong, nonatomic) MJRefreshHeaderView *head;
@property (strong, nonatomic) ChatModel *chatModel;
@property (strong, nonatomic) NSNumber *toUserId; //这里的用户Id是和我聊天的对方的用户ID
@property (weak, nonatomic) IBOutlet UITableView *chatTableView;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *bottomConstraint;

@end

@implementation RootViewController{
    UUInputFunctionView *IFView;
}

-(id)initWithUserId : (NSNumber*)toUserId {
    self = [super init];
    if(self){
        self.toUserId = toUserId;
        self.chatModel = [[ChatModel alloc]initWithToUserId:self.toUserId];
    }
    return self;
}

- (NSNumber*) getToUserId {
    return self.toUserId;
}

-(void) saveText : (NSString*) text : (int) Des {
    NSDictionary *dic = @{@"strContent": text,
                          @"type": @(UUMessageTypeText),
                          @"Des": [NSNumber numberWithInt:Des]};
    [self dealTheFunctionData:dic];
}

- (void) savePicture : (NSData *) data : (int) Des {
    UIImage *img = [UIImage imageWithData:data];
    NSDictionary *dic = @{@"picture": img,
                          @"type": @(UUMessageTypePicture),
                          @"Des": [NSNumber numberWithInt:Des]};
    [self dealTheFunctionData:dic];
}

- (void) savePictureByImg : (UIImage *) img : (int) Des {
    NSDictionary *dic = @{@"picture": img,
                          @"type": @(UUMessageTypePicture),
                          @"Des": [NSNumber numberWithInt:Des]};
    [self dealTheFunctionData:dic];
}

- (void) saveVoice : (NSData *) data : (int) second : (int) Des {
    NSDictionary *dic = @{@"voice": data,
                          @"strVoiceTime": [NSString stringWithFormat:@"%d", second],
                          @"type": @(UUMessageTypeVoice),
                          @"Des": [NSNumber numberWithInt:Des]};
    [self dealTheFunctionData:dic];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    //NSLog(@"Root viewDidLoad");
    
    [self initBar];
    [self addRefreshViews];
    [self loadBaseViewsAndData]; // 视图初始化载入最近聊天数据
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
    //add notification
    [[NSNotificationCenter defaultCenter]addObserver:self selector:@selector(keyboardChange:) name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter]addObserver:self selector:@selector(keyboardChange:) name:UIKeyboardWillHideNotification object:nil];
    [[NSNotificationCenter defaultCenter]addObserver:self selector:@selector(tableViewScrollToBottom) name:UIKeyboardDidShowNotification object:nil];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    [[NSNotificationCenter defaultCenter]removeObserver:self];
}

// 顶部标签栏
- (void)initBar
{
    UISegmentedControl *segment = [[UISegmentedControl alloc]initWithItems:@[@" 私人会话 ",@" 群组 "]];
    [segment addTarget:self action:@selector(segmentChanged:) forControlEvents:UIControlEventValueChanged];
    segment.selectedSegmentIndex = 0;
    self.navigationItem.titleView = segment;
    
    self.navigationController.navigationBar.tintColor = [UIColor grayColor];
    //self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc]initWithBarButtonSystemItem:UIBarButtonSystemItemOrganize target:nil action:nil];
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc]initWithBarButtonSystemItem:UIBarButtonSystemItemSearch target:self action:nil];
}
- (void)segmentChanged:(UISegmentedControl *)segment
{
    //NSLog(@"顶部标签页切换");
    [self.chatModel resetPage]; //重置page
    self.chatModel.isGroupChat = segment.selectedSegmentIndex;
    [self.chatModel.dataSource removeAllObjects];
    [self.chatModel addRandomItemsToDataSource];
    [self.chatTableView reloadData];
}

- (void)addRefreshViews
{
    __weak typeof(self) weakSelf = self;
    
    //load more
    int pageNum = 5;
    
    _head = [MJRefreshHeaderView header];
    _head.scrollView = self.chatTableView;
    _head.beginRefreshingBlock = ^(MJRefreshBaseView *refreshView) {
        [weakSelf.chatModel addRandomItemsToDataSource];
        if (weakSelf.chatModel.dataSource.count > pageNum) {
            NSIndexPath *indexPath = [NSIndexPath indexPathForRow:pageNum inSection:0];
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                [weakSelf.chatTableView reloadData]; //刷新list
                [weakSelf.chatTableView scrollToRowAtIndexPath:indexPath atScrollPosition:UITableViewScrollPositionTop animated:NO];
            });
        }
        [weakSelf.head endRefreshing];
    };
}

// 视图初始化载入最近聊天数据
- (void)loadBaseViewsAndData
{
    
    self.chatModel.isGroupChat = NO;
    [self.chatModel addRandomItemsToDataSource]; //添加数据
    
    IFView = [[UUInputFunctionView alloc]initWithSuperVC:self];
    IFView.delegate = self;
    [self.view addSubview:IFView];
    
    [self.chatTableView reloadData];
    [self tableViewScrollToBottom];
}

-(void)keyboardChange:(NSNotification *)notification
{
    NSDictionary *userInfo = [notification userInfo];
    NSTimeInterval animationDuration;
    UIViewAnimationCurve animationCurve;
    CGRect keyboardEndFrame;
    
    [[userInfo objectForKey:UIKeyboardAnimationCurveUserInfoKey] getValue:&animationCurve];
    [[userInfo objectForKey:UIKeyboardAnimationDurationUserInfoKey] getValue:&animationDuration];
    [[userInfo objectForKey:UIKeyboardFrameEndUserInfoKey] getValue:&keyboardEndFrame];
    
    [UIView beginAnimations:nil context:nil];
    [UIView setAnimationDuration:animationDuration];
    [UIView setAnimationCurve:animationCurve];
    
    //adjust ChatTableView's height
    if (notification.name == UIKeyboardWillShowNotification) {
        self.bottomConstraint.constant = keyboardEndFrame.size.height+40;
    }else{
        self.bottomConstraint.constant = 40;
    }
    
    [self.view layoutIfNeeded];
    
    //adjust UUInputFunctionView's originPoint
    CGRect newFrame = IFView.frame;
    newFrame.origin.y = keyboardEndFrame.origin.y - newFrame.size.height;
    IFView.frame = newFrame;
    
    [UIView commitAnimations];
    
}

//tableView Scroll to bottom
- (void)tableViewScrollToBottom
{
    if (self.chatModel.dataSource.count==0)
        return;
    
    NSIndexPath *indexPath = [NSIndexPath indexPathForRow:self.chatModel.dataSource.count-1 inSection:0];
    [self.chatTableView scrollToRowAtIndexPath:indexPath atScrollPosition:UITableViewScrollPositionBottom animated:YES];
}


#pragma mark - InputFunctionViewDelegate
- (void)UUInputFunctionView:(UUInputFunctionView *)funcView sendMessage:(NSString *)message
{
    Header *header = [[[[Header builder] setType:[@(UUMessageTypeText) intValue]]
                        setLength:(int)message.length] build];
    Body *body = [[[[[Body builder] setContent:message] setFromUserId:[Common getUserId].intValue] setToUserId:self.toUserId.intValue] build];
    O2Msg *o2msg = [[[[O2Msg builder]
                      setHeader:header]
                     setBody:body] build];
    
    // use the MQTT client to send a message with the switch status to the topic
    /*[self.client publishData:[o2msg data]
                       toTopic:kTopicOthers
                       withQos:AtMostOnce
                        retain:YES
             completionHandler:nil];
     // we passed nil to the completionHandler as we are not interested to know
     // when the message was effectively sent
     */
    [Common sendMsg:[o2msg data] :[NSString stringWithFormat:@"%@", self.toUserId]];
    [self saveText:(message) :0];
}

- (void)UUInputFunctionView:(UUInputFunctionView *)funcView sendPicture:(UIImage *)image
{
    //NSLog(@"befor compass, image size:%lu kb", [UIImagePNGRepresentation(image) length]/1024);
    NSData *dataObj = UIImageJPEGRepresentation(image, 0.5); //压缩,UIImagePNGRepresentation(UIImage* image) 要比UIImageJPEGRepresentation(UIImage* image, 1.0) 返回的图片数据量大很多。项目中做图片上传之前，经过测试同一张拍照所得照片png大小在8M，而JPG压缩系数为0.75时候，大小只有1M。而且，将压缩系数降低对图片视觉上并没有太大的影响。
    UIImage *img = [UIImage imageWithData:dataObj];
    //NSLog(@"after compass, image size:%lu kb", [dataObj length]/1024);
    Header *header = [[[[Header builder] setType:[@(UUMessageTypePicture) intValue]]
                      setLength:1] build];
    Body *body = [[[[[Body builder] setBuffer:dataObj] setFromUserId:[Common getUserId].intValue] setToUserId:self.toUserId.intValue] build];
    O2Msg *o2msg = [[[[O2Msg builder]
                     setHeader:header]
                    setBody:body] build];
    
    
    [Common sendMsg:[o2msg data] :[NSString stringWithFormat:@"%@", self.toUserId]];
    [self savePictureByImg:(img) :0];
}

- (void)UUInputFunctionView:(UUInputFunctionView *)funcView sendVoice:(NSData *)voice time:(NSInteger)second
{
    Header *header = [[[[Header builder] setType:[@(UUMessageTypeVoice) intValue]]
                       setLength:1] build];
    Body *body = [[[[[[Body builder] setBuffer:voice] setSecond:(int)second] setFromUserId:[Common getUserId].intValue] setToUserId:self.toUserId.intValue] build];
    O2Msg *o2msg = [[[[O2Msg builder]
                      setHeader:header]
                     setBody:body] build];
    [Common sendMsg:[o2msg data] :[NSString stringWithFormat:@"%@", self.toUserId]];
    [self saveVoice:(o2msg.body.buffer) :(int)second :0];
}

- (void)dealTheFunctionData:(NSDictionary *)dic
{
    // 处理发送按钮
    UUInputFunctionView *funcView = [UUInputFunctionView alloc];
    funcView.TextViewInput.text = @"";
    [funcView changeSendBtnWithPhoto:YES];
    
    [self.chatModel addSpecifiedItem:dic];
    [self.chatTableView reloadData];
    [self tableViewScrollToBottom];
}

#pragma mark - tableView delegate & datasource
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section{
    return self.chatModel.dataSource.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath{
    UUMessageCell *cell = [tableView dequeueReusableCellWithIdentifier:@"CellID"];
    if (cell == nil) {
        cell = [[UUMessageCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"CellID"];
        cell.delegate = self;
    }
    [cell setMessageFrame:self.chatModel.dataSource[indexPath.row]];
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath{
    return [self.chatModel.dataSource[indexPath.row] cellHeight];
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath{
    [self.view endEditing:YES];
}

- (void)scrollViewWillBeginDragging:(UIScrollView *)scrollView{
    [self.view endEditing:YES];
}

#pragma mark - cellDelegate
- (void)headImageDidClick:(UUMessageCell *)cell userId:(NSString *)userId{
    // headIamgeIcon is clicked
    UIAlertView *alert = [[UIAlertView alloc]initWithTitle:cell.messageFrame.message.strName message:@"您点击了头像" delegate:nil cancelButtonTitle:@"确定" otherButtonTitles:nil];
    [alert show];
}

@end
