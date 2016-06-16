//
//  AppDelegate.m
//  UUChatTableView
//
//  Created by shake on 15/1/6.
//  Copyright (c) 2015年 uyiuyao. All rights reserved.
//

#import "AppDelegate.h"
#import "RootViewController.h"
#import "LoginViewController.h"

@interface AppDelegate ()

@end

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {

    self.window = [[UIWindow alloc]initWithFrame:[UIScreen mainScreen].bounds];
    
    //RootViewController *root = [[RootViewController alloc]init];
    //UINavigationController *nav = [[UINavigationController alloc]initWithRootViewController:root];
    
    LoginViewController *root = [[LoginViewController alloc]init];
    UINavigationController *nav = [[UINavigationController alloc]initWithRootViewController:root];
    
    //UINavigationController *nav = [[UINavigationController alloc]initWithRootViewController:_rootController];
    
    self.window.rootViewController = nav;
    
    [self.window makeKeyAndVisible];

    return YES;
}

@end
