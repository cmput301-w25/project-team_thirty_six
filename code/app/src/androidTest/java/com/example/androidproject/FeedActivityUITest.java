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

/**
 * Test class for FeedActivity using Espresso framework.
 * Tests the user interface and interactions for the mood feed functionality.
 */

@RunWith(AndroidJUnit4.class)
public class FeedActivityUITest {

    private FeedActivity feedActivity;

    @Rule
    public ActivityTestRule<FeedActivity> activityRule = new ActivityTestRule<>(FeedActivity.class, true, false);

    /**
     * Setup method executed before each test.
     * Launches the activity with test user credentials.
     */
    @Before
    public void setUp() {
        // Setup initial conditions for the test
        Intent intent = new Intent();
        intent.putExtra("currentUser", "testUser");
        activityRule.launchActivity(intent);
    }
    /**
     * Tests visibility of the filter button.
     * Verifies the filter button is properly displayed on screen.
     */
    @Test
    public void testFilterButtonVisibility() {
        // Check if the filter button is visible
        Espresso.onView(withId(R.id.filter_button)).check(matches(ViewMatchers.isDisplayed()));
    }

    /**
     * Tests filter button click behavior.
     * Verifies that clicking the filter button opens the filter dialog
     * with the correct title.
     */
    @Test
    public void testFilterButtonClick() {
        // Perform a click on the filter button and check if dialog appears
        Espresso.onView(withId(R.id.filter_button)).perform(click());
        // Check if dialog title is correct
        Espresso.onView(withText("Filter by")).check(matches(ViewMatchers.isDisplayed()));
    }

}
