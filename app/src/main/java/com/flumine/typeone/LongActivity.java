package com.flumine.typeone;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LongActivity extends BaseRecordActivity {

    int recordId;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(getClass().getName(), "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.record_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == (R.id.delete_listed_record)) {
            deleteRecord();
            back(null);
        }
        return true;
    }

    private void deleteRecord() {
        findViewById(R.id.timer).setVisibility(View.VISIBLE);
        findViewById(R.id.layout).setVisibility(View.GONE);
        Log.d("REST", "Deleting record with id " + recordId);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.DELETE, BASE_URL.concat("/api/record/" + recordId +"/"),
                null,
                response -> {
                    back(null);
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
        Volley.newRequestQueue(LongActivity.this).add(jsonObjectRequest);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_long);
        date = new Date();
        ((EditText)findViewById(R.id.date_string)).setText(DATE_FORMAT.format(date));
        ((EditText)findViewById(R.id.time_string)).setText(TIME_FORMAT.format(date));
        ((EditText)findViewById(R.id.insulin_name)).setEnabled(false);
        recordId = getIntent().getIntExtra("RECORD_ID", 0);
        getRecord();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        recordId = intent.getIntExtra("RECORD_ID", -1);
        Log.d("REST", "New intent record id is " + intent.getIntExtra("RECORD_ID", -1));
        getRecord();
    }

    private void getRecord() {
        Log.d("REST", "Activity intent record id is " + recordId);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, BASE_URL.concat("/api/record/" + recordId +"/"),
                null,
                response -> {
                    try {
                        update(response);
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
        Volley.newRequestQueue(LongActivity.this).add(jsonObjectRequest);
    }

    private void update(JSONObject response) throws Exception {

        date = DRF_DATE_FORMAT.parse(response.getString("time"));

        ((TextView)findViewById(R.id.date_string)).setText(DATE_FORMAT.format(date));
        ((TextView)findViewById(R.id.time_string)).setText(TIME_FORMAT.format(date));
        ((TextView)findViewById(R.id.shot)).setText(response.getString("insulin_amount"));
        ((TextView)findViewById(R.id.insulin_name)).setText(
                getStringResource(response.getJSONObject("insulin").getString("name")));
        ((TextView)findViewById(R.id.insulin_name)).setEnabled(false);
        ((TextView)findViewById(R.id.notes)).setText(response.getString("notes"));

        LinearLayout photosLayout = (LinearLayout)findViewById(R.id.photos);

        photosLayout.removeAllViews();

        JSONArray photos = response.getJSONArray("photos");
        for (int i=0; i < photos.length(); i++) {
            JSONObject photo = (JSONObject)photos.get(i);
            String rawImage = photo.getString("data");
            byte[] image = Base64.decode(rawImage, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
            ImageView imageView = (ImageView) getLayoutInflater().inflate(R.layout.image, null);
            imageView.setImageBitmap(bitmap);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(1024, 768));
            imageView.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(LongActivity.this, PhotoActivity.class);
                    intent.putExtra("PHOTO_ID", photo.getInt("id"));
                    intent.putExtra("RECORD_ID", recordId);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("TEST", "Fail to get photo id", e);
                }
            });
            photosLayout.addView(imageView);
        }
        Log.d("REST", "Record retrieved");
        findViewById(R.id.timer).setVisibility(View.GONE);
        findViewById(R.id.layout).setVisibility(View.VISIBLE);
    }

    public void storeRecord(View view) throws Exception {

        JSONObject record = new JSONObject();

        record.put("id", recordId);
        record.put("type", "1");
        record.put("time", DRF_DATE_FORMAT.format(date));
        record.put("insulin_amount", getNumber(R.id.shot));
        record.put("notes", getStr(R.id.notes));

        Log.d("REST", "Storing " + record.toString());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.PUT, BASE_URL.concat("/api/record/" + recordId +"/"),
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

        Volley.newRequestQueue(LongActivity.this).add(jsonObjectRequest);

    }

}
