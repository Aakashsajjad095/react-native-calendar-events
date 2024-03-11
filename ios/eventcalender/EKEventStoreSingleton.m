//
//  EKEventStoreSingleton.m
//  eventcalender
//
//  Created by Aakash Sajjad on 08/03/2024.
//

// #import <Foundation/Foundation.h>
#import <Foundation/Foundation.h>
#import "EKEventStoreSingleton.h"


@implementation EKEventStoreSingleton

#pragma mark Singleton Methods

+ (EKEventStore *)getInstance {
    static EKEventStore *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[EKEventStore alloc] init];
    });
    return sharedInstance;
}

@end
