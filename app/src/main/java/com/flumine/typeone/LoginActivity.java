package com.flumine.typeone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class LoginActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("REST", "login form created");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("REST", "redirected to login");
    }


    public void login(View view) throws Exception {

        JSONObject object = new JSONObject();
        object.put("username", findViewById(R.id.username));
        object.put("password", findViewById(R.id.password));
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, BASE_URL.concat("/api/token/"), object,
                response -> {
                    try {
                        SharedPreferences pref =
                                getApplicationContext().getSharedPreferences("JWT", MODE_PRIVATE);
                        pref.edit().putString("access", response.getString("access")).apply();
                        pref.edit().putString("refresh", response.getString("refresh")).apply();
                        showRecords();
                    } catch (Exception e) {
                        Log.e("REST", e.getMessage());
                    }
                },
                error -> ((TextView)findViewById(R.id.text))
                        .setText(String.format("Error getting response: %s", error.toString()))
        );
        Volley.newRequestQueue(getApplicationContext()).add(jsonObjectRequest);

    }

    private void showRecords() {
        Intent intent = new Intent(this, RecordsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

}