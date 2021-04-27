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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordsActivity extends BaseActivity {

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
        Log.d("REST", "Getting records");
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest (
                Request.Method.GET, BASE_URL.concat("/api/records/"), null,
                response -> {
                    try {
                        Log.d("REST", "Records retrieved");
                        JSONArray jarray = new JSONArray(response.toString());
                        List<JSONObject> result = new ArrayList<>();
                        for (int i=0; i<jarray.length(); i ++) {
                            result.add(jarray.getJSONObject(i));
                        }
                        fillRecords(result);
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
                params.put("Authorization", "Bearer "+ accessToken);
                return params;
            }
        };
        Volley.newRequestQueue(RecordsActivity.this).add(jsonObjectRequest);
    }

    private void fillRecords(List<JSONObject> list) {
        ListView records = findViewById(R.id.records);
        ArrayAdapter<JSONObject> arrayAdapter =
                new ArrayAdapter<JSONObject>(this, R.layout.item, list);
        records.setAdapter(arrayAdapter);
    }

}