package com.flumine.typeone;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.test.espresso.contrib.PickerActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onIdle;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

public class RecordsTest {

    @Rule
    public ActivityScenarioRule<RecordsActivity> activityRule
            = new ActivityScenarioRule<>(RecordsActivity.class);

    @Test
    public void testRefresh() {
        onView(withId(R.id.refresh)).perform(click());
    }

    @Test
    public void testAddLong() {
        onView(withId(R.id.new_long)).perform(click());
        onView(withId(R.id.date_string)).perform(click());
        onView(withClassName(Matchers.equalTo(DatePickerDialog.class.getName()))).perform(PickerActions.setDate(1961, 4, 12));
        onView(withClassName(Matchers.equalTo(TimePickerDialog.class.getName()))).perform(PickerActions.setTime(9, 7));
        onView(withId(R.id.shot)).perform(typeText("22"), closeSoftKeyboard());
        onView(withId(R.id.submit)).perform(click());
    }

}