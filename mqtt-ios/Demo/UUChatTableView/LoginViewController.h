//
//  LoginViewController.h
//  UUChatTableView
//
//  Created by Liberty on 15/10/17.
//  Copyright © 2015年 uyiuyao. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface LoginViewController : UIViewController
{
    UIButton *btn_login;
    UITextField *txt_username;
    UITextField *txt_password;
}
@property (nonatomic,strong) IBOutlet UIButton *btn_login;
@property (nonatomic,strong) IBOutlet UITextField *txt_username;
@property (nonatomic,strong) IBOutlet UITextField *txt_password;

-(IBAction) Login : (id)sender;
@end
