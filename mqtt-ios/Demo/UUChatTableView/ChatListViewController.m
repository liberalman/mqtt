//
//  ChatListViewController.m
//  UUChatTableView
//
//  Created by Liberty on 15/10/17.
//  Copyright © 2015年 uyiuyao. All rights reserved.
//

#import "ChatListViewController.h"
#import "RootViewController.h"
#import "ChatModel.h"

@interface ChatListViewController ()

@end

@implementation ChatListViewController

static NSMutableArray *viewList;
static NSMutableDictionary *viewdic;
static RootViewController *currView;

+ (RootViewController *) getCurrView {
    return currView;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    //list = [[NSArray alloc] initWithObjects:@"100078", @"116947" , nil];
    static dispatch_once_t oncePredicate1;
    dispatch_once(&oncePredicate1, ^{
        NSString *sql = [NSString stringWithFormat:@"\
                         CREATE TABLE IF NOT EXISTS \
                         ChatList(userId bigint primary key, Orders integer default 0);"];
        [ChatModel execSql:sql];
    });
    
    viewList = [ChatModel getChatList];
    viewdic = [[NSMutableDictionary alloc] init];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    //定义个静态字符串为了防止与其他类的tableivew重复
    static NSString *TableSampleIdentifier = @"TableSampleIdentifier";
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:
                             TableSampleIdentifier];
    if (cell == nil) {
        cell = [[UITableViewCell alloc]
                initWithStyle:UITableViewCellStyleDefault
                reuseIdentifier:TableSampleIdentifier];
    }
    
    NSUInteger row = [indexPath row];
    cell.textLabel.text = [viewList objectAtIndex:row];
    cell.detailTextLabel.text = @"!";
    
    /*
     // load image
     NSURL *url = [NSURL URLWithString:@"http://7vzu9s.com1.z0.glb.clouddn.com/header_132055_20151015210520.jpg?imageView2/0/format/jpg/q/95"];
     UIImage *image = [UIImage imageWithData:[NSData dataWithContentsOfURL:url]];
     //UIImage *image = [UIImage imageNamed:@"qq"];
     cell.imageView.image = image;
     //UIImage *highLighedImage = [UIImage imageNamed:@"youdao"];
     UIImage *highLighedImage = image;
     cell.imageView.highlightedImage = highLighedImage;
     
     NSLog(@"cell %d", (int)row);
     */
    return cell;
}

/*
 // 调整cell高度
 - (CGFloat)tableView:(UITableView*)tableView heightForRowAtIndexPath:(nonnull NSIndexPath *)indexPath {
 UITableViewCell *cell = [self tableView:tableView cellForRowAtIndexPath:indexPath];
 NSLog(@"cell height %f", cell.frame.size.height);
 return cell.frame.size.height + 20;
 }
 */

// 第section组有多少行
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return viewList.count;
}

/**
 *  一共有多少组数据
 */
-(NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(nonnull NSIndexPath *)indexPath {
    //NSLog(@"%d %d", viewlist.count, indexPath.item);
    RootViewController *chatView = [viewdic objectForKey:viewList[indexPath.item]];
    if(!chatView){
        chatView = [[RootViewController alloc] initWithUserId:viewList[indexPath.item]];
        [viewdic setObject:chatView forKey:viewList[indexPath.item]];
    }
    currView = chatView;
    [self.navigationController pushViewController:chatView animated:YES];
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
