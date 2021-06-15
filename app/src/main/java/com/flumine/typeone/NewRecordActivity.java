package com.flumine.typeone;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NewRecordActivity extends BaseRecordActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_record);
        date = new Date();
        ((EditText)findViewById(R.id.date_string)).setText(DATE_FORMAT.format(date));
        ((EditText)findViewById(R.id.time_string)).setText(TIME_FORMAT.format(date));
        ((EditText)findViewById(R.id.insulin_name)).setEnabled(false);
        ((EditText)findViewById(R.id.insulin_name)).setEnabled(false);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        date = new Date();
    }

    public void storeRecord(View view) throws Exception {

        JSONObject record = new JSONObject();

        record.put("type", "0");
        record.put("time", DRF_DATE_FORMAT.format(date));
        record.put("insulin_amount", getNumber(R.id.shot));
        record.put("glucose_level", getNumber(R.id.sugar));
        record.put("bread_units", getNumber(R.id.bread_string));
        record.put("notes", getStr(R.id.notes));

        Log.d("REST", "Storing " + record.toString());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, BASE_URL.concat("/api/records/"),
                record,
                response -> {
                    try {
                        Log.e("REST", "Update record response is " + response.toString());
                        back(view);
                    } catch (Exception e) {
                        Log.e("REST", e.getMessage());
                    }
                },
                errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                String accessToken = getApplicationContext()
                        .getSharedPreferences("JWT", MODE_PRIVATE)
                        .getString("access", null);
                params.put("Authorization", "Bearer " + accessToken);
                return params;
            }
        };

        Volley.newRequestQueue(NewRecordActivity.this).add(jsonObjectRequest);

    }

}