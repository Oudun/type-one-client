package com.flumine.typeone;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getRecords();
    }

    private void getRecords() {
        List<JSONObject> result = new ArrayList<>();
        String url = "http://192.168.0.191:8000/api/records/";
        JSONObject object = new JSONObject();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, object,
                response -> {
                    try {
                        JSONArray jarray = response.getJSONArray("records");
                        for (int i=0; i<jarray.length(); i ++) {
                            result.add(jarray.getJSONObject(i));
                        }
                    } catch (Exception e) {
                        Log.e("REST", e.getMessage());
                    }
                },
                error -> Log.e("REST", error.toString())
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                String accessToken = getApplicationContext()
                        .getSharedPreferences("JWT", MODE_PRIVATE)
                        .getString("access", null);
                params.put("Authorization", "Bearer "+ accessToken);
                return params;
            }
        };

        Volley.newRequestQueue(getApplicationContext()).add(jsonObjectRequest);
    }

    private void fillRecords(List<JSONObject> list) {
        ListView records = findViewById(R.id.records);
        ArrayAdapter<JSONObject> arrayAdapter =
                new ArrayAdapter<JSONObject>(this,android.R.layout.simple_list_item_1, list);
        records.setAdapter(arrayAdapter);
    }

}