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

/**
 * The type Edit sms.
 */
public class EditSms extends AppCompatActivity implements View.OnClickListener {

    /**
     * The request code supplied in the startActivityForResult for an contact request
     */
    private static final int PICK_CONTACT = 1;

    /**
     * EditText of the title
     */
    private EditText titleEditText;
    /**
     * EditText of the phonenumber
     */
    private EditText phoneNrEditText;
    /**
     * EditText oth the SMS text
     */
    private EditText smsTextEditText;
    /**
     * EditText of the time
     */
    private EditText timeEditText;
    /**
     * EditText of the date
     */
    private EditText dateEditText;

    /**
     * DatePickerDialog
     */
    private DatePickerDialog datePickerDialog;
    /**
     * TimePickerDialog
     */
    private TimePickerDialog timePickerDialog;
    /**
     * SimpleDateFormat for he date
     */
    private SimpleDateFormat dateFormatter;
    /**
     * SimpleDateFormat for the time
     */
    private SimpleDateFormat timeFormatter;

    /**
     * Calender in which the time and date are temporary saved
     */
    private Calendar newDate;

    /**
     * This will be set to true, if the momentaneous request of this Activity is, to update and entry
     */
    private Boolean Update = false;

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
        setContentView(R.layout.activity_edit_sms);
        findViewsById();
        Intent i = getIntent();
        if (i.getStringExtra("UUID") != null) {
            Update = true;

            fillFields(i);
        }

        newDate = Calendar.getInstance();
        timeFormatter = new SimpleDateFormat("HH:mm", Locale.GERMAN);
        dateFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

        if (i.getLongExtra("timestamp", 0) != 0)
            newDate.setTimeInMillis(i.getLongExtra("timestamp", Calendar.getInstance().getTimeInMillis()));
        else
            newDate.setTimeInMillis(System.currentTimeMillis());

        timeEditText.setText(timeFormatter.format(newDate.getTime()));
        dateEditText.setText(dateFormatter.format(newDate.getTime()));

        initTimeDialog();
        initDateDialog();

        setOnClickListener();
    }

    /**
     * Fills the EditText fields if with the given data
     *
     * @param i - The data will be saved as Extras in the used Intent.
     */
    private void fillFields(Intent i) {
        titleEditText.setText(i.getStringExtra("title"));
        phoneNrEditText.setText(i.getStringExtra("phoneNr"));
        smsTextEditText.setText(i.getStringExtra("smsText"));
    }

    /**
     * Adds the different OnClickListener
     */
    private void setOnClickListener() {
        phoneNrEditText.setOnClickListener(this);
        findViewById(R.id.contactButton).setOnClickListener(this);
        timeEditText.setOnClickListener(this);
        findViewById(R.id.timePicker).setOnClickListener(this);
        dateEditText.setOnClickListener(this);
        findViewById(R.id.datePicker).setOnClickListener(this);
    }

    /**
     * Sets the values of the different EditText variables
     */
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

    /**
     * Initialises the TimePicker Dialog
     */
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

    /**
     * Initialises the DatePicker Dialog
     */
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


    /**
     * Inflate the menu; this adds items to the action bar if it is present.
     *
     * @param menu - The menu which should be added
     * @return - True or False if everything worked
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_sms, menu);
        return true;
    }

    /**
     * Listener for the selection in the toolbar menu
     *
     * @param item - The clicked Item
     * @return - True or False if everything worked
     */
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

    /**
     * Handles the save action
     *
     * @throws ParseException
     */
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

    /**
     * Handles the update action
     *
     * @throws ParseException
     */
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

    /**
     * Handles the delete action
     */
    private void deleteSms() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("UUID", getIntent().getStringExtra("UUID"));
        returnIntent.putExtra("delete", "delete");

        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    /**
     * Sets the onclick function for the time and datepicker
     *
     * @param v - View in which the pickers will be showed
     */
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