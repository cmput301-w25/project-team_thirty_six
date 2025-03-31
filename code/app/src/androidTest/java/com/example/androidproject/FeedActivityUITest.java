package com.example.androidproject;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


import android.content.Intent;

import com.example.androidproject.FeedActivity;

@RunWith(AndroidJUnit4.class)
public class FeedActivityUITest {

    private FeedActivity feedActivity;

    @Rule
    public ActivityTestRule<FeedActivity> activityRule = new ActivityTestRule<>(FeedActivity.class, true, false);

    @Before
    public void setUp() {
        // Setup initial conditions for the test
        Intent intent = new Intent();
        intent.putExtra("currentUser", "testUser");
        activityRule.launchActivity(intent);
    }

    @Test
    public void testFilterButtonVisibility() {
        // Check if the filter button is visible
        Espresso.onView(withId(R.id.filter_button)).check(matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testFilterButtonClick() {
        // Perform a click on the filter button and check if dialog appears
        Espresso.onView(withId(R.id.filter_button)).perform(click());
        // Check if dialog title is correct
        Espresso.onView(withText("Filter by")).check(matches(ViewMatchers.isDisplayed()));
    }

}
