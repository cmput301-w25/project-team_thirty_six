package com.example.androidproject;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;
import android.graphics.Movie;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MoodHistoryActivityUITest {

//    @Rule
//    public ActivityScenarioRule<MainActivity> scenario = new
//            ActivityScenarioRule<MainActivity>(MainActivity.class);

    @BeforeClass
    public static void setup() {
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";

        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }

    @Before
    public void launchActivity() {
        // Create an Intent to launch MoodHistoryActivity with the currentUser set to "testUser"

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), CreatePostActivity.class);
        intent.putExtra("currentUser", "testUser");
        ActivityScenario.launch(intent);
        // Gets the mood dropdown and selects anger
        onView(withId(R.id.add_select_mood_dropdown)).perform(ViewActions.click());
        onView(withText("Anger")).perform(ViewActions.click());

        //Sets the text and group for mood view
        onView(withId(R.id.add_reason)).perform(ViewActions.typeText("TEST TEXT"));
        closeSoftKeyboard();
        onView(withId(R.id.add_mood_group_button)).perform(ViewActions.click());
        onView(withId(R.id.add_confirm_button)).perform(ViewActions.click());


        Intent intent2 = new Intent(ApplicationProvider.getApplicationContext(), MoodHistoryActivity.class);
        intent2.putExtra("currentUser", "testUser");

        // Launch the activity with the custom Intent
        ActivityScenario.launch(intent2);
    }

    @Test
    public void testMoodListDisplayed() throws InterruptedException {
        Thread.sleep(5000);
        // Verify that the mood list is displayed
        onView(withId(R.id.mood_list)).check(matches(isDisplayed()));
    }

    @Test
    public void testFilterButtonClick() {
        // Click the filter button
        onView(withId(R.id.filter_button)).perform(click());

        // Verify that the filter dialog is displayed
        onView(withText("Filter by")).check(matches(isDisplayed()));
    }

    @Test
    public void testFilterByRecentWeek() {
        // Click the filter button
        onView(withId(R.id.filter_button)).perform(click());

        // Check the "Recent Week" checkbox
        onView(withId(R.id.check_recent_week)).perform(click());

        // Click the "Apply" button
        onView(withText("Apply")).perform(click());

        // Verify that the mood list is updated (you may need to add specific assertions)
        onView(withId(R.id.mood_list)).check(matches(isDisplayed()));
    }

    @Test
    public void testFilterByEmotionalState() {
        // Click the filter button
        onView(withId(R.id.filter_button)).perform(click());

        // Check the "Filter by Mood" checkbox
        onView(withId(R.id.check_filter_mood)).perform(click());

        // Select a mood from the spinner
        onView(withId(R.id.spinner_moods)).perform(click());
        onView(withText("Happy")).perform(click());

        // Click the "Apply" button
        onView(withText("Apply")).perform(click());

        // Verify that the mood list is updated (you may need to add specific assertions)
        onView(withId(R.id.mood_list)).check(matches(isDisplayed()));
    }

    @Test
    public void testFilterByKeyword() {
        // Click the filter button
        onView(withId(R.id.filter_button)).perform(click());

        // Check the "Filter by Keyword" checkbox
        onView(withId(R.id.check_filter_keyword)).perform(click());

        // Enter a keyword in the EditText
        onView(withId(R.id.edit_keyword)).perform(click(), (ViewAction) withText("great"));

        // Click the "Apply" button
        onView(withText("Apply")).perform(click());

        // Verify that the mood list is updated (you may need to add specific assertions)
        onView(withId(R.id.mood_list)).check(matches(isDisplayed()));
    }

    @Test
    public void testResetFilter() {
        // Click the filter button
        onView(withId(R.id.filter_button)).perform(click());

        // Click the "Reset" button
        onView(withText("Reset")).perform(click());

        // Verify that the mood list is displayed with all moods
        onView(withId(R.id.mood_list)).check(matches(isDisplayed()));
    }
}