package com.flumine.typeone;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Iterator;

import static android.content.Context.MODE_PRIVATE;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    protected static final String BASE_URL = "http://192.168.0.191:8000";

    JSONObject resp;

    @Test
    public void testLogin() throws Exception {
        JSONObject object = new JSONObject();
        object.put("username", "admin");
        object.put("password", "admin");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, BASE_URL.concat("/api/token/"), object,
                response -> {setResp(response);},
                error -> {Log.e("REST", error.toString());}
        );
        Volley.newRequestQueue(getApplicationContext()).add(jsonObjectRequest);
        while (resp == null) {
            try {
                Thread.currentThread().wait(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Iterator<String> it = resp.keys();
        while (it.hasNext()) {
            String key = it.next();
            Log.d("TEST", key + ":" + resp.getString(key));
        }
        assertNotNull(resp.getString("access"));
        assertNotNull(resp.getString("refresh"));
    }

    private void setResp(JSONObject response) {
        resp = response;
    }

}