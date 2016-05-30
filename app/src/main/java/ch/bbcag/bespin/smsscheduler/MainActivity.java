package ch.bbcag.bespin.smsscheduler;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private ArrayList<ScheduledSms> scheduledSms;
    private static final String TAG = "ApplicationStart";
    final String SCHEDULEDSMS = "ch.bbcag.bespin.smsscheduler.sheduledSms";

    ArrayList<RowItem> rowItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        createList(prefs);

        startIn10Sec();
    }

    public void startIn10Sec() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        long time = System.currentTimeMillis() + 10000;

        /* Repeating on every 20 minutes interval */
        PendingIntent pendingIntent;

        Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);

        manager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
    }


    public void addSms(ScheduledSms newSms) {
        if (null == scheduledSms) {
            scheduledSms = new ArrayList<>();
        }

        scheduledSms.add(newSms);

        //save the task list to preference
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        try {
            editor.putString(SCHEDULEDSMS, ObjectSerializer.serialize(scheduledSms));
        } catch (Exception e) {
            e.printStackTrace();
        }
        editor.commit();
    }

    private void createList(SharedPreferences prefs) {
        // load tasks from preference
        try {
            scheduledSms = (ArrayList<ScheduledSms>) ObjectSerializer.deserialize(prefs.getString(SCHEDULEDSMS, ObjectSerializer.serialize(new ArrayList<ScheduledSms>())));
        } catch (Exception e) {
            e.printStackTrace();
        }

//        addTestSms();

        ListView smsList = (ListView) findViewById(R.id.plannedSmsList);

        AdapterView.OnItemClickListener mListClickedHandler = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), EditSms.class);
                String selected = parent.getItemAtPosition(position).toString();

                intent.putExtra("name", selected);
                startActivity(intent);
            }
        };

        if (prefs.getString(SCHEDULEDSMS, null) != null) {
            try {
                scheduledSms = (ArrayList<ScheduledSms>) ObjectSerializer.deserialize(prefs.getString(SCHEDULEDSMS, ObjectSerializer.serialize(new ArrayList<ScheduledSms>())));
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!scheduledSms.isEmpty()) {
                for (ScheduledSms sms : scheduledSms) {
                    RowItem item = new RowItem(sms.title, sms.phoneNr, sms.smsText, sms.timestamp, sms.uniqueId);
                    rowItems.add(item);
                }
            }

            smsList.setOnItemClickListener(mListClickedHandler);
        } else {
            Log.i(TAG, "here");

            RowItem item = new RowItem("No scheduled items yet", "", "", 0, UUID.randomUUID().toString());
            rowItems.add(item);
        }

        CustomAdapter adapter = new CustomAdapter(this, rowItems);
        smsList.setAdapter(adapter);
    }


    public void addTestSms() {
        addSms(new ScheduledSms("First Entry", "0111111111", "some Text", 1465732800, UUID.randomUUID().toString()));
        addSms(new ScheduledSms("Second Entry", "0222222222", "Some other Text", 1469871000, UUID.randomUUID().toString()));
        addSms(new ScheduledSms("Third Entry", "0333333333", "Something different", 1482537600, UUID.randomUUID().toString()));
        addSms(new ScheduledSms("Fourth Entry", "0444444444", "Lorem Ipsum", 1483697520, UUID.randomUUID().toString()));
    }
}
