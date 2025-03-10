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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MoodHistoryActivityUITest {

    private static FirebaseFirestore db;
    private static CollectionReference usersRef, moodsRef;

    @Rule
    public ActivityScenarioRule<MoodHistoryActivity> scenario = new
            ActivityScenarioRule<MoodHistoryActivity>(MoodHistoryActivity.class);

    @BeforeClass
    public static void setup(){
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";

        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }

    @Before
    public void seedDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.usersRef = db.collection("Users");
        User user1 = new User("user1", "pass1");
        usersRef.document(user1.getUsername()).set(user1);
        this.moodsRef = db.collection("Moods");
        MoodState mood = new MoodState("Anger");
        mood.setId("testMoodId");
        mood.setUser("user1");
        mood.setReason("Test not working");
        Database.getInstance().addMood(mood);

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