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

import java.util.HashMap;
import java.util.Map;

public class RecordActivity extends BaseActivity {

    int recordId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        getRecord();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getRecord();
    }

    private void getRecord() {
        recordId = getIntent().getIntExtra("RECORD_ID", 0);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, BASE_URL.concat("/api/record/" + recordId +"/"), null,
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
        ((TextView)findViewById(R.id.date_string))
                .setText(response.getString("time"));
        ((TextView)findViewById(R.id.shot))
                .setText(response.getString("insulin_amount"));
        ((TextView)findViewById(R.id.sugar))
                .setText(response.getString("glucose_level"));
        ((TextView)findViewById(R.id.bread_string))
                .setText(response.getString("bread_units"));

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
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent(RecordActivity.this, PhotoActivity.class);
                        intent.putExtra("PHOTO_ID", photo.getInt("id"));
                        intent.putExtra("RECORD_ID", recordId);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e("TEST", "Fail to get photo id", e);
                    }
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

}
