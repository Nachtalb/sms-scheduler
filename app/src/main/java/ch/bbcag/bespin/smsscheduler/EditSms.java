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
import java.util.Locale;
import java.util.Objects;

public class EditSms extends AppCompatActivity implements View.OnClickListener {

    private static final int PICK_CONTACT = 1;

    private EditText titleEditText;
    private EditText phoneNrEditText;
    private EditText smsTextEditText;
    private EditText timeEditText;
    private EditText dateEditText;

    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private SimpleDateFormat dateFormatter;
    private SimpleDateFormat timeFormatter;

    private Calendar newDate;

    private Boolean Update = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_sms);

        findViewsById();

        Intent i = getIntent();
        if (i.getStringExtra("UUID") != null) {
            Update = true;
            fillFields(i);
        }

        setOnClickListener();

        newDate = Calendar.getInstance();
        timeFormatter = new SimpleDateFormat("HH:mm", Locale.GERMAN);
        dateFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

        if (i.getLongExtra("timestamp", 0) != 0) {
            newDate.setTimeInMillis(i.getLongExtra("timestamp", Calendar.getInstance().getTimeInMillis()));
        } else {
            newDate.setTimeInMillis(System.currentTimeMillis());
        }

        timeEditText.setText(timeFormatter.format(newDate.getTime()));
        dateEditText.setText(dateFormatter.format(newDate.getTime()));

        initTimeDialog();
        initDateDialog();

        setOnClickListener();
    }

    private void fillFields(Intent i) {
        titleEditText.setText(i.getStringExtra("title"));
        phoneNrEditText.setText(i.getStringExtra("phoneNr"));
        smsTextEditText.setText(i.getStringExtra("smsText"));
    }

    private void setOnClickListener() {
        phoneNrEditText.setOnClickListener(this);
        findViewById(R.id.contactButton).setOnClickListener(this);
        timeEditText.setOnClickListener(this);
        findViewById(R.id.timePicker).setOnClickListener(this);
        dateEditText.setOnClickListener(this);
        findViewById(R.id.datePicker).setOnClickListener(this);
    }

    private void findViewsById() {
        titleEditText = (EditText) findViewById(R.id.titel);

        phoneNrEditText = (EditText) findViewById(R.id.phoneNr);
        assert phoneNrEditText != null;
        phoneNrEditText.setInputType(InputType.TYPE_NULL);

        smsTextEditText = (EditText) findViewById(R.id.smsText);

        timeEditText = (EditText) findViewById(R.id.time);
        assert timeEditText != null;
        timeEditText.setInputType(InputType.TYPE_NULL);

        dateEditText = (EditText) findViewById(R.id.date);
        assert dateEditText != null;
        dateEditText.setInputType(InputType.TYPE_NULL);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (reqCode == PICK_CONTACT && resultCode == Activity.RESULT_OK) {
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
                    phoneNrEditText.setText(phoneNrString);
                    phoneQuery.close();
                }
            }
            query.close();
        }
    }

    private void initTimeDialog() {
        Calendar calendar = newDate;
        timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                newDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                newDate.set(Calendar.MINUTE, minute);
                timeEditText.setText(timeFormatter.format(newDate.getTime()));
                Log.v("example", "Timestamp = " + newDate.getTimeInMillis());
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
    }

    private void initDateDialog() {
        Calendar calendar = newDate;
        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                newDate.set(year, monthOfYear, dayOfMonth);
                dateEditText.setText(dateFormatter.format(newDate.getTime()));
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
                    if (Update)
                        updateSms();
                    else
                        saveSms();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.delete:
                deleteSms();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveSms() throws ParseException {
        String title = titleEditText.getText().toString();
        String phoneNr = phoneNrEditText.getText().toString();
        String smsText = smsTextEditText.getText().toString();
        long timestamp = newDate.getTimeInMillis();

        if (Objects.equals(title, "") || Objects.equals(phoneNr, "") || Objects.equals(smsText, "")) {
            Toast.makeText(this, "You have to fill all fields.", Toast.LENGTH_SHORT).show();
        } else {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("title", title);
            returnIntent.putExtra("phoneNr", phoneNr);
            returnIntent.putExtra("smsText", smsText);
            returnIntent.putExtra("timestamp", timestamp);

            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
    }

    private void updateSms() throws ParseException {
        String title = titleEditText.getText().toString();
        String phoneNr = phoneNrEditText.getText().toString();
        String smsText = smsTextEditText.getText().toString();
        long timestamp = newDate.getTimeInMillis();

        if (Objects.equals(title, "") || Objects.equals(phoneNr, "") || Objects.equals(smsText, "")) {
            Toast.makeText(this, "You have to fill all fields.", Toast.LENGTH_SHORT).show();
        } else {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("title", title);
            returnIntent.putExtra("phoneNr", phoneNr);
            returnIntent.putExtra("smsText", smsText);
            returnIntent.putExtra("timestamp", timestamp);
            returnIntent.putExtra("UUID", getIntent().getStringExtra("UUID"));

            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
    }

    private void deleteSms() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("UUID", getIntent().getStringExtra("UUID"));
        returnIntent.putExtra("delete", "delete");

        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v == phoneNrEditText || v == findViewById(R.id.contactButton)) {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, PICK_CONTACT);
        } else if (v == timeEditText || v == findViewById(R.id.timePicker)) {
            timePickerDialog.show();
        } else if (v == dateEditText || v == findViewById(R.id.datePicker)) {
            datePickerDialog.show();
        }
    }
}