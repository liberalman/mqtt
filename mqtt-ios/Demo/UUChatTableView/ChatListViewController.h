//
//  ChatListViewController.h
//  UUChatTableView
//
//  Created by Liberty on 15/10/17.
//  Copyright © 2015年 uyiuyao. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "RootViewController.h"

@interface ChatListViewController : UIViewController<UITableViewDelegate, UITableViewDataSource>
+ (RootViewController *) getCurrView;
@end
