package com.flumine.typeone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NewRecordActivity extends BaseActivity {

    DateFormat LONG_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    DateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("MM dd HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_record);
        ((TextView)findViewById(R.id.date_string)).setText(SHORT_DATE_FORMAT.format(new Date()));
    }

    public void storeRecord(View view) throws Exception {

        JSONObject record = new JSONObject();

        String date = ((TextView)(findViewById(R.id.date_string))).getEditableText().toString();

        record.put("type", "0");
        //todo
        record.put("time", LONG_DATE_FORMAT.format(new Date()));
        record.put("insulin_amount", ((TextView)findViewById(R.id.shot)).getText());
        record.put("glucose_level", ((TextView)findViewById(R.id.sugar)).getText());
        record.put("bread_units", ((TextView)findViewById(R.id.bread_string)).getText());
        record.put("notes", ((TextView)findViewById(R.id.notes)).getText());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, BASE_URL.concat("/api/records/"),
                record,
                response -> {
                    try {
                        Log.e("REST", "Add record response is " + response.toString());
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

    public void back(View view) {
        Intent intent = new Intent(this, RecordsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        startActivity(intent);
    }

}