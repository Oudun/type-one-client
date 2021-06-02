package com.flumine.typeone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.ClientError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public abstract class BaseActivity extends AppCompatActivity {

    //"2021-05-23T13:07:33.365727-05:00"
    protected static DateFormat DRF_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ");
    protected static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM");
    protected static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

    //protected static final String BASE_URL = "http://192.168.0.191:8000";
    protected static final String BASE_URL = "https://type-one.herokuapp.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("REST", "Base Activity onCreate");
        super.onCreate(savedInstanceState);
    }

    protected Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            if (error instanceof AuthFailureError) {
                Log.d("REST", "Authentication error");
                try {
                    refreshToken();
                } catch (Exception e) {
                    Log.e("REST", e.getLocalizedMessage());
                }
            } else if (error instanceof TimeoutError) {
                Log.d("REST", "Timeout");
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Fail to connect to the server",
                        Toast.LENGTH_LONG);
                toast.show();
            } else if (error instanceof ClientError) {
                Log.d("REST", "Client error");
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Error message is " + error.getMessage(),
                        Toast.LENGTH_LONG);
                toast.show();
            }
        }
    };

    private void refreshToken() throws Exception {
        Log.e("REST", "Base Activity refresh token");
        SharedPreferences pref =
                getApplicationContext().getSharedPreferences("JWT", MODE_PRIVATE);
        JSONObject object = new JSONObject();
        String refreshToken = pref.getString("refresh", null);
        if (refreshToken == null) {
            getNewTokens();
        }
        object.put("refresh", pref.getString("refresh", null));
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, BASE_URL.concat("/api/token/refresh/"), object,
                response -> {
                    try {
                        String newAccessToken = response.getString("access");
                        Log.d("REST", "New access token retrieved " + newAccessToken);
                        pref.edit().putString("access", newAccessToken).apply();
                    } catch (JSONException e) {
                        Log.e("REST", "Error " + e.getMessage());
                    }
                },
                error -> {
                    if (error instanceof AuthFailureError) {
                        Log.e("REST", "Failed to refresh token " + error.getMessage());
                        getNewTokens();
                    }
                }
        );
        Volley.newRequestQueue(getApplicationContext()).add(jsonObjectRequest);
    }

    private void getNewTokens() {
        Log.e("REST", "Base Activity getNewTokens");
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    protected String getNumber(int id) {
        String result = ((TextView)findViewById(id)).getText().toString();
        return result.isEmpty() ? "0" : result;
    }

    protected String getStr(int id) {
        return ((TextView)findViewById(id)).getText().toString();
    }

}