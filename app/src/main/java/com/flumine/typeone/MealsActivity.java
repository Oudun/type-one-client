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
import java.util.Locale;
import java.util.Map;

public class MealsActivity extends BaseActivity {

    int recordId;

    ListView mealsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meals);
        recordId = getIntent().getIntExtra("RECORD_ID", 0);
        mealsView = findViewById(R.id.meals);
        mealsView.setOnItemClickListener((parent, view, position, id) -> {
            try {
                JSONObject meal = (JSONObject) mealsView.getAdapter().getItem(position);
                JSONObject ingredientUnit = meal.getJSONObject("ingredient_unit");
                JSONObject ingredient = ingredientUnit.getJSONObject("ingredient");
                int mealId = meal.getInt("id");
                int ingredientUnitId = ingredientUnit.getInt("id");
                double quantity = meal.getDouble("quantity");
                Intent intent = new Intent(this, MealActivity.class);
                intent.putExtra("RECORD_ID", (int)recordId);
                intent.putExtra("INGREDIENT_UNIT_ID", ingredientUnitId);
                intent.putExtra("INGREDIENT_ID", ingredient.getInt("id"));
                intent.putExtra("MEAL_ID", mealId);
                intent.putExtra("QUANTITY", quantity);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                Log.d("REST",
                        String.format("Sending intent %s is RECORD_ID:%d INGREDIENT_UNIT_ID:%d MEAL_ID:%d QUANTITY:%f",
                                intent.hashCode(), recordId, ingredientUnitId, mealId, quantity));
                startActivity(intent);
            } catch (Exception e) {
                Log.e("REST", "Fail to forward to record details", e);
            }
        });
        getMeals();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        recordId = intent.getIntExtra("RECORD_ID", -1);
        Log.d("REST", "New intent record id is " + intent.getIntExtra("RECORD_ID", -1));
        getMeals();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getMeals();
    }

    private void getMeals() {

        findViewById(R.id.timer).setVisibility(View.VISIBLE);
        findViewById(R.id.meals).setVisibility(View.GONE);

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
                        findViewById(R.id.meals).setVisibility(View.VISIBLE);
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

    public void back(View view) {
        Intent intent = new Intent(this, RecordActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("RECORD_ID", recordId);
        startActivity(intent);
    }

    public void addMeal(View view) {
        Intent intent = new Intent(this, IngredientsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("RECORD_ID", recordId);
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
            String title;
            try {
                JSONObject record = (JSONObject) getItem(position);
                JSONObject ingredientUnit = record.getJSONObject("ingredient_unit");
                JSONObject ingredient = ingredientUnit.getJSONObject("ingredient");
                String ingredientName = ingredient.getString("name");
                double breadUnitsPer100g = ingredient.getDouble("bread_units_per_100g");
                int gramsInUnit = ingredientUnit.getInt("grams_in_unit");
                String unitName = ingredientUnit.getJSONObject("unit").getString("name");
                double quantity = record.getDouble("quantity");
                String ingredientNameTranslated = getStringResource(ingredientName);
                String unitNameTranslated = getStringResource(unitName);
                title = String.format(Locale.getDefault(), "%s, %4.1f %s",
                        ingredientNameTranslated, quantity, unitNameTranslated);
            } catch (Exception e) {
                title = "ERROR";
                Log.e("REST", "Fails to parse json", e);
            }
            TextView view = (TextView) activity.getLayoutInflater()
                    .inflate(R.layout.meal_item, null);
            view.setText(title);
            return view;
        }

    }

}