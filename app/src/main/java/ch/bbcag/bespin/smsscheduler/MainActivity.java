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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private HashMap<String, ScheduledSms> scheduledSms = new HashMap<>();
    private static final String TAG = "ApplicationStart";
    private static final String PENDINGINTENTID = "pendingIntentId";
    final String SCHEDULEDSMS = "ch.bbcag.bespin.smsscheduler.sheduledSms";
    public SharedPreferences prefs;

    ArrayList<RowItem> rowItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        createList();
//        addTestSms();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Create Scheduled SMS", Snackbar.LENGTH_LONG).setAction("New", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        newEditSmsView();
                    }
                }).show();
            }
        });
    }

    private void newEditSmsView() {
        Intent intent = new Intent(getApplicationContext(), EditSms.class);
        startActivity(intent);
    }

    private void updateEditSmsView(String UUID){
        ScheduledSms sms = scheduledSms.get(UUID);

        Intent intent = new Intent(getApplicationContext(), EditSms.class);
        intent.putExtra("UUID", sms.UUID);

        startActivity(intent);
    }

    private void updateSms(String UUID) {

    }

    public void addSms(String title, String phoneNr, String smsText, long unixTimestamp) {
        int pendingIntentId = getNewPendingIntentId();

        ScheduledSms newSms = new ScheduledSms(title, phoneNr, smsText, unixTimestamp, UUID.randomUUID().toString(), pendingIntentId);

        scheduledSms.put(newSms.UUID, newSms);


        addPendingSMS(newSms, pendingIntentId);

        //save the task list to preference
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        try {
            editor.putInt(PENDINGINTENTID, pendingIntentId);
            editor.putString(SCHEDULEDSMS, ObjectSerializer.serialize(scheduledSms));
        } catch (Exception e) {
            e.printStackTrace();
        }
        editor.apply();
    }

    public PendingIntent addPendingSMS(ScheduledSms sms, int pendingIntentId) {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        long unixTimestamp = sms.timestamp;
        PendingIntent pendingIntent = sms.getPendingIntent(this, pendingIntentId);

        manager.setExact(AlarmManager.RTC, unixTimestamp, pendingIntent);

        return pendingIntent;
    }

    public int getPendingIntentIdCount() {
        return prefs.getInt(PENDINGINTENTID, -1);
    }

    public int getNewPendingIntentId() {
        int id = getPendingIntentIdCount();
        return (id == -1) ? 0 : id + 1;
    }

    public void cancelSms(String UUID) {

        PendingIntent pendingIntent = scheduledSms.get(UUID).getPendingIntent(this, scheduledSms.get(UUID).pendingIntentId);

        pendingIntent.cancel();

        scheduledSms.remove(UUID);

        createList();
    }

    public boolean checkIfHasEntries() {
        return prefs.getString(SCHEDULEDSMS, null) != null;
    }

    private void createList() {
        ListView smsList = (ListView) findViewById(R.id.plannedSmsList);
        rowItems.clear();

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
