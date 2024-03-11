//
//  AddCalendarEvent.h
//  eventcalender
//
//  Created by Aakash Sajjad on 08/03/2024.
//

// #ifndef AddCalendarEvent_h
// #define AddCalendarEvent_h


// #endif /* AddCalendarEvent_h */

#import <UIKit/UIKit.h>
#import <EventKit/EventKit.h>
#import <EventKitUI/EKEventEditViewController.h>
#import <EventKitUI/EKEventViewController.h>
#import <EventKitUI/EventKitUIDefines.h>
#import <React/RCTUIManager.h>
#import <React/RCTUtils.h>

@interface AddCalendarEvent : NSObject <RCTBridgeModule, EKEventEditViewDelegate, EKEventViewDelegate>

@end