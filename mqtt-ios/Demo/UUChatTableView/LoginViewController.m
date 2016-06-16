//
//  LoginViewController.m
//  UUChatTableView
//
//  Created by Liberty on 15/10/17.
//  Copyright © 2015年 uyiuyao. All rights reserved.
//

#import "LoginViewController.h"
#import "RootViewController.h"
#import "ChatListViewController.h"
#import "Common.h"

@interface LoginViewController ()

@end

@implementation LoginViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    /*self.view.backgroundColor = [UIColor blueColor];
    _btn_login = [UIButton buttonWithType:UIButtonTypeCustom];
    [_btn_login setTitle:@"login" forState:(UIControlStateNormal)];
    _btn_login.frame = CGRectMake(50, 100, 50, 30);
    [_btn_login addTarget:self action:@selector(login) forControlEvents:(UIControlEventTouchUpInside)];
    [self.view addSubview:_btn_login];*/
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(IBAction) Login : (id)sender {
    //RootViewController *root = [[RootViewController alloc] init];
    //[self.navigationController pushViewController:root animated:YES]; // 跳转到另外一页

    Common *com = [[Common alloc] init];
    [com Login :_txt_username.text :_txt_password.text :self.navigationController];
    
    /*
    UITabBarController *tabctl = [[UITabBarController alloc] init];
    [self.navigationController pushViewController:tabctl animated:YES]; // 跳转到另外一页
    
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
     */
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
