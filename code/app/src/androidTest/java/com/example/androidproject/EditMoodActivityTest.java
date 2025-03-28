package com.example.androidproject;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.junit.Assert.assertEquals;

import android.content.Intent;
import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * This test verifies that a mood seeded by the database can be
 * edited through the app's UI
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EditMoodActivityTest {
    private static FirebaseFirestore db;
    private static CollectionReference usersRef;
    private static CollectionReference moodsRef;

    /**
     * Start up the emulator before the tests
     */
    @BeforeClass
    public static void connectToEmulator() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.useEmulator("10.0.2.2", 8080);
        FirebaseStorage.getInstance().useEmulator("10.0.2.2", 9199);
    }
    /**
     * Seed the data base with a mock user and mock mood
     */
    @Before
    public void setup() {
        db = FirebaseFirestore.getInstance(); // do NOT call useEmulator here

        usersRef = db.collection("Users");
        moodsRef = db.collection("Moods");

        User user1 = new User("user1", "pass1");
        usersRef.document(user1.getUsername()).set(user1);

        MoodState mood = new MoodState("Anger");
        mood.setId("testMoodId");
        mood.setUser("user1");
        mood.setReason("Test not working");

        moodsRef.document(mood.getId()).set(mood);
    }

    /**
     * Test the ability to open and interact with the EditMoodActivity.
     * This tests if a mood can be edited
     * @throws InterruptedException
     */
    @Test
    public void useEditMoodToChangeAMood() throws InterruptedException {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MoodHistoryActivity.class);
        intent.putExtra("currentUser", "user1");

        ActivityScenario<MoodHistoryActivity> scenario = ActivityScenario.launch(intent);

        Thread.sleep(1000);
        onView(withText("View More Details")).perform(click());
        Thread.sleep(1000);
        onView(withText("Edit")).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.btnEditMoodSelectMood)).perform(click());
        onView(withText("Happiness")).perform(click());
        onView(withId(R.id.done_button)).perform(click());
        onView(withText("Happiness")).check(matches(isDisplayed()));
    }

    /**
     * This test is used to confirm if the social situation can be edited
     * @throws InterruptedException
     */
    @Test
    public void useEditMoodToChangeSocialSituation() throws InterruptedException {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MoodHistoryActivity.class);
        intent.putExtra("currentUser", "user1");

        ActivityScenario<MoodHistoryActivity> scenario = ActivityScenario.launch(intent);

        Thread.sleep(1000);
        onView(withText("View More Details")).perform(click());
        Thread.sleep(1000);
        onView(withText("Edit")).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.radioAlone)).perform(scrollTo(), click());
        onView(withId(R.id.done_button)).perform(click());
        onView(withText("Social Situation: Alone")).check(matches(isDisplayed()));
    }
    /**
     * This test is used to confirm if the reason can be edited
     * @throws InterruptedException
     */
    @Test
    public void useEditMoodToChangeReason() throws InterruptedException {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MoodHistoryActivity.class);
        intent.putExtra("currentUser", "user1");

        ActivityScenario<MoodHistoryActivity> scenario = ActivityScenario.launch(intent);

        Thread.sleep(1000);
        onView(withText("View More Details")).perform(click());
        Thread.sleep(1000);
        onView(withText("Edit")).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.editReason)).perform(clearText(), typeText("This test works"), closeSoftKeyboard());
        onView(withId(R.id.done_button)).perform(click());
        onView(withText("This test works")).check(matches(isDisplayed()));
    }

    @After
    public void tearDown() {
        String projectId = "mooddatabase-4398e";
        try {
            URL url = new URL("http://10.0.2.2:8080/emulator/v1/projects/" + projectId + "/databases/(default)/documents");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            int response = urlConnection.getResponseCode();
            Log.i("Firestore Reset", "Response Code: " + response);
            urlConnection.disconnect();
        } catch (IOException e) {
            Log.e("Firestore Reset", "Error during teardown: " + e.getMessage());
        }
    }
}


