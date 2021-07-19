package com.flumine.typeone;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MealActivity extends BaseActivity {

    private int recordId;
    private int ingredientUnitId;
    private int ingredientId;
    private int mealId;
    private double quantity;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(getClass().getName(), "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.meal_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == (R.id.delete_listed_meal) && mealId != 0) {
            deleteMeal();
            back(null);
        }
        return true;
    }

    private void deleteMeal() {
        Log.d("REST", "Deleting record with id " + recordId);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.DELETE, BASE_URL.concat("/api/record/" + recordId +"/meal/" + mealId + "/"),
                null,
                response -> {
                    back(null);
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
        Volley.newRequestQueue(MealActivity.this).add(jsonObjectRequest);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recordId = getIntent().getIntExtra("RECORD_ID", 0);
        ingredientUnitId = getIntent().getIntExtra("INGREDIENT_UNIT_ID", 0);
        mealId = getIntent().getIntExtra("MEAL_ID", 0);
        quantity = getIntent().getDoubleExtra("QUANTITY", 0);
        Log.d("REST",
                String.format("Intent %s is RECORD_ID:%d INGREDIENT_UNIT_ID:%d MEAL_ID:%d QUANTITY:%f",
                        getIntent().hashCode(), recordId, ingredientUnitId, mealId, quantity));
        setContentView(R.layout.activity_meal);
        getMeal();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("REST", "MealActivity.onNewIntent " + intent.hashCode());
        super.onNewIntent(intent);
        recordId = intent.getIntExtra("RECORD_ID", 0);
        ingredientUnitId = intent.getIntExtra("INGREDIENT_UNIT_ID", 0);
        mealId = intent.getIntExtra("MEAL_ID", 0);
        quantity = intent.getDoubleExtra("QUANTITY", 0);
        Log.d("REST",
                String.format("New intent %s is RECORD_ID:%d INGREDIENT_UNIT_ID:%d MEAL_ID:%d QUANTITY:%f",
                        intent.hashCode(), recordId, ingredientUnitId, mealId, quantity));
        getMeal();
    }

    private void getMeal() {
        findViewById(R.id.timer).setVisibility(View.VISIBLE);
        findViewById(R.id.layout).setVisibility(View.GONE);
        Log.d("REST", "Activity intent record id is " + recordId);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, BASE_URL.concat("/api/ingredient/" + ingredientUnitId +"/"),
                null,
                response -> {
                    try {
                        ingredientId = response.getJSONObject("ingredient").getInt("id");
                        refresh(response, quantity);
                    } catch (Exception e) {
                        Log.e("REST", e.getMessage());
                    } finally {
                        findViewById(R.id.timer).setVisibility(View.GONE);
                        findViewById(R.id.layout).setVisibility(View.VISIBLE);
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
        Volley.newRequestQueue(MealActivity.this).add(jsonObjectRequest);
    }

    private void refresh(JSONObject response, double quantity) {
        RadioGroup radioGroup = findViewById(R.id.hints);
        try {
            ((TextView)findViewById(R.id.ingredient_name)).setText(
                    getStringResource(response.getJSONObject("ingredient").getString("name")));
            ((TextView)findViewById(R.id.unit_name)).setText(
                    getStringResource(response.getJSONObject("unit").getString("name")));
            final TextView quantityValue = (EditText)findViewById(R.id.quantity_value);
            quantityValue.setText(String.valueOf(quantity));
            String grammsInUnitStr = response.getString("grams_in_unit");
            JSONArray hints = response.getJSONObject("ingredient").getJSONArray("hints");
            radioGroup.removeAllViews();
            for (int i=0; i < hints.length(); i++) {
                final JSONObject hintRecord = (JSONObject)hints.get(i);
                final String grammsInHintStr = hintRecord.getString("grams_in_hint");
                String rawImage = hintRecord.getString("thumb");
                byte[] image = Base64.decode(rawImage, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                Drawable thumbBitmap = new BitmapDrawable(Resources.getSystem(), bitmap);
                thumbBitmap.setBounds(0, 0, 640, 480);

                int grammsInHint = Integer.parseInt(grammsInHintStr);
                float grammsInUnit = Float.parseFloat(grammsInUnitStr);
                String unitsInHintStr = String.valueOf(grammsInHint/grammsInUnit);
                Log.d("REST", "Units in hint = " + unitsInHintStr);

                RadioButton radioButton = new RadioButton(getApplicationContext());
                radioButton.setCompoundDrawables(null, null, null, thumbBitmap);
                radioButton.setText(unitsInHintStr);
                Log.d("REST", "Units in hint set = " + radioButton.getText());
                radioButton.setOnClickListener(v -> {
                        quantityValue.setText(unitsInHintStr);
                });
                radioGroup.addView(radioButton);
            }
        } catch (Exception e) {
            Log.e("REST", "Failed to show meal", e);
        }
    }

    public void back(View view) {
        Intent intent = new Intent(this,
                mealId == 0 ? IngredientsActivity.class : MealsActivity.class);
        intent.putExtra("RECORD_ID", recordId);
        startActivity(intent);
    }

    public void store(View view) throws Exception {
        if (mealId == 0) {
            store();
        } else {
            update();
        }
    }

    public void update() throws Exception {

        findViewById(R.id.timer).setVisibility(View.VISIBLE);
        findViewById(R.id.layout).setVisibility(View.GONE);

        JSONObject record = new JSONObject();

        record.put("id", mealId);
        record.put("record_id", recordId);
        record.put("ingredient_unit_id", ingredientUnitId);
        record.put("quantity", ((TextView)findViewById(R.id.quantity_value)).getText());

        Log.d("REST", "Storing " + record.toString());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                // /api/record/1/meal/16/
                Request.Method.PUT, BASE_URL.concat("/api/record/" + recordId +"/meal/" + mealId + "/"),
                record,
                response -> {
                    try {
                        Log.d("REST", "create meal response is " + response.toString());
                    } catch (Exception e) {
                        findViewById(R.id.timer).setVisibility(View.GONE);
                        findViewById(R.id.layout).setVisibility(View.VISIBLE);
                        Log.e("REST", e.getMessage());
                    } finally {
                        Intent intent = new Intent(this, MealsActivity.class);
                        intent.putExtra("RECORD_ID", recordId);
                        startActivity(intent);
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

        Volley.newRequestQueue(MealActivity.this).add(jsonObjectRequest);
    }

    public void store() throws Exception {

        findViewById(R.id.timer).setVisibility(View.VISIBLE);
        findViewById(R.id.layout).setVisibility(View.GONE);

        JSONObject record = new JSONObject();

        record.put("record_id", recordId);
        record.put("ingredient_unit_id", ingredientUnitId);
        record.put("quantity", ((TextView)findViewById(R.id.quantity_value)).getText());

        Log.d("REST", "Storing " + record.toString());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, BASE_URL.concat("/api/record/" + recordId +"/meals/"),
                record,
                response -> {
                    try {
                        Log.d("REST", "create meal response is " + response.toString());
                    } catch (Exception e) {
                        findViewById(R.id.timer).setVisibility(View.GONE);
                        findViewById(R.id.layout).setVisibility(View.VISIBLE);
                        Log.e("REST", e.getMessage());
                    } finally {
                        Intent intent = new Intent(this, MealsActivity.class);
                        intent.putExtra("RECORD_ID", recordId);
                        startActivity(intent);
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

        Volley.newRequestQueue(MealActivity.this).add(jsonObjectRequest);

    }

    public void addHint(View view) {
        Intent intent = new Intent(this, HintActivity.class);
        intent.putExtra("RECORD_ID", recordId);
        intent.putExtra("MEAL_ID", recordId);
        intent.putExtra("INGREDIENT_UNIT_ID", ingredientUnitId);
        intent.putExtra("INGREDIENT_ID", ingredientUnitId);
        startActivity(intent);
    }

}