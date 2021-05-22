package com.flumine.typeone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CameraActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, Camera2BasicFragment.newInstance())
                    .commit();
        }
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

        int recordId = getIntent().getIntExtra("RECORD_ID", 0);
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
        Volley.newRequestQueue(CameraActivity.this).add(jsonObjectRequest);

    }

}