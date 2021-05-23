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
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PhotoActivity extends BaseActivity {

    int recordId;
    int photoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        recordId = getIntent().getIntExtra("RECORD_ID", 0);
        photoId = getIntent().getIntExtra("PHOTO_ID", 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        getPhoto();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPhoto();
    }

    private void getPhoto() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, BASE_URL.concat("/api/record/" + recordId +"/photo/" + photoId), null,
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
        Volley.newRequestQueue(PhotoActivity.this).add(jsonObjectRequest);
    }

    private void update(JSONObject photo) throws Exception {
        String rawImage = photo.getString("data");
        byte[] image = Base64.decode(rawImage, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
        ImageView imageView = (ImageView) findViewById(R.id.photo);
        imageView.setImageBitmap(bitmap);
    }

    private void deletePhoto() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.DELETE,
                BASE_URL.concat(String.format("/api/record/%d/photo/%d/", recordId, photoId)),
                null,
                response -> {
                    Log.d("REST", "Photo deleted");
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
        Volley.newRequestQueue(PhotoActivity.this).add(jsonObjectRequest);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getPhoto();
    }

    public void back(View view) {
        Intent intent = new Intent(this, RecordActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("RECORD_ID", recordId);
        startActivity(intent);
    }

    public void deletePhoto(View view) {
        deletePhoto();
        back(view);
    }

}