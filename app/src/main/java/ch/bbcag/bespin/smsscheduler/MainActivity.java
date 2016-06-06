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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    static final int ADD_REQUEST = 0;
    static final int UPDATE_REQUEST = 1;
    private static final String PENDINGINTENTID = "pendingIntentId";
    private static final String SCHEDULEDSMS = "ch.bbcag.bespin.smsscheduler.sheduledSms";
    public SharedPreferences prefs;
    private HashMap<String, ScheduledSms> scheduledSms = new HashMap<>();
    private ArrayList<RowItem> rowItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        createList();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newEditSmsView();
            }
        });
    }

    private void newEditSmsView() {
        Intent intent = new Intent(getApplicationContext(), EditSms.class);
        startActivityForResult(intent, ADD_REQUEST);
    }

    private void updateEditSmsView(String UUID) {
        ScheduledSms sms = scheduledSms.get(UUID);

        Intent intent = new Intent(getApplicationContext(), EditSms.class);
        intent.putExtra("title", sms.title);
        intent.putExtra("phoneNr", sms.phoneNr);
        intent.putExtra("smsText", sms.smsText);
        intent.putExtra("timestamp", sms.timestamp);
        intent.putExtra("UUID", sms.UUID);

        startActivityForResult(intent, UPDATE_REQUEST);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (data != null) {

            String title;
            String phoneNr;
            String smsText;
            String UUID;
            long timestamp;

            switch (reqCode) {
                case ADD_REQUEST:
                    title = data.getStringExtra("title");
                    phoneNr = data.getStringExtra("phoneNr");
                    smsText = data.getStringExtra("smsText");
                    timestamp = data.getLongExtra("timestamp", 0);
                    if (timestamp == 0) {
                        Toast.makeText(this, "There was an error while adding new sms, please try again", Toast.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(getCurrentFocus(), title + " added.", Snackbar.LENGTH_LONG).show();
                        addSms(title, phoneNr, smsText, timestamp);
                    }
                    break;
                case UPDATE_REQUEST:

                    if (data.getStringExtra("delete") != null) {
                        deleteSms(data.getStringExtra("UUID"));
                    } else {
                        title = data.getStringExtra("title");
                        phoneNr = data.getStringExtra("phoneNr");
                        smsText = data.getStringExtra("smsText");
                        timestamp = data.getLongExtra("timestamp", 0);
                        UUID = data.getStringExtra("UUID");

                        Log.i("TEST", title + " | " + phoneNr + " | " + smsText + " | " + timestamp + " | " + UUID);

                        if (timestamp == 0) {
                            Toast.makeText(this, "There was an error while updating sms, please try again", Toast.LENGTH_SHORT).show();
                        } else {
                            updateSms(title, phoneNr, smsText, timestamp, UUID);
                        }
                    }
                    break;
                default:
                    Toast.makeText(this, "There was an error, please try again", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void updateSms(String title, String phoneNr, String smsText, long timestamp, String uuid) {
        deleteSms(uuid);

        addSms(title, phoneNr, smsText, timestamp);
        Snackbar.make(getCurrentFocus(), title + " updated", Snackbar.LENGTH_LONG).show();
    }

    public void addSms(String title, String phoneNr, String smsText, long unixTimestamp) {
        int pendingIntentId = getNewPendingIntentId();

        ScheduledSms newSms = new ScheduledSms(title, phoneNr, smsText, unixTimestamp, UUID.randomUUID().toString(), pendingIntentId);

        scheduledSms.put(newSms.UUID, newSms);


        addPendingSMS(newSms, pendingIntentId);

        //save the task list to preference
        updateSharedPreferences();
        createList();
    }

    private void updateSharedPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

        if (scheduledSms.isEmpty()) {
            editor.clear();
        } else {

            try {
                editor.putInt(PENDINGINTENTID, getNewPendingIntentId());
                editor.putString(SCHEDULEDSMS, ObjectSerializer.serialize(scheduledSms));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        editor.apply();
    }

    private PendingIntent addPendingSMS(ScheduledSms sms, int pendingIntentId) {
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

    public void deleteSms(String UUID) {

        PendingIntent pendingIntent = scheduledSms.get(UUID).getPendingIntent(this, scheduledSms.get(UUID).pendingIntentId);
        pendingIntent.cancel();

        String title = scheduledSms.get(UUID).title;
        scheduledSms.remove(UUID);

        updateSharedPreferences();
        createList();

        Snackbar.make(getCurrentFocus(), title + " deleted", Snackbar.LENGTH_SHORT).show();
    }

    public boolean checkIfHasEntries() {
        return prefs.getString(SCHEDULEDSMS, null) != null;
    }

    public void createList() {
        ListView smsList = (ListView) findViewById(R.id.plannedSmsList);
        rowItems.clear();

        AdapterView.OnItemClickListener mListClickedHandler = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                updateEditSmsView(rowItems.get(position).UUID);
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
            RowItem item = new RowItem("No scheduled items yet", "", "", 0, UUID.randomUUID().toString());
            rowItems.add(item);
        }

        CustomAdapter adapter = new CustomAdapter(this, rowItems);
        smsList.setAdapter(adapter);
    }
}
