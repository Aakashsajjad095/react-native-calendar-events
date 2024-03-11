//
//  EKEventStoreSingleton.h
//  eventcalender
//
//  Created by Aakash Sajjad on 08/03/2024.
//

//#ifndef EKEventStoreSingleton_h
//#define EKEventStoreSingleton_h
//
//
//#endif /* EKEventStoreSingleton_h */
#ifndef EKEventStoreSingleton_h
#define EKEventStoreSingleton_h
#import <EventKit/EventKit.h>

@interface EKEventStoreSingleton : NSObject {
}


+ (EKEventStore *)getInstance;

@end

#endif /* EKEventStoreSingleton_h */
