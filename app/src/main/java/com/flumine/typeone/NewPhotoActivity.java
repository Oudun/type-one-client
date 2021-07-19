package com.flumine.typeone;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class NewPhotoActivity extends BaseActivity {

    private static final int SELECT_PICTURE = 1;

    private byte[] bytes;

    int recordId;

    ImageView img;

    public void onCreate(Bundle savedInstanceState) {
        Log.d("REST", "NewPhotoActivity.onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photo);
        img = findViewById(R.id.photo);
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), SELECT_PICTURE);
        recordId = getIntent().getIntExtra("RECORD_ID", 0);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("REST", "NewPhotoActivity.onNewIntent");
        super.onNewIntent(intent);
        recordId = intent.getIntExtra("RECORD_ID", -1);
        Log.d("REST", "New intent record id is " + intent.getIntExtra("RECORD_ID", -1));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("REST", "NewPhotoActivity.onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImage = data.getData();
                img.setImageURI(selectedImage);
                try (InputStream is = getApplicationContext().getContentResolver().openInputStream(selectedImage);
                     ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();) {
                    Bitmap originalBitmap = BitmapFactory.decodeStream(is);
                    Log.v("REST", "Original image size is w:"
                            + originalBitmap.getWidth() + " x h:" + originalBitmap.getHeight());
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 360,
                            ((originalBitmap.getHeight() * 360) / originalBitmap.getWidth()),
                            true);
                    Log.v("REST", "Scaled image size is w:"
                            + scaledBitmap.getWidth() + " x h:" + scaledBitmap.getHeight());
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream);
                    Log.v("REST", "Compressed to stream " + byteArrayOutputStream);
                    bytes = byteArrayOutputStream.toByteArray();
                    Log.v("REST", "Compressed file size is " +
                            (bytes == null ? "null" : bytes.length));
                } catch (Exception e) {
                    Log.e("REST", "Failed to read " + selectedImage, e);
                }
            }
        }
    }

    public void savePhoto(View view) {

        Log.d("REST", "NewPhotoActivity.savePhoto");

        findViewById(R.id.timer).setVisibility(View.VISIBLE);
        findViewById(R.id.photo).setVisibility(View.GONE);

        byte[] encoded = android.util.Base64.encode(bytes, android.util.Base64.DEFAULT);
        Log.d("REST", "Encoded image is " + new String(encoded));
        JSONObject object = new JSONObject();

        try {
            Log.v("REST", "Start building image string");
            object.put("data", new String(encoded));
            Log.v("REST", "End building image string");
        } catch (Exception e) {
            Log.e("REST", e.getMessage());
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, BASE_URL.concat("/api/record/" + recordId +"/photos/"), object,
                response -> {
                    Log.d("REST", response.toString());
                    Intent intent = new Intent(this, RecordActivity.class);
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
        Volley.newRequestQueue(NewPhotoActivity.this).add(jsonObjectRequest);
    }

    public void back(View view) {
        Intent intent = new Intent(this, RecordActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("RECORD_ID", recordId);
        startActivity(intent);
    }

}