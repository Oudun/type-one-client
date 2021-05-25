package com.flumine.typeone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CameraActivity extends BaseActivity {

    int recordId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, Camera2BasicFragment.newInstance())
                    .commit();
        }
        recordId = getIntent().getIntExtra("RECORD_ID", 0);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        recordId = intent.getIntExtra("RECORD_ID", -1);
        Log.d("REST", "New intent record id is " + intent.getIntExtra("RECORD_ID", -1));
    }

    public void back() {
        Intent intent = new Intent(this, RecordActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        intent.putExtra("RECORD_ID", recordId);
        startActivity(intent);
    }

    public void storePicture(byte[] bytes) {

        byte[] encoded = android.util.Base64.encode(bytes, android.util.Base64.DEFAULT);
        Log.d("REST", "Encoded image is " + new String(encoded));
        JSONObject object = new JSONObject();

        try {
            object.put("data", new String(encoded));
        } catch (Exception e) {
            Log.e("REST", e.getMessage());
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
            Request.Method.POST, BASE_URL.concat("/api/record/" + recordId +"/photos/"), object,
            response -> {
                Log.d("REST", response.toString());
                Intent intent = new Intent(this, RecordsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra("RECORD_ID", recordId);
                startActivity(intent);
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

        int socketTimeout = 300000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);

        Volley.newRequestQueue(CameraActivity.this).add(jsonObjectRequest);

    }

}