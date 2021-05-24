package com.flumine.typeone;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RecordActivity extends BaseActivity {

    int recordId;
    long time;

    DateFormat LONG_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    DateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("MM dd HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        recordId = getIntent().getIntExtra("RECORD_ID", 0);
        getRecord();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        Volley.newRequestQueue(RecordActivity.this).add(jsonObjectRequest);
    }

    private void update(JSONObject response) throws Exception {

        //((TextView)findViewById(R.id.date_string)).setText(response.getString("time"));
        ((TextView)findViewById(R.id.shot)).setText(response.getString("insulin_amount"));
        ((TextView)findViewById(R.id.sugar)).setText(response.getString("glucose_level"));
        ((TextView)findViewById(R.id.bread_string)).setText(response.getString("bread_units"));

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
                    Intent intent = new Intent(RecordActivity.this, PhotoActivity.class);
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        recordId = intent.getIntExtra("RECORD_ID", -1);
        Log.d("REST", "New intent record id is " + intent.getIntExtra("RECORD_ID", -1));
        getRecord();
    }

    public void updateRecord(View view) {
        getRecord();
    }

    public void addPhoto(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("RECORD_ID", recordId);
        startActivity(intent);
    }

    public void back(View view) {
        Intent intent = new Intent(this, RecordsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    public void storeRecord(View view) throws Exception {

        JSONObject record = new JSONObject();

        String date = ((TextView)(findViewById(R.id.date_string))).getEditableText().toString();

        record.put("id", recordId);
        record.put("type", "0");
        //todo
        record.put("time", LONG_DATE_FORMAT.format(new Date()));
        record.put("insulin_amount", ((TextView)findViewById(R.id.shot)).getText());
        record.put("glucose_level", ((TextView)findViewById(R.id.sugar)).getText());
        record.put("bread_units", ((TextView)findViewById(R.id.bread_string)).getText());
        record.put("notes", "aaaa");

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

        Volley.newRequestQueue(RecordActivity.this).add(jsonObjectRequest);

    }

}
