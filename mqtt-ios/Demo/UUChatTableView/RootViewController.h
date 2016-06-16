//
//  RootViewController.h
//  UUChatTableView
//
//  Created by shake on 15/1/4.
//  Copyright (c) 2015年 uyiuyao. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <sqlite3.h>

@interface RootViewController : UIViewController
-(id)initWithUserId : (NSNumber*)toUserId;
-(void) saveText : (NSString*) text : (int) Des;
- (NSNumber*) getToUserId;
- (void) savePicture : (NSData *) data : (int) Des;
- (void) savePictureByImg : (UIImage *) img : (int) Des;
- (void) saveVoice : (NSData *) data : (int) second : (int) Des;
@end
