//
//  BIDViewController.m
//  MQTT
//
//  Created by hzfuji_mac_002 on 2014/05/12.
//  Copyright (c) 2014年 hzfuji_mac_002. All rights reserved.
//

#import "BIDViewController.h"
#import "MQTTKit.h"

#define kMQTTServerHost @"192.168.1.164"
#define kTopic @"tbk"
#define kTopic1 @"tbk1"
//#define kTopic @"notification"
//#define kMQTTServerHost @"iot.eclipse.org"
//#define kTopic @"MQTTExample/LED"

@interface BIDViewController ()

// this UISwitch will be used to display the status received from the topic.
@property (weak, nonatomic) IBOutlet UISwitch *subscribedSwitch;

// create a property for the MQTTClient that is used to send and receive the message
@property (nonatomic, strong) MQTTClient *client;

@end

@implementation BIDViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    
    // create the MQTT client with an unique identifier
    NSString *clientID = [UIDevice currentDevice].identifierForVendor.UUIDString;
//    NSLog(@"client is connected with id %@", clientID);
    self.client = [[MQTTClient alloc] initWithClientId:clientID];
    
    // keep a reference on the switch to avoid having a reference to self in the
    // block below (retain/release cycle, blah blah blah)
    UISwitch *subSwitch = self.subscribedSwitch;
    
    // define the handler that will be called when MQTT messages are received by the client
    [self.client setMessageHandler:^(MQTTMessage *message) {
        // extract the switch status from the message payload
        NSLog(@"%@", message.payloadString);
        BOOL on = [message.payloadString boolValue];
        
        // the MQTTClientDelegate methods are called from a GCD queue.
        // Any update to the UI must be done on the main queue
        dispatch_async(dispatch_get_main_queue(), ^{
            [subSwitch setOn:on animated:YES];
        });
    }];
    
    // connect the MQTT client
    [self.client connectToHost:kMQTTServerHost completionHandler:^(MQTTConnectionReturnCode code) {
        if (code == ConnectionAccepted) {
            // The client is connected when this completion handler is called
            NSLog(@"client is connected with id %@", clientID);
            // Subscribe to the topic
            [self.client subscribe:kTopic withCompletionHandler:^(NSArray *grantedQos) {
                // The client is effectively subscribed to the topic when this completion handler is called
                NSLog(@"subscribed to topic %@", kTopic);
            }];
        }
    }];

}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)switchButton1:(id)sender {
    BOOL on = [sender isOn];
    NSString *payload = [NSNumber numberWithBool:on].stringValue;
    
    payload = @"xuqf";
    
    [self.client subscribe:kTopic1 withCompletionHandler:^(NSArray *grantedQos) {
        NSLog(@"%@",kTopic1);
    }];
    
    // use the MQTT client to send a message with the switch status to the topic
    [self.client publishString:payload
                       toTopic:kTopic1
                       withQos:AtMostOnce
                        retain:YES
             completionHandler:nil];
    // we passed nil to the completionHandler as we are not interested to know
    // when the message was effectively sent
}
@end
