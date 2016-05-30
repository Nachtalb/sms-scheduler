package ch.bbcag.bespin.smsscheduler;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditSms extends AppCompatActivity {

    // UI References
    private EditText date;
    private EditText time;

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
        setTimeField();
        setDateField();
    }

    private void findViewsById() {
        // get time field
        time = (EditText) findViewById(R.id.time);
        assert time != null;
        time.setInputType(InputType.TYPE_NULL);
        time.requestFocus();

        // get date field
        date = (EditText) findViewById(R.id.date);
        assert date != null;
        date.setInputType(InputType.TYPE_NULL);
        date.requestFocus();
    }

    private void setTimeField() {
        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerDialog.show();
            }
        });

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

    private void setDateField() {
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });

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
//                saveSms();
                return true;
            case R.id.delete:
//                deleteSms();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

//    public void saveSms() {
//
//    }
//
//    public void deleteSms() {
//
//    }

    public void onClickTimeButton(View view) {
        timePickerDialog.show();
    }

    public void onClickDateButton(View view) {
        datePickerDialog.show();
    }
}