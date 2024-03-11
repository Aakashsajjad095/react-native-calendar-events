//
//  AddCalendarEvent.m
//  eventcalender
//
//  Created by Aakash Sajjad on 08/03/2024.
//

//#import <Foundation/Foundation.h>
#import "AddCalendarEvent.h"
#import "EKEventStoreSingleton.h"

// #import <React/RCTEventEmitter.h>


@interface AddCalendarEvent()
// @interface AddCalendarEvent : RCTEventEmitter <EKEventEditViewDelegate, EKEventViewDelegate>


@property (nonatomic) UIViewController *viewController;
@property (nonatomic) NSDictionary *eventOptions;

@property (nonatomic) RCTPromiseResolveBlock resolver;
@property (nonatomic) RCTPromiseRejectBlock rejecter;

@end


@implementation AddCalendarEvent

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE()
+ (BOOL)requiresMainQueueSetup
{
    return NO;
}

// - (NSArray<NSString *> *)supportedEvents {
//     return @[@"EventSaved", @"EventDeleted"];
// }

static NSString *const DELETED = @"DELETED";
static NSString *const SAVED = @"SAVED";
static NSString *const CANCELED = @"CANCELED";
static NSString *const DONE = @"DONE";
static NSString *const RESPONDED = @"RESPONDED";

- (NSDictionary *)constantsToExport
{
    return @{
             DELETED: DELETED,
             SAVED: SAVED,
             CANCELED: CANCELED,
             DONE: DONE,
             RESPONDED: RESPONDED
             };
}

static NSString *const _eventId = @"eventId";
static NSString *const _title = @"title";
static NSString *const _location = @"location";
static NSString *const _startDate = @"startDate";
static NSString *const _endDate = @"endDate";
static NSString *const _notes = @"notes";
static NSString *const _url = @"url";
static NSString *const _allDay = @"allDay";

static NSString *const MODULE_NAME= @"AddCalendarEvent";


- (EKEventStore *)getEventStoreInstance {
    return [EKEventStoreSingleton getInstance];
}

- (instancetype)init {
    self = [super init];
    if (self != nil) {
        [self resetPromises];
    }
    return self;
}

#pragma mark -
#pragma mark Dialog methods

RCT_EXPORT_METHOD(addEventToCalendar:(NSDictionary *)options resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{

    NSLog(@"presentEventCreatingDialog method called with options: %@", options);
    self.eventOptions = options;
    self.resolver = resolve;
    self.rejecter = reject;
    EKEventEditViewController *controller = [[EKEventEditViewController alloc] init];
    controller.event = [self createNewEventInstance];
    controller.eventStore = [self getEventStoreInstance];
    controller.editViewDelegate = self;
    // [self assignNavbarColorsTo:controller.navigationBar];
    [self presentViewController:controller];
}

RCT_EXPORT_METHOD(addedEventViewing:(NSDictionary *)options resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    self.eventOptions = options;
    self.resolver = resolve;
    self.rejecter = reject;
    EKEventViewController *controller = [[EKEventViewController alloc] init];
    controller.event = [self getEventInstance];
    controller.delegate = self;
    if (options[@"allowsEditing"]) {
        controller.allowsEditing = [RCTConvert BOOL:options[@"allowsEditing"]];
    }
    if (options[@"allowsCalendarPreview"]) {
        controller.allowsCalendarPreview = [RCTConvert BOOL:options[@"allowsCalendarPreview"]];
    }
    UINavigationController *navBar = [[UINavigationController alloc] initWithRootViewController:controller];
    [self assignNavbarColorsTo:navBar.navigationBar];
    [self presentViewController:navBar];
}

-(void)assignNavbarColorsTo: (UINavigationBar *) navigationBar
{
    NSDictionary * navbarOptions = _eventOptions[@"navigationBarIOS"];

    if (navbarOptions) {
        if (navbarOptions[@"tintColor"]) {
            navigationBar.tintColor = [RCTConvert UIColor:navbarOptions[@"tintColor"]];
        }
        if (navbarOptions[@"backgroundColor"]) {
            navigationBar.backgroundColor = [RCTConvert UIColor:navbarOptions[@"backgroundColor"]];
        }
        if (navbarOptions[@"translucent"]) {
            navigationBar.translucent = [RCTConvert BOOL:navbarOptions[@"translucent"]];
        }
        if (navbarOptions[@"barTintColor"]) {
            navigationBar.barTintColor = [RCTConvert UIColor:navbarOptions[@"barTintColor"]];
        }
        if(navbarOptions[@"titleColor"]) {
            UIColor* titleColor = [RCTConvert UIColor:navbarOptions[@"titleColor"]];
            navigationBar.titleTextAttributes = @{NSForegroundColorAttributeName: titleColor};
        }
    }
}

RCT_EXPORT_METHOD(editAddedEvent:(NSDictionary *)options resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    self.eventOptions = options;
    self.resolver = resolve;
    self.rejecter = reject;

    EKEventEditViewController *controller = [[EKEventEditViewController alloc] init];
    [[self getEventStoreInstance] calendarItemWithIdentifier: _eventOptions[_eventId]];
    controller.event = [self getEventInstance];
    controller.eventStore = [self getEventStoreInstance];
    controller.editViewDelegate = self;
    [self assignNavbarColorsTo:controller.navigationBar];
    [self presentViewController:controller];
}

- (void)presentViewController: (UIViewController *) controller {
    self.viewController = RCTPresentedViewController();
    [self.viewController presentViewController:controller animated:YES completion:nil];
}

- (nullable EKEvent *)getEventInstance {
    EKEvent *maybeEvent = [[self getEventStoreInstance] eventWithIdentifier: _eventOptions[_eventId]];
    if (!maybeEvent) {
        maybeEvent = [[self getEventStoreInstance] calendarItemWithIdentifier: _eventOptions[_eventId]];
    }
    return maybeEvent;
}

- (EKEvent *)createNewEventInstance {
    EKEvent *event = [EKEvent eventWithEventStore: [self getEventStoreInstance]];
    NSDictionary *options = _eventOptions;

    event.title = [RCTConvert NSString:options[_title]];
    event.location = options[_location] ? [RCTConvert NSString:options[_location]] : nil;
    if (options[_startDate]) {
        event.startDate = [RCTConvert NSDate:options[_startDate]];
    }
    if (options[_endDate]) {
        event.endDate = [RCTConvert NSDate:options[_endDate]];
    }
    if (options[_url]) {
        event.URL = [RCTConvert NSURL:options[_url]];
    }
    if (options[_notes]) {
        event.notes = [RCTConvert NSString:options[_notes]];
    }
    if (options[_allDay]) {
        event.allDay = [RCTConvert BOOL:options[_allDay]];
    }
    return event;
}

- (void)rejectPromise: (NSString *) code withMessage: (NSString *) message withError: (NSError *) error {
    if (self.rejecter) {
        self.rejecter(code, message, error);
        [self resetPromises];
    }
}

- (void)resetPromises {
    self.resolver = nil;
    self.rejecter = nil;
}

// #pragma mark -
// #pragma mark EKEventEditViewDelegate

// - (void)eventEditViewController:(EKEventEditViewController *)controller
//           didCompleteWithAction:(EKEventEditViewAction)action
// {
//     AddCalendarEvent * __weak weakSelf = self;
//     [self.viewController dismissViewControllerAnimated:YES completion:^
//      {
//          dispatch_async(dispatch_get_main_queue(), ^{
//              if (action == EKEventEditViewActionCanceled) {
//                  [weakSelf resolveWithAction:CANCELED];
//              } else if (action == EKEventEditViewActionSaved) {
//                  EKEvent *evt = controller.event;
//                  NSDictionary *params = @{
//                                           @"eventIdentifier":evt.eventIdentifier,
//                                           @"calendarItemIdentifier":evt.calendarItemIdentifier,
//                                           };
//                  [weakSelf resolveWithAction:SAVED andParams:params];
//              } else if (action == EKEventEditViewActionDeleted) {
//                  [weakSelf resolveWithAction:DELETED];
//              }
//          });
//      }];
// }


// #pragma mark - EKEventEditViewDelegate
#pragma mark -
#pragma mark EKEventEditViewDelegate

- (void)eventEditViewController:(EKEventEditViewController *)controller
          didCompleteWithAction:(EKEventEditViewAction)action
{
    AddCalendarEvent * __weak weakSelf = self;
    [self.viewController dismissViewControllerAnimated:YES completion:^{
        dispatch_async(dispatch_get_main_queue(), ^{
            if (action == EKEventEditViewActionCanceled) {
                [weakSelf resolveWithAction:CANCELED];
            } else if (action == EKEventEditViewActionSaved) {
                EKEvent *evt = controller.event;
                 // Convert startDate and endDate to Unix timestamps in milliseconds
                NSTimeInterval startTimeMillis = [evt.startDate timeIntervalSince1970] * 1000;
                NSTimeInterval endTimeMillis = [evt.endDate timeIntervalSince1970] * 1000;
                NSDictionary *params = @{
                    @"eventIdentifier": evt.eventIdentifier,
                    @"calendarItemIdentifier": evt.calendarItemIdentifier,
                     @"title": evt.title,
                    @"description": evt.notes ?: [NSNull null], // Use NSNull if notes is nil
                    @"location": evt.location ?: [NSNull null], // Use NSNull if location is nil
//                    @"startTime": @([evt.startDate timeIntervalSince1970]),
//                    @"endTime": @([evt.endDate timeIntervalSince1970])
                    // @"title": evt.title,
                   @"startTime": @(startTimeMillis),
                    @"endTime": @(endTimeMillis)
                    // @"notes": evt.notes ?: [NSNull null] // Use NSNull if notes is nil
                    // Add other relevant event properties here
                };
                [weakSelf resolveWithAction:SAVED andParams:params];
            } else if (action == EKEventEditViewActionDeleted) {
                [weakSelf resolveWithAction:DELETED];
            }
        });
    }];
}



#pragma mark -
#pragma mark EKEventViewDelegate

- (void)eventViewController:(EKEventViewController *)controller
      didCompleteWithAction:(EKEventViewAction)action
{
    AddCalendarEvent * __weak weakSelf = self;
    [self.viewController dismissViewControllerAnimated:YES completion:^
     {
         dispatch_async(dispatch_get_main_queue(), ^{
             if (action == EKEventViewActionDeleted) {
                 [weakSelf resolveWithAction:DELETED];
             } else if (action == EKEventViewActionDone) {
                 [weakSelf resolveWithAction:DONE];
             } else if (action == EKEventViewActionResponded) {
                 [weakSelf resolveWithAction:RESPONDED];
             }
         });
     }];
}

- (void)resolveWithAction: (NSString *)action {
    [self resolvePromise: @{
                             @"action": action
                             }];
}

- (void)resolveWithAction: (NSString *)action andParams: (NSDictionary *) params {
    NSMutableDictionary *extendedArgs = [params mutableCopy];
    [extendedArgs setObject:action forKey:@"action"];
    [self resolvePromise: extendedArgs];
}

- (void)resolvePromise: (id) result {
    if (self.resolver) {
        self.resolver(result);
        [self resetPromises];

        // Emit event when promise is resolved
        // [self sendEventWithName:@"EventSaved" body:result];
    }
}

@end
