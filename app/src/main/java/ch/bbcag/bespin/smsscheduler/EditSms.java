package ch.bbcag.bespin.smsscheduler;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditSms extends AppCompatActivity implements View.OnClickListener {

    private static final int PICK_CONTACT = 1;

    // UI References
    private EditText title;
    private EditText phoneNr;
    private EditText smsText;
    private EditText time;
    private EditText date;

    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private SimpleDateFormat dateFormatter;
    private SimpleDateFormat timeFormatter;
    private MainActivity mainActivity;

    private Calendar newDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_sms);
        findViewsById();
        setOnClickListener();

        newDate = Calendar.getInstance();
        timeFormatter = new SimpleDateFormat("HH:mm");
        dateFormatter = new SimpleDateFormat("dd.MM.yyyy");

        Intent i = getIntent();
        if (i != null && i.getLongExtra("timestamp", -1) != -1) {
            newDate.setTimeInMillis(i.getLongExtra("timestamp", Calendar.getInstance().getTimeInMillis()));
            time.setText(timeFormatter.format(newDate.getTime()));
            date.setText(dateFormatter.format(newDate.getTime()));
        }

        Log.v("example", "Timestamp = " + newDate.getTimeInMillis());

        timeDialog();
        dateDialog();
    }

    private void findViewsById() {
        title = (EditText) findViewById(R.id.titel);

        phoneNr = (EditText) findViewById(R.id.phoneNr);
        assert phoneNr != null;
        phoneNr.setInputType(InputType.TYPE_NULL);

        smsText = (EditText) findViewById(R.id.smsText);

        time = (EditText) findViewById(R.id.time);
        assert time != null;
        time.setInputType(InputType.TYPE_NULL);

        date = (EditText) findViewById(R.id.date);
        assert date != null;
        date.setInputType(InputType.TYPE_NULL);
    }

    private void setOnClickListener() {
        phoneNr.setOnClickListener(this);
        findViewById(R.id.contactButton).setOnClickListener(this);
        time.setOnClickListener(this);
        findViewById(R.id.timePicker).setOnClickListener(this);
        date.setOnClickListener(this);
        findViewById(R.id.datePicker).setOnClickListener(this);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case (PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor query = getContentResolver().query(contactData, null, null, null, null);
                    assert query != null;
                    if (query.moveToFirst()) {
                        String id = query.getString(query.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                        String hasPhone = query.getString(query.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                        if (hasPhone.equalsIgnoreCase("1")) {
                            Cursor phoneQuery = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
                            assert phoneQuery != null;
                            phoneQuery.moveToFirst();
                            String phoneNrString = phoneQuery.getString(phoneQuery.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            Toast.makeText(getApplicationContext(), phoneNrString, Toast.LENGTH_SHORT).show();
                            phoneNr.setText(phoneNrString);
                            phoneQuery.close();
                        }
                    }
                    query.close();
                }
        }
    }

    private void timeDialog() {
        Calendar calendar = newDate;
        timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                newDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                newDate.set(Calendar.MINUTE, minute);
                time.setText(timeFormatter.format(newDate.getTime()));
                Log.v("example", "Timestamp = " + newDate.getTimeInMillis());
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
    }


    private void dateDialog() {
        Calendar calendar = newDate;
        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                newDate.set(year, monthOfYear, dayOfMonth);
                date.setText(dateFormatter.format(newDate.getTime()));
                Log.v("example", "Timestamp = " + newDate.getTimeInMillis());
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_sms, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.save:
                try {
                    saveSms();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.delete:
                deleteSms("");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveSms() throws ParseException {
        mainActivity.addSms(title.toString(), phoneNr.toString(), smsText.toString(), newDate.getTimeInMillis());
        mainActivity.createList();
    }

    private void deleteSms(String UUID) {
        mainActivity.cancelSms(UUID);
    }

/*
    public void onClickContactButton(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }
*/

/*
    public void onClickTimeButton(View view) {
        timePickerDialog.show();
    }
*/

/*
    public void onClickDateButton(View view) {
        datePickerDialog.show();
    }
*/

    @Override
    public void onClick(View v) {
        if (v == phoneNr || v == findViewById(R.id.contactButton)) {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, PICK_CONTACT);
        } else if (v == time || v == findViewById(R.id.timePicker)) {
            timePickerDialog.show();
        } else if (v == date || v == findViewById(R.id.datePicker)) {
            datePickerDialog.show();
        }
    }
}