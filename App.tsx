import React, { useState, useEffect, useCallback } from 'react';
import {
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  useColorScheme,
  View,
  NativeModules,
  Platform,
  TouchableOpacity,
  DeviceEventEmitter
} from 'react-native';

import { request, PERMISSIONS, RESULTS } from "react-native-permissions";
import moment, { Moment } from "moment";

import {
  Colors,
} from 'react-native/Libraries/NewAppScreen';
import { InputField } from './component/InputField';
import Header from './component/Header';

const { AddCalendarEvent } = NativeModules
const utcDateToString = (momentInUTC: Moment): string => {
  let s = moment.utc(momentInUTC).format("YYYY-MM-DDTHH:mm:ss.SSS[Z]");
  return s;
};

function App(): JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';
  const [eventId, setEventId] = useState("");
  const [eventTitle, setEventTitle] = useState("Client Meeting");
  const [nowUTC, setNowUTC] = useState(moment.utc());
  const [eventNotes, setEventNotes] = useState('This is the gentle reminder')

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };


  console.log('event is', AddCalendarEvent)



  // useEffect(() => {
  //   const savedEventListener = DeviceEventEmitter.addListener('EventSaved', (event) => {
  //     // Handle the 'EventSaved' event data
  //     console.log('Event saved:', event);
  //   });

  //   const deletedEventListener = DeviceEventEmitter.addListener('EventDeleted', (event) => {
  //     // Handle the 'EventDeleted' event data
  //     console.log('Event deleted:', event);
  //   });

  //   // Clean up event listeners when component unmounts
  //   return () => {
  //     savedEventListener.remove();
  //     deletedEventListener.remove();
  //   };
  // }, []);



  const addToCalendar = useCallback(() => {
    const eventConfig = {
      title: eventTitle,
      startDate: utcDateToString(nowUTC),
      endDate: utcDateToString(moment.utc(nowUTC).add(1, "hours")),
      notes: eventNotes,
      navigationBarIOS: {
        translucent: false,
        tintColor: "orange",
        barTintColor: "orange",
        backgroundColor: "green",
        titleColor: "blue",
      },
    };

    request(
      Platform.select({
        ios: PERMISSIONS.IOS.CALENDARS,
        default: PERMISSIONS.ANDROID.WRITE_CALENDAR,
      })
    )
      .then((result) => {
        console.log('permission error', result)
        if (result !== RESULTS.GRANTED) {
          throw new Error(`No permission: ${result}`);
        }
        return AddCalendarEvent.addEventToCalendar(eventConfig);
      })
      .then((eventInfo) => {
        console.warn(JSON.stringify(eventInfo));
        console.log('data receive from native side', eventInfo)

        if ("eventIdentifier" in eventInfo) {
          setEventTitle(eventInfo.title)
          setEventId(eventInfo.eventIdentifier);
          setNowUTC(eventInfo.startTime)
          setEventNotes(eventInfo.description)
        }
      })
      .catch((error: string) => {
        // handle error such as when user rejected permissions
        console.warn(error);
      });
  }, [eventTitle, nowUTC]);


  const editCalendarEvent = useCallback(() => {
    const eventConfig = {
      eventId,
    };

    request(
      Platform.select({
        ios: PERMISSIONS.IOS.CALENDARS_WRITE_ONLY,
        default: PERMISSIONS.ANDROID.WRITE_CALENDAR,
      })
    )
      .then((result) => {
        if (result !== RESULTS.GRANTED) {
          throw new Error(`No permission: ${result}`);
        }
        return AddCalendarEvent.editAddedEvent(eventConfig);
      })
      .then((eventInfo) => {
        console.log('edit event calender return', JSON.stringify(eventInfo))
        console.warn(JSON.stringify(eventInfo));
        setEventTitle(eventInfo.title)
        setNowUTC(eventInfo.startTime)
        setEventNotes(eventInfo.description)

      })
      .catch((error: string) => {
        console.warn(error);
      });
  }, [eventId]);








  useEffect(() => {
    if (Platform.OS === "android") {
      request(PERMISSIONS.ANDROID.READ_CALENDAR).then((result) => {
        console.warn(`android calendar read permission: ${result}`);
      });
    }
  }, []);


  return (
    <SafeAreaView style={backgroundStyle}>
      <StatusBar
        barStyle={isDarkMode ? 'light-content' : 'dark-content'}
        backgroundColor={backgroundStyle.backgroundColor}
      />
      <ScrollView
        contentInsetAdjustmentBehavior="automatic"
        style={backgroundStyle}>
        <Header headerTitle='New Event' />
        <View
          style={{
            backgroundColor: isDarkMode ? Colors.black : Colors.white,
          }}>

          
<Text style={styles.eventLabelStyle}>Event Title</Text>
          <InputField
            value={eventTitle}
            placeholder='Event Name'
            onChangeText={(text: string) => {
              const filteredText = text.replace(/[^a-zA-Z\s]|^\s+/g, '');
              setEventTitle(filteredText);
            }}
          />
           <Text style={styles.eventLabelStyle}>Event Notes</Text>
          <InputField
            value={eventNotes}
            placeholder='Event Notes'
            onChangeText={(text: string) => setEventNotes(text)}
          />

          <Text style={styles.eventPlaceholderStyle}>Event Date:</Text>
          {/* <Text style={styles.eventTitleStyle}>Event title: {eventTitle}</Text> */}

          <Text style={styles.eventTitleStyle}>Date From: {moment.utc(nowUTC).local().format("lll")}</Text>
          <TouchableOpacity style={styles.addEventBtn} onPress={() => addToCalendar()}>
            <Text style={styles.btnTitleStyle}>Create Event</Text>
          </TouchableOpacity>

         
          <Text style={styles.eventPlaceholderStyle}>Created Event is:</Text>
          <Text style={styles.eventTitleStyle}>Event title: {eventTitle}</Text>
          <Text style={styles.eventTitleStyle}>Event notes: {eventNotes}</Text>
          <Text style={styles.eventTitleStyle}>Date From: {moment.utc(nowUTC).local().format("lll")}</Text>


          <TouchableOpacity disabled={eventId ? false : true} style={[styles.addEventBtn, { backgroundColor: eventId ? 'green' : 'grey' }]} onPress={() => editCalendarEvent()}>
            <Text style={styles.btnTitleStyle}>Edit Event</Text>
          </TouchableOpacity>

        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: '600',
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
  },
  highlight: {
    fontWeight: '700',
  },
  eventTitleStyle: {
    fontSize: 15,
    margin: 15,
  },
  eventLabelStyle: {
    fontSize: 15,
   marginLeft:15,
   color: 'grey',
  },
  eventPlaceholderStyle: {
    fontSize: 20,
    color: 'grey',
    margin: 15,
  },
  addEventBtn: {
    flex: 1,
    backgroundColor: 'green',
    justifyContent: 'center',
    alignItems: 'center',
    padding: 10,
    margin: 10
  },
  btnTitleStyle: {
    color: 'white',
    fontSize: 20
  }
});

export default App;
