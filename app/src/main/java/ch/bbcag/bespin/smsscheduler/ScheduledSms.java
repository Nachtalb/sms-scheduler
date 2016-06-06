package ch.bbcag.bespin.smsscheduler;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.io.Serializable;

public class ScheduledSms implements Serializable {
    public String title;
    public String phoneNr;
    public String smsText;
    public long timestamp;
    public String UUID;
    public int pendingIntentId;

    public ScheduledSms(String title, String phoneNr, String smsText, long timestamp, String uniqueId, int pendingIntentId) {
        this.title = title;
        this.phoneNr = phoneNr;
        this.smsText = smsText;
        this.timestamp = timestamp;
        this.UUID = uniqueId;
        this.pendingIntentId = pendingIntentId;
    }

    public PendingIntent getPendingIntent(Context context, int pendingIntentId) {

        PendingIntent pendingIntent;

        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.putExtra("title", title);
        alarmIntent.putExtra("phoneNr", phoneNr);
        alarmIntent.putExtra("smsText", smsText);
        alarmIntent.putExtra("UUID", UUID);

        pendingIntent = PendingIntent.getBroadcast(context, pendingIntentId, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }
}
