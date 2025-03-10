package com.example.androidproject;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MoodDetailsActivityIntentTest {

    private static final String TEST_MOOD_ID = "testMoodId";
    private static final String TEST_USER_ID = "testUser";

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testActivityReceivesIntentExtras() {
        // Create an Intent with test extras
        Intent intent = new Intent();
        intent.putExtra("id", TEST_MOOD_ID);
        intent.putExtra("user", TEST_USER_ID);

        // Launch MoodDetailsActivity
        try (ActivityScenario<MoodDetailsActivity> scenario = ActivityScenario.launch(intent)) {
            // Verify that the activity received the correct extras
            intended(hasExtra("id", TEST_MOOD_ID));
            intended(hasExtra("user", TEST_USER_ID));
        }
    }

    @Test
    public void testEditButtonLaunchesEditMoodActivity() {
        // Create an Intent with test extras
        Intent intent = new Intent();
        intent.putExtra("id", TEST_MOOD_ID);
        intent.putExtra("user", TEST_USER_ID);

        // Launch MoodDetailsActivity
        try (ActivityScenario<MoodDetailsActivity> scenario = ActivityScenario.launch(intent)) {
            // Click the Edit button (assumed to be R.id.button_edit)
            onView(withId(R.id.button_edit)).perform(click());

            // Verify that EditMoodActivity is launched with the correct extras
            intended(hasComponent(EditMoodActivity.class.getName()));
            intended(hasExtra("moodId", TEST_MOOD_ID));
        }
    }
}
