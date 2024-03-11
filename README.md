# react-native-calendar-events 📅

This repository enables the integration of native calendar functionalities into React Native applications. It allows users to initiate activities on Android or display modal windows on iOS for adding, viewing, or editing events in the device's calendar. By utilizing promises and EventEmittor, developers can receive notifications when a new event is added along with its ID or when an event is removed. These functionalities are implemented through native modules.


# 📅 react-native-calendar-events Screenshots

<div style="display: flex; flex-wrap: wrap; justify-content: space-between;">
    <img src="https://github.com/Aakashsajjad095/react-native-calendar-events/assets/53012829/6785a7e2-bee9-40a9-970a-ab3f9c5d0676" alt="Screenshot 1" width="200" style="margin: 30px;" />
    <img src="https://github.com/Aakashsajjad095/react-native-calendar-events/assets/53012829/5c73c8ed-dc23-4ab4-963b-385eb0a1813c" alt="Screenshot 2" width="200" style="margin: 30px;" />
    <img src="https://github.com/Aakashsajjad095/react-native-calendar-events/assets/53012829/03667d46-ecc5-41bc-aec4-56b71ee2b96f" alt="Screenshot 3" width="200" style="margin: 30px;" />
    <img src="https://github.com/Aakashsajjad095/react-native-calendar-events/assets/53012829/df71d9d5-2935-42ad-b69e-c98b8edf160d" alt="Screenshot 4" width="200" style="margin: 30px;" />
</div>



<div style="display: flex; justify-content: center;">
  <div style="flex: 1; margin-right: 5px;">
    <h3 align="center">Android Preview</h3>
  [device-2024-03-11-143624_98idBXqR.webm](https://github.com/Aakashsajjad095/react-native-calendar-events/assets/53012829/beb2c67b-2dc4-4c3a-a609-221a25673b82)
  </div>
  <div style="flex: 1; margin-left: 5px;">
    <h3 align="center">iOS Preview</h3>
  https://github.com/Aakashsajjad095/react-native-calendar-events/assets/53012829/04e46695-926e-40fb-bc8c-f10e316b7c62
  </div>
</div>

## Permissions

You'll also need to install and setup [react-native-permissions](https://github.com/zoontek/react-native-permissions/), or similar, to request calendar permissions for your app.

## Quick example

See the app.tsx file for a demo app.

Using `react-native-permissions` to request calendar permission before creating a calendar event.

```js
import { Platform,NativeModules } from 'react-native';
import * as Permissions from 'react-native-permissions';

//call native module
const { AddCalendarEvent } = NativeModules

const eventConfig = {
  title,
  // and other options
};

Permissions.request(
  Platform.select({
    ios: Permissions.PERMISSIONS.IOS.CALENDARS_WRITE_ONLY,
    android: Permissions.PERMISSIONS.ANDROID.WRITE_CALENDAR,
  })
)
  .then(result => {
    if (result !== Permissions.RESULTS.GRANTED) {
      throw new Error(`No permission: ${result}`);
    }
    return AddCalendarEvent.addEventToCalendar(eventConfig)
  })
  .then((eventInfo: { calendarItemIdentifier: string, eventIdentifier: string }) => {
    // handle success - receives an object with `calendarItemIdentifier` and `eventIdentifier` keys, both of type string.
    // These are two different identifiers on iOS.
    // On Android, where they are both equal and represent the event id, also strings.
    // when { action: 'CANCELED' } is returned, the dialog was dismissed
    console.warn(JSON.stringify(eventInfo));
  })
  .catch((error: string) => {
    // handle error such as when user rejected permissions
    console.warn(error);
  });
```

