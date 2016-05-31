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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditSms extends AppCompatActivity {

    private static final int PICK_CONTACT = 1;

    // UI References
    private EditText phoneNr;
    private EditText time;
    private EditText date;

    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private SimpleDateFormat dateFormatter;
    private SimpleDateFormat timeFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_sms);

        timeFormatter = new SimpleDateFormat("HH:mm", Locale.GERMAN);
        dateFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

        findViewsById();
        timeDialog();
        dateDialog();
    }

    private void findViewsById() {
        phoneNr = (EditText) findViewById(R.id.phoneNr);
        assert phoneNr != null;
        phoneNr.setInputType(InputType.TYPE_NULL);

        time = (EditText) findViewById(R.id.time);
        assert time != null;
        time.setInputType(InputType.TYPE_NULL);

        date = (EditText) findViewById(R.id.date);
        assert date != null;
        date.setInputType(InputType.TYPE_NULL);
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
        Calendar calendar = Calendar.getInstance();
        timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Calendar newTime = Calendar.getInstance();
                newTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                newTime.set(Calendar.MINUTE, minute);
                time.setText(timeFormatter.format(newTime.getTime()));
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
    }

    private void dateDialog() {
        Calendar calendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                date.setText(dateFormatter.format(newDate.getTime()));
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
                // TODO: saveSMS method
                return true;
            case R.id.delete:
                // TODO: deleteSMS method
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onClickContactButton(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    public void onClickTimeButton(View view) {
        timePickerDialog.show();
    }

    public void onClickDateButton(View view) {
        datePickerDialog.show();
    }
}