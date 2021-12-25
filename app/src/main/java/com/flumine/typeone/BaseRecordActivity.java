package com.flumine.typeone;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

public class BaseRecordActivity extends BaseActivity {

    DatePickerDialog datePickerDialog;

    Date date;

    TimePickerDialog timePickerDialog;

    public void setTime(View view) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                Log.v("REST", "calendar.getTime() before =  " + calendar.getTime());
                Log.v("REST", "new hour value " + hourOfDay);
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                Log.v("REST", "calendar.getTime() after =  " + calendar.getTime());
                Log.v("REST", "TIME_FORMAT.format(calendar.getTime()) =  "
                        + TIME_FORMAT.format(calendar.getTime()));
                Log.v("REST", "TIME_FORMAT.format(calendar.getTime()) =  "
                        + TIME_FORMAT.format(calendar.getTime()));
                Log.v("REST", "TIME_FORMAT.format(calendar.getTime()) =  "
                        + TIME_FORMAT.format(calendar.getTime()));
                calendar.set(Calendar.MINUTE, minute);
                ((EditText)findViewById(R.id.time_string)).setText(TIME_FORMAT.format(calendar.getTime()));
                date = calendar.getTime();
            }
        }, hour, minute, true);
        timePickerDialog.show();
    }

    public void setDate(View view) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                ((EditText)findViewById(R.id.date_string)).setText(DATE_FORMAT.format(calendar.getTime()));
                date = calendar.getTime();
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    public void back(View view) {
        Intent intent = new Intent(this, RecordsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

}
