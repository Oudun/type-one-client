package com.flumine.typeone;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

import android.util.Base64;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.flumine.typeone.BuildConfig;

public class RecordsActivity extends BaseActivity {

    ListView recordsView;

    private static final String LONG_SHOT_TYPE = "1";

    private static final String SHORT_SHOT_TYPE = "0";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(getClass().getName(), "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.records_menu, menu);
        menu.findItem(R.id.version).setIcon(R.drawable.ic_baseline_info_24);
        menu.findItem(R.id.logout).setIcon(R.drawable.ic_baseline_login_24);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == (R.id.logout)) {
            logout();
        }
        if (item.getItemId() == R.id.version) {
            AlertDialog.Builder builder = new AlertDialog.Builder(RecordsActivity.this);
            builder.setTitle(R.string.app_name)
                    .setCancelable(false)
                    .setMessage(BuildConfig.GitHash)
                    .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.setCanceledOnTouchOutside(true);
            alert.show();

        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);
        recordsView = findViewById(R.id.records);
        recordsView.setOnItemClickListener((parent, view, position, id) -> {
            try {
                String type = ((JSONObject)parent.getItemAtPosition(position)).getString("type");
                Intent intent;
                if (type.equals(LONG_SHOT_TYPE)) {
                    intent = new Intent(this, LongActivity.class);
                } else if (type.equals(SHORT_SHOT_TYPE)) {
                    intent = new Intent(this, RecordActivity.class);
                } else {
                    throw new Exception("Unknown record type");
                }
                intent.putExtra("RECORD_ID", (int)id);
                Log.d("INTENT", String.valueOf(id));
                Log.d("INTENT", String.valueOf(intent.getIntExtra("RECORD_ID", 0)));
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            } catch (Exception e) {
                Log.e("REST", "Fail to forward to record details", e);
            }
        });
        getRecords();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getRecords();
    }

    private void logout() {
        Log.d("REST", "Logging out");
        SharedPreferences pref =
                getApplicationContext().getSharedPreferences("JWT", MODE_PRIVATE);
        pref.edit().remove("access").apply();
        pref.edit().remove("refresh").apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    private void getRecords() {

        findViewById(R.id.timer).setVisibility(View.VISIBLE);
        findViewById(R.id.records).setVisibility(View.GONE);

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
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        Volley.newRequestQueue(RecordsActivity.this).add(jsonObjectRequest);
    }

    public void refresh(View view) {
        getRecords();
    }

    public void newRecord(View view) {
        Intent intent = new Intent(this, NewRecordActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        startActivity(intent);
    }

    public void newLong(View view) {
        Intent intent = new Intent(this, NewLongActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        startActivity(intent);
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
            JSONObject record = (JSONObject) getItem(position);
            Log.v("REST", record.toString());
            try {
                //if (convertView == null)
                convertView = activity.getLayoutInflater().inflate(R.layout.item, null);
                GridLayout grid = (GridLayout) convertView;
                Date date = DRF_DATE_FORMAT.parse(record.getString("time"));
                String type = record.getString("type");
                String id = record.getString("id");
                ((TextView)grid.findViewById(R.id.date_string)).setText(DATE_FORMAT.format(date));
                ((TextView)grid.findViewById(R.id.time_string)).setText(TIME_FORMAT.format(date));
                ((TextView)grid.findViewById(R.id.bread_string)).setText(String.format(Locale.getDefault(),
                        "%.1f", record.getDouble("bread_units")));
                ((TextView)grid.findViewById(R.id.shot_string)).setText(record.getString("insulin_amount"));
                ((TextView)grid.findViewById(R.id.gluc_string)).setText(record.getString("glucose_level"));
                ((TextView)grid.findViewById(R.id.notes)).setText(record.getString("notes"));
                if (LONG_SHOT_TYPE.equals(type)) {
                    ((TextView)grid.findViewById(R.id.bread_string)).setText("");
                    ((TextView)grid.findViewById(R.id.gluc_string)).setText("");
                    grid.setBackgroundColor(getResources().getColor(R.color.control_background));
                }
                JSONObject insulin = record.getJSONObject("insulin");
                ((TextView)grid.findViewById(R.id.insulin_name))
                        .setText(getStringResource(insulin.getString("name")));
                JSONArray photos = record.getJSONArray("photos");
                if (photos.length()>0) {
                    String rawImage = ((JSONObject)photos.get(0)).getString("thumb");
                    String picId = ((JSONObject)photos.get(0)).getString("id");
                    byte[] image = Base64.decode(rawImage, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                    ((ImageView)grid.findViewById(R.id.meal_img))
                            .setImageBitmap(bitmap);
                    Log.v("RESTPICT", String.format("Setting pic#%s for record #%s (time %s)",
                            picId, id, record.getString("time")));
                } else {
                    Log.v("RESTPICT", String.format("No pics for record #%s", id));
                }
            } catch (Exception e) {
                Log.e("REST", e.getLocalizedMessage());
            }
            return convertView;
        }

    }

}

