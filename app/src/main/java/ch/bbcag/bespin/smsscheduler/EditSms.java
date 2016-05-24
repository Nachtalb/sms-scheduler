package ch.bbcag.bespin.smsscheduler;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditSms extends AppCompatActivity implements View.OnClickListener {

    // UI Reference
    private EditText date;

    private DatePickerDialog datePickerDialog;
    private SimpleDateFormat dateFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_sms);

        dateFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);
        findViewsById();
        setDateTimeField();

    }

    private void findViewsById() {
        date = (EditText) findViewById(R.id.date);
        assert date != null;
        date.setInputType(InputType.TYPE_NULL);
        date.requestFocus();
    }

    private void setDateTimeField() {
        date.setOnClickListener(this);

        Calendar newCalendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                date.setText(dateFormatter.format(newDate.getTime()));
            }
        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
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

    public void onClickTimePicker(View view) {

    }

    public void onClickDatePicker(View view) {
        datePickerDialog.show();
    }

    @Override
    public void onClick(View v) {
        datePickerDialog.show();
    }
}