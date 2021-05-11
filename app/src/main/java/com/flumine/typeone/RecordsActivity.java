package com.flumine.typeone;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import android.util.Base64;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RecordsActivity extends BaseActivity {

    ListView recordsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);
        recordsView = findViewById(R.id.records);
        recordsView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, RecordActivity.class);
            intent.putExtra("RECORD_ID", (int)id);
            Log.d("INTENT", String.valueOf(id));
            Log.d("INTENT", String.valueOf(intent.getIntExtra("RECORD_ID", 0)));
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
        getRecords();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getRecords();
    }

    private void getRecords() {
        Log.d("REST", "Getting records");
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(
                Request.Method.GET, BASE_URL.concat("/api/records/"), null,
                response -> {
                    try {
                        Log.d("REST", "Records retrieved");
                        JSONArray jarray = new JSONArray(response.toString());
                        recordsView.setAdapter(
                                new JSONAdapter(RecordsActivity.this, jarray));
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
        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        Volley.newRequestQueue(RecordsActivity.this).add(jsonObjectRequest);
    }

    public void refresh(View view) {
        getRecords();
    }

    class JSONAdapter extends BaseAdapter implements ListAdapter {

        JSONArray array;
        Activity activity;

        public JSONAdapter(Activity activity, JSONArray array) {
            this.array = array;
            this.activity = activity;
        }

        @Override
        public int getCount() {
            return array.length();
        }

        @Override
        public Object getItem(int position) {
            return array.optJSONObject(position);
        }

        @Override
        public long getItemId(int position) {
            JSONObject jsonObject = (JSONObject)getItem(position);
            return jsonObject.optLong("id");
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = activity.getLayoutInflater().inflate(R.layout.item, null);
            JSONObject record = (JSONObject) getItem(position);
            try {
                GridLayout grid = (GridLayout) convertView;
                String dateTime = record.getString("time");
                Object[] dateParts = new MessageFormat("{0} {1} {2}").parse(dateTime);
                ((TextView)grid.findViewById(R.id.date_string)).setText(dateParts[0]+" "+dateParts[1]);
                ((TextView)grid.findViewById(R.id.time_string)).setText((String)dateParts[2]);
                ((TextView)grid.findViewById(R.id.bread_string))
                        .setText(record.getString("bread_units"));
                ((TextView)grid.findViewById(R.id.shot_string)).setText("TEST");
                JSONObject insulin = record.getJSONObject("insulin");
                int inslulinNameId = getResources().getIdentifier(insulin.getString("name"),
                        "string", "com.flumine.typeone");
                ((TextView)grid.findViewById(R.id.insulin_name))
                        .setText(getResources().getString(inslulinNameId));
                ((TextView)grid.findViewById(R.id.shot_string))
                        .setText(record.getString("insulin_amount"));
                ((TextView)grid.findViewById(R.id.gluc_string))
                        .setText(record.getString("glucose_level"));
                JSONArray photos = record.getJSONArray("photos");
                if (photos.length()>0) {
                    String rawImage = ((JSONObject)photos.get(0)).getString("thumb");
                    byte[] image = Base64.decode(rawImage, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                    ((ImageView)grid.findViewById(R.id.meal_img))
                            .setImageBitmap(bitmap);
                }
            } catch (Exception e) {
                Log.e("REST", e.getLocalizedMessage());
            }
            return convertView;
        }

    }

}

