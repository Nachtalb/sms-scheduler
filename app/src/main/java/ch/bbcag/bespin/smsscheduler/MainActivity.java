package ch.bbcag.bespin.smsscheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private HashMap<String, ScheduledSms> scheduledSms = new HashMap<>();
    private static final String TAG = "ApplicationStart";
    final String SCHEDULEDSMS = "ch.bbcag.bespin.smsscheduler.sheduledSms";
    public SharedPreferences prefs;

    ArrayList<RowItem> rowItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        createList();

        addTestSms();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }

    public void addSms(String title, String phoneNr, String smsText, long unixTimestamp) {
        ScheduledSms newSms = new ScheduledSms(title, phoneNr, smsText, unixTimestamp, UUID.randomUUID().toString());

        scheduledSms.put(newSms.UUID, newSms);

        addPendingSMS(newSms);

        //save the task list to preference
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        try {
            editor.putString(SCHEDULEDSMS, ObjectSerializer.serialize(scheduledSms));
        } catch (Exception e) {
            e.printStackTrace();
        }
        editor.apply();

    }


    public PendingIntent addPendingSMS(ScheduledSms sms) {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        long unixTimestamp = sms.timestamp;
        PendingIntent pendingIntent = sms.getPendingIntent(this);

        manager.setExact(AlarmManager.RTC, unixTimestamp, pendingIntent);

        return pendingIntent;
    }

    public void cancelSms(String UUID) {

        PendingIntent pendingIntent = scheduledSms.get(UUID).getPendingIntent(this);

        pendingIntent.cancel();

        scheduledSms.remove(UUID);

        createList();
    }

    public boolean checkIfHasEntries(){
        return prefs.getString(SCHEDULEDSMS, null) != null;
    }

    private void createList() {
        ListView smsList = (ListView) findViewById(R.id.plannedSmsList);
        smsList.setAdapter(null);

        AdapterView.OnItemClickListener mListClickedHandler = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), EditSms.class);
                String selected = parent.getItemAtPosition(position).toString();

                intent.putExtra("name", selected);
                startActivity(intent);
            }
        };

        if (checkIfHasEntries()) {
            try {
                scheduledSms = (HashMap<String, ScheduledSms>) ObjectSerializer.deserialize(prefs.getString(SCHEDULEDSMS, ObjectSerializer.serialize(new HashMap<String, ScheduledSms>())));
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!scheduledSms.isEmpty()) {
                for (Map.Entry<String, ScheduledSms> sms : scheduledSms.entrySet()) {
                    RowItem item = new RowItem(sms.getValue().title, sms.getValue().phoneNr, sms.getValue().smsText, sms.getValue().timestamp, sms.getValue().UUID);
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
        addSms("Tobi", "+41793030111", "Tobias isch e Hobbyglobi", System.currentTimeMillis() + 3000);
        addSms("Roman", "+41796564172", "Roman isch de ruler of his class", System.currentTimeMillis() + 6000);
        addSms("Hudson", "+41786224306", "Hudson isch het e huet", System.currentTimeMillis() + 9000);

        createList();
    }
}
