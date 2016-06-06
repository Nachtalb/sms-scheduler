package ch.bbcag.bespin.smsscheduler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getExtras().getString("title");
        String phoneNr = intent.getExtras().getString("phoneNr");
        String smsText = intent.getExtras().getString("smsText");

        // For our recurring task, we'll just display a message
        Toast.makeText(context, "ScheduleSending SMS: " + title, Toast.LENGTH_SHORT).show();

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNr, null, smsText, null, null);
    }


}