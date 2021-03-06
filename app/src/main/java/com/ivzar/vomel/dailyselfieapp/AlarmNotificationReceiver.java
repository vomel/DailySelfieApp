package com.ivzar.vomel.dailyselfieapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by vomel on 27/01/2017.
 */

public class AlarmNotificationReceiver extends BroadcastReceiver {
    // Notification ID to allow for future updates
    private static final int MY_NOTIFICATION_ID = 1;
    private static final String TAG = "AlarmNotificationReceiver";

    // Notification Text Elements
    private final CharSequence tickerText = "Are You Playing Angry Birds Again!";
    private final CharSequence contentTitle = "Daily Selfie";
    private final CharSequence contentText = "Time for another selfie!!";

    // Notification Action Elements
    private Intent mNotificationIntent;
    private PendingIntent mContentIntent;

    // Notification Sound and Vibration on Arrival
    private final long[] mVibratePattern = {0, 200, 200, 300};

    @Override
    public void onReceive(Context context, Intent intent) {

        // The Intent to be used when the user clicks on the Notification View
        mNotificationIntent = new Intent(context, MainActivity.class);

        // The PendingIntent that wraps the underlying Intent
        mContentIntent = PendingIntent.getActivity(context, 0, mNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build the Notification
        Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setSmallIcon(android.R.drawable.ic_menu_camera)
                .setAutoCancel(true).setContentTitle(contentTitle)
                .setContentText(contentText).setContentIntent(mContentIntent)
                .setVibrate(mVibratePattern);

        // Get the NotificationManager
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        // Pass the Notification to the NotificationManager:
        Notification notification = notificationBuilder.build();
        notification.defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND;
        mNotificationManager.notify(MY_NOTIFICATION_ID, notification);

        // Log occurrence of notify() call
        Log.i(MainActivity.TAG, "Sending notification at:" + DateFormat.getDateTimeInstance().format(new Date()));
    }
}

