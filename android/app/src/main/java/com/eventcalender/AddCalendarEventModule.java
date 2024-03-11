package com.eventcalender;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.app.LoaderManager;
import android.content.Loader;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.*;

import java.util.HashMap;
import java.util.Map;

import static com.eventcalender.Utils.doesEventExist;
import static com.eventcalender.Utils.extractLastEventId;
import static com.eventcalender.Utils.getTimestamp;

 import com.facebook.react.modules.core.DeviceEventManagerModule;


public class AddCalendarEventModule extends ReactContextBaseJavaModule implements ActivityEventListener, LoaderManager.LoaderCallbacks {

    public static final String ADD_EVENT_MODULE_NAME = "AddCalendarEvent";
    private static final int ADD_EVENT_REQUEST_CODE = 11;
    private static final int SHOW_EVENT_REQUEST_CODE = 12;
    private static final int PRIOR_RESULT_ID = 1;
    private static final int POST_RESULT_ID = 2;
    private Promise promise;
    private Long eventPriorId;
    private Long shownOrEditedEventId;

    private static final String DELETED = "DELETED";
    private static final String SAVED = "SAVED";
    private static final String CANCELED = "CANCELED";
    private static final String DONE = "DONE";
    private static final String RESPONDED = "RESPONDED";


//for event emitter
    private static final String EVENT_SAVED = "EventSaved";
private static final String EVENT_DELETED = "EventDeleted";
private static final String EVENT_REJECTED = "EventRejected";


private Intent savedIntent;


    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put(DELETED, DELETED);
        constants.put(SAVED, SAVED);
        constants.put(CANCELED, CANCELED);
        constants.put(DONE, DONE);
        constants.put(RESPONDED, RESPONDED);
        return constants;
    }


    public AddCalendarEventModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addActivityEventListener(this);
        resetMembers();
    }

    private void resetMembers() {
        promise = null;
        eventPriorId = 0L;
        shownOrEditedEventId = 0L;
    }

    private boolean isEventBeingEdited() {
        return shownOrEditedEventId != 0L;
    }

    @Override
    public String getName() {
        return ADD_EVENT_MODULE_NAME;
    }

    @ReactMethod
    public void addEventToCalendar(ReadableMap config, Promise eventPromise) {
        promise = eventPromise;

        this.addEventToCalendarActivity(config);
    }

    private void addEventToCalendarActivity(ReadableMap config) {
        try {
            setPriorEventId(getCurrentActivity());

            final Intent calendarIntent = new Intent(Intent.ACTION_INSERT);
            savedIntent=calendarIntent;
            calendarIntent
                    .setType("vnd.android.cursor.item/event")
                    .putExtra("title", config.getString("title"));

            if (config.hasKey("startDate")) {
                calendarIntent.putExtra("beginTime", getTimestamp(config.getString("startDate")));
            }

            if (config.hasKey("endDate")) {
                calendarIntent.putExtra("endTime", getTimestamp(config.getString("endDate")));
            }

            if (config.hasKey("location")
                    && config.getString("location") != null) {
                calendarIntent.putExtra("eventLocation", config.getString("location"));
            }

            if (config.hasKey("notes")
                    && config.getString("notes") != null) {
                calendarIntent.putExtra("description", config.getString("notes"));
            }

            if (config.hasKey("allDay")) {
                calendarIntent.putExtra("allDay", config.getBoolean("allDay"));
            }


            getReactApplicationContext().startActivityForResult(calendarIntent, ADD_EVENT_REQUEST_CODE, Bundle.EMPTY);
        } catch (Exception e) {
            rejectPromise(e);
        }
    }



    // Method to emit event when an event is saved
private void emitEventSaved(WritableMap result) {
    getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(EVENT_SAVED, result);
}

// Method to emit event when an event is deleted
private void emitEventDeleted(WritableMap result) {
    getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(EVENT_DELETED, result);
}


    @ReactMethod
    public void createCalendarEvent() {
        Toast.makeText(getReactApplicationContext(), "Event called", Toast.LENGTH_SHORT).show();
    }


    @ReactMethod
    public void editAddedEvent(ReadableMap config, Promise eventPromise) {
        promise = eventPromise;
        boolean shouldUseEditIntent = config.hasKey("useEditIntent") && config.getBoolean("useEditIntent");
        Intent intent = new Intent(shouldUseEditIntent ? Intent.ACTION_EDIT : Intent.ACTION_VIEW);

        this.editAddedEventActivity(config, intent);
    }

    @ReactMethod
    public void addedEventViewing(ReadableMap config, Promise eventPromise) {
        promise = eventPromise;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        this.editAddedEventActivity(config, intent);
    }

    private void editAddedEventActivity(ReadableMap config, Intent intent) {
        String eventIdString = config.getString("eventId");
        if (!doesEventExist(getReactApplicationContext().getContentResolver(), eventIdString)) {
            rejectPromise("event with id " + eventIdString + " not found");
            return;
        }
        shownOrEditedEventId = Long.valueOf(eventIdString);
        Uri eventUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, shownOrEditedEventId);

        setPriorEventId(getCurrentActivity());

        intent.setData(eventUri);

        try {
            getReactApplicationContext().startActivityForResult(intent, SHOW_EVENT_REQUEST_CODE, Bundle.EMPTY);
        } catch (Exception e) {
            rejectPromise(e);
        }
    }

    private void setPriorEventId(Activity activity) {
        if (activity != null) {
            activity.getLoaderManager().initLoader(PRIOR_RESULT_ID, null, this);
        }
    }

    // @Override
    // public void onActivityResult(Activity activity, final int requestCode, final int resultCode, final Intent intent) {
    //     if ((requestCode != ADD_EVENT_REQUEST_CODE && requestCode != SHOW_EVENT_REQUEST_CODE) || promise == null) {
    //         return;
    //     }
    //     setPostEventId(activity);
    // }

    @Override
public void onActivityResult(Activity activity, final int requestCode, final int resultCode, final Intent intent) {
    if ((requestCode != ADD_EVENT_REQUEST_CODE && requestCode != SHOW_EVENT_REQUEST_CODE) || promise == null) {
        return;
    }
     Toast.makeText(getReactApplicationContext(), "Event called"+savedIntent, Toast.LENGTH_SHORT).show();
    setPostEventId(activity);
    // Store the intent object
  
}


    private void setPostEventId(Activity activity) {
        if (activity != null) {
            activity.getLoaderManager().initLoader(POST_RESULT_ID, null, this);
        }
    }

    // TODO get rid of the loaders?
    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getReactApplicationContext(),
                CalendarContract.Events.CONTENT_URI,
                new String[]{"MAX(_id) as max_id"}, null, null, "_id");
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        Cursor cursor = (Cursor) data;
        if (cursor.isClosed()) {
            Log.d(ADD_EVENT_MODULE_NAME, "cursor was closed; loader probably wasn't destroyed previously (destroyLoader() failed)");
            rejectPromise("cursor was closed");
            return;
        }
        Long lastEventId = extractLastEventId(cursor);

        if (loader.getId() == PRIOR_RESULT_ID) {
            eventPriorId = lastEventId;
        } else if (loader.getId() == POST_RESULT_ID) {
              WritableMap eventDetails = getEventDetails(lastEventId);
            returnResultBackToJS(lastEventId,eventDetails);
        }

        destroyLoader(loader);
    }

    private void returnResultBackToJS(@Nullable Long eventPostId,WritableMap eventDetails) {
        if (promise == null) {
            Log.e(ADD_EVENT_MODULE_NAME, "promise is null");
            return;
        }

        if (eventPriorId == null || eventPostId == null) {
            promise.reject(ADD_EVENT_MODULE_NAME, "event prior and/or post id were null, extractLastEventId probably encountered a problem");
        } else {
            determineActionAndResolve(eventPriorId, eventPostId,eventDetails);
        }
        resetMembers();
    }

    private void determineActionAndResolve(long priorId, long postId,WritableMap eventDetails) {
        ContentResolver cr = getReactApplicationContext().getContentResolver();

        boolean wasNewEventCreated = postId > priorId;
        boolean doesPostEventExist = doesEventExist(cr, postId);

        WritableMap result = Arguments.createMap();
        String eventId = String.valueOf(postId);
        if (doesPostEventExist && wasNewEventCreated) {
            result.putString("eventIdentifier", eventId);
            result.putString("calendarItemIdentifier", eventId);
            result.putString("action", SAVED);
            //    result.putString("updatedTitle", updatedTitle);
        result.putString("title", eventDetails.getString("title"));
        result.putString("description", eventDetails.getString("description"));
        result.putString("location", eventDetails.getString("location"));
        result.putDouble("startTime", eventDetails.getDouble("startTime"));
        result.putDouble("endTime", eventDetails.getDouble("endTime"));
        //emitEventSaved(result); // Emitting event when event is saved
        } else if (!isEventBeingEdited() || doesEventExist(cr, shownOrEditedEventId)) {
            // NOTE you'll get here even when you edit and save an existing event
            result.putString("action", CANCELED);
              result.putString("title", eventDetails.getString("title"));
        result.putString("description", eventDetails.getString("description"));
        result.putString("location", eventDetails.getString("location"));
        result.putDouble("startTime", eventDetails.getDouble("startTime"));
        result.putDouble("endTime", eventDetails.getDouble("endTime"));
                // result.putString("updatedTitle", updatedTitle);
                
        } else {
            result.putString("action", DELETED);
             //emitEventDeleted(result); // Emitting event when event is canceled
        }
        promise.resolve(result);
    }

private WritableMap getEventDetails(long eventId) {
     // Define the projection to specify which columns you want to retrieve
    String[] projection = new String[]{
        CalendarContract.Events.TITLE,
        CalendarContract.Events.DESCRIPTION,
        CalendarContract.Events.EVENT_LOCATION,
        CalendarContract.Events.DTSTART,
        CalendarContract.Events.DTEND
    };

    // Construct the query URI for the event with the given ID
    Uri eventUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);

    // Query the calendar provider for the event details
    Cursor cursor = getReactApplicationContext().getContentResolver().query(eventUri, projection, null, null, null);

   // Create a WritableMap to hold the event details
    WritableMap eventDetailsMap = Arguments.createMap();

    // Check if the cursor is not null and contains data
    if (cursor != null && cursor.moveToFirst()) {
        // Retrieve the event details from the cursor
        String title = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.TITLE));
        String description = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.DESCRIPTION));
        String location = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.EVENT_LOCATION));
        long startTime = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTSTART));
        long endTime = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTEND));

        // Put the event details into the WritableMap
        eventDetailsMap.putString("title", title);
        eventDetailsMap.putString("description", description);
        eventDetailsMap.putString("location", location);
        eventDetailsMap.putDouble("startTime", startTime);
        eventDetailsMap.putDouble("endTime", endTime);

        return eventDetailsMap;
    }

    return null;
}


    private void rejectPromise(Exception e) {
        rejectPromise(e.getMessage());
    }

    private void rejectPromise(String e) {
        if (promise == null) {
            Log.e(ADD_EVENT_MODULE_NAME, "promise is null");
            return;
        }
        promise.reject(ADD_EVENT_MODULE_NAME, e);
        resetMembers();
    }

    private void destroyLoader(Loader loader) {
        // if loader isn't destroyed, onLoadFinished() gets called multiple times for some reason
        Activity activity = getCurrentActivity();
        if (activity != null) {
            activity.getLoaderManager().destroyLoader(loader.getId());
        } else {
            Log.d(ADD_EVENT_MODULE_NAME, "activity was null when attempting to destroy the loader");
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
    }

    @Override
    public void onNewIntent(Intent intent) {
    }
}
