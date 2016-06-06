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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Main activity. Where everything starts.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * The request code supplied in the startActivityForResult for an add request
     */
    static final int ADD_REQUEST = 0;
    /**
     * The request code supplied in the startActivityForResult for an update request
     */
    static final int UPDATE_REQUEST = 1;
    /**
     * The String under which the newest PendingIntent ID will be saved. This is to prevent giving
     * the PendingIntents same IDs
     */
    private static final String PENDINGINTENTID = "ch.bbcag.bespin.smsscheduler.pendingIntentId";
    /**
     * The String under which the scheduled SMSs will be saved in the shred preferences
     */
    private static final String SCHEDULEDSMS = "ch.bbcag.bespin.smsscheduler.sheduledSms";
    /**
     * A HashMap with all scheduled SMSs
     */
    public static HashMap<String, ScheduledSms> scheduledSms = new HashMap<>();
    /**
     * SharedPreferences variable which will be used in the whole project.
     */
    public SharedPreferences prefs;
    /**
     * An ArrayList with all list Items for the MainActivity
     */
    private ArrayList<RowItem> rowItems = new ArrayList<>();

    /**
     * Perform initialization of all fragments and loaders.
     *
     * @param savedInstanceState - If the activity is being re-initialized after previously being
     *                           shut down then this Bundle contains the data it most recently
     *                           supplied in onSaveInstanceState. Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        reloadList();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newEditSmsActivity();
            }
        });
    }

    /**
     * Loads the EditSms Activity
     */
    private void newEditSmsActivity() {
        Intent intent = new Intent(getApplicationContext(), EditSms.class);
        startActivityForResult(intent, ADD_REQUEST);
    }

    /**
     * Loads the Update SMS Activity
     *
     * @param UUID - UUID of the SMS
     */
    private void updateEditSmsActivity(String UUID) {
        ScheduledSms sms = scheduledSms.get(UUID);

        Intent intent = new Intent(getApplicationContext(), EditSms.class);
        intent.putExtra("title", sms.title);
        intent.putExtra("phoneNr", sms.phoneNr);
        intent.putExtra("smsText", sms.smsText);
        intent.putExtra("timestamp", sms.timestamp);
        intent.putExtra("UUID", sms.UUID);

        startActivityForResult(intent, UPDATE_REQUEST);
    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param reqCode    - The integer request code originally supplied to startActivityForResult(), allowing you to identify who this result came from.
     * @param resultCode - The integer result code returned by the child activity through its setResult().
     * @param data       - An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
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
                    if (data.getStringExtra("delete") != null) {
                        Toast.makeText(this, "Adding SMS canceled.", Toast.LENGTH_LONG);
                    }
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

    /**
     * Updates an existing SMS
     *
     * @param title     - New Title
     * @param phoneNr   - New PhoneNumber
     * @param smsText   - new SMS Text
     * @param timestamp - New Time as UnixTimestamp
     * @param uuid      - The UUID of the existing SMS
     */
    private void updateSms(String title, String phoneNr, String smsText, long timestamp, String uuid) {
        deleteSms(uuid);
        addSms(title, phoneNr, smsText, timestamp);

        Snackbar.make(getCurrentFocus(), title + " updated", Snackbar.LENGTH_LONG).show();
    }

    /**
     * Adds a SMS to the SMS List and reloads the MainActivity List
     *
     * @param title         - Title of the new SMS
     * @param phoneNr       - PhoneNumber of the new SMS
     * @param smsText       - Text of the SMS
     * @param unixTimestamp - Unix Timestamp
     */
    public void addSms(String title, String phoneNr, String smsText, long unixTimestamp) {
        int pendingIntentId = getNewPendingIntentId();
        ScheduledSms newSms = new ScheduledSms(title, phoneNr, smsText, unixTimestamp, UUID.randomUUID().toString(), pendingIntentId);

        scheduledSms.put(newSms.UUID, newSms);

        if (newSms.timestamp >= System.currentTimeMillis())
            addPendingSMS(newSms);

        updateSharedPreferences();
        reloadList();
    }

    /**
     * Updates the shared preferences with the scheduledSms Hash Array
     */
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

    /**
     * Lets the PendingIntent create ands associates it with the AlarmManager
     *
     * @param sms - ScheduledSms
     */
    private void addPendingSMS(ScheduledSms sms) {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        long unixTimestamp = sms.timestamp;
        PendingIntent pendingIntent = getPendingIntent(sms.UUID);

        manager.setExact(AlarmManager.RTC, unixTimestamp, pendingIntent);
    }

    /**
     * Gets the PendingIntent ID for the next sms
     *
     * @return - The new PendingIntent ID
     */
    public int getNewPendingIntentId() {
        int id = prefs.getInt(PENDINGINTENTID, -1);
        return (id == -1) ? 0 : id + 1;
    }

    /**
     * Deletes an SMS from shared preferences and removes the PendingIntent
     *
     * @param UUID - UUID of the SMS
     */
    public void deleteSms(String UUID) {
        PendingIntent pendingIntent = scheduledSms.get(UUID).getPendingIntent(this, scheduledSms.get(UUID).pendingIntentId);
        pendingIntent.cancel();

        if (scheduledSms.get(UUID).timestamp >= System.currentTimeMillis()) {
            PendingIntent pendingIntent = getPendingIntent(UUID);
            pendingIntent.cancel();
        }

        String title = scheduledSms.get(UUID).title;
        scheduledSms.remove(UUID);

        updateSharedPreferences();
        reloadList();

        Snackbar.make(getCurrentFocus(), title + " deleted", Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Checks if the Shared Preferences contains some scheduled SMS or not
     *
     * @return - True or False
     */
    public boolean checkIfHasEntries() {
        return prefs.getString(SCHEDULEDSMS, null) != null;
    }

    /**
     * Reloads the list in the MainActivityView
     */
    public void reloadList() {
        ListView smsList = (ListView) findViewById(R.id.plannedSmsList);
        rowItems.clear();

        AdapterView.OnItemClickListener mListClickedHandler = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                updateEditSmsActivity(rowItems.get(position).UUID);
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

    /**
     * Returns the new PendingIntent for the SMS
     *
     * @param UUID - UUID of the SMS
     * @return - PendingIntent of the SMS
     */
    public PendingIntent getPendingIntent(String UUID) {

        PendingIntent pendingIntent;

        Intent alarmIntent = new Intent(this, AlarmReceiver.class);

        alarmIntent.putExtra("title", scheduledSms.get(UUID).title);
        alarmIntent.putExtra("phoneNr", scheduledSms.get(UUID).phoneNr);
        alarmIntent.putExtra("smsText", scheduledSms.get(UUID).smsText);
        alarmIntent.putExtra("UUID", UUID);

        pendingIntent = PendingIntent.getBroadcast(
                this,
                scheduledSms.get(UUID).pendingIntentId,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        return pendingIntent;
    }
}