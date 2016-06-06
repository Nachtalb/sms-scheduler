package ch.bbcag.bespin.smsscheduler;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;

/**
 * The Alarm receiver for sending Messages
 */
public class AlarmReceiver extends BroadcastReceiver {
    /**
     * The String under which the scheduled SMSs will be saved in the shred preferences
     */
    private static final String SCHEDULEDSMS = "ch.bbcag.bespin.smsscheduler.sheduledSms";

    /**
     * This method is called when the BroadcastReceiver is receiving an Intent broadcast.
     *
     * @param context - The Context in which the receiver is running.
     * @param intent  - The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getExtras().getString("title");
        String phoneNr = intent.getExtras().getString("phoneNr");
        String smsText = intent.getExtras().getString("smsText");
        String UUID = intent.getExtras().getString("UUID");

        // For our recurring task, we'll just display a message
        Toast.makeText(context, "ScheduleSending SMS: " + title, Toast.LENGTH_SHORT).show();
        String notificationText;
        String notificationTitle;
        int notificationDrawable;

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNr, null, smsText, null, null);
            changeTitle(UUID, context);
            notificationTitle = "SMS sent";
            notificationText = "\"" + title + "\" sent successfully!";
            notificationDrawable = R.drawable.ic_sms_white_48dp;
        } catch (Exception e) {
            notificationTitle = "SMS Fail";
            notificationText = "There was an error while sending \"" + title + "\"!";
            notificationDrawable = R.drawable.ic_sms_failed_white_48dp;
        }

        issueNotification(context, notificationText, notificationTitle, notificationDrawable);
    }

    /**
     * Issues a Notification
     *
     * @param context              - The Context to access application-specific resources and classes.
     * @param notificationText     - The Notification Text
     * @param notificationTitle    - The Notification Title
     * @param notificationDrawable - The Notification Icon
     */
    private void issueNotification(Context context, String notificationText, String notificationTitle, int notificationDrawable) {
        try {
            NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(notificationDrawable)
                    .setContentTitle("SMS-Scheduler: " + notificationTitle)
                    .setContentText(notificationText);
            Intent resultIntent = new Intent(context, MainActivity.class);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );

            mBuilder.setContentIntent(resultPendingIntent);

            int mNotificationId = (int) System.currentTimeMillis();
            mNotifyMgr.notify(mNotificationId, mBuilder.build());
        } catch (Exception e) {
            System.err.println("Caught Exception: " + e.getMessage());
        }
    }

    /**
     * Adds "(sent)" to the title of an SMS
     *
     * @param UUID    - The UUID of the SMS
     * @param context - The Context to access application-specific resources and classes.
     */
    private void changeTitle(String UUID, Context context) {
        HashMap<String, ScheduledSms> scheduledSms;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        try {
            scheduledSms = (HashMap<String, ScheduledSms>) ObjectSerializer.deserialize(prefs.getString(SCHEDULEDSMS, ObjectSerializer.serialize(new HashMap<String, ScheduledSms>())));
            scheduledSms.get(UUID).title = scheduledSms.get(UUID).title + " (Sent)";
            editor.putString(SCHEDULEDSMS, ObjectSerializer.serialize(scheduledSms));
            editor.apply();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}