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

    public ScheduledSms(String title, String phoneNr, String smsText, long timestamp, String uniqueId) {
        this.title = title;
        this.phoneNr = phoneNr;
        this.smsText = smsText;
        this.timestamp = timestamp;
        this.UUID = uniqueId;
    }

    public PendingIntent getPendingIntent(Context context){

        PendingIntent pendingIntent;

        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.putExtra("title", title);
        alarmIntent.putExtra("phoneNr", phoneNr);
        alarmIntent.putExtra("smsText", smsText);

        pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_ONE_SHOT);

        return pendingIntent;
    }
}
