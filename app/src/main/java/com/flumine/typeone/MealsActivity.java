package com.flumine.typeone;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MealsActivity extends BaseActivity {

    int recordId;

    ListView mealsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recordId = getIntent().getIntExtra("RECORD_ID", 0);
        setContentView(R.layout.activity_meals);
        getMeals();
    }

    private void getMeals() {

        findViewById(R.id.timer).setVisibility(View.VISIBLE);
        findViewById(R.id.records).setVisibility(View.GONE);

        Log.d("REST", "Getting meals");
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(
                Request.Method.GET, BASE_URL.concat("/api/record/" + recordId + "/meals/"), null,
                response -> {
                    try {
                        Log.d("REST", "Meals retrieved");
                        JSONArray jarray = new JSONArray(response.toString());
                        mealsView.setAdapter(
                                new MealsActivity.JSONAdapter(MealsActivity.this, jarray));
                    } catch (Exception e) {
                        Log.e("REST", e.getMessage());
                    } finally {
                        findViewById(R.id.timer).setVisibility(View.GONE);
                        findViewById(R.id.records).setVisibility(View.VISIBLE);
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
        int socketTimeout = 300000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        Volley.newRequestQueue(MealsActivity.this).add(jsonObjectRequest);

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
            return null;
        }

    }

}