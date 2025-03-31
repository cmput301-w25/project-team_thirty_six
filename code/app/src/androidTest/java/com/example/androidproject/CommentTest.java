package com.example.androidproject;

import static androidx.test.core.app.ActivityScenario.launch;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;
import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * Creates the comment tests
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class CommentTest {
    String moodID;
    /**
     * Sets up firebase and firestore
     */
    @BeforeClass
    public static void setup(){
        // Connects to the database
        FirebaseFirestore.getInstance().useEmulator("10.0.2.2",8080);
        FirebaseStorage.getInstance().useEmulator("10.0.2.2",9090);
    }

    /**
     * Gives the mood details screen something to start with
     */
    @Before
    public void startTest(){
        // Gets the database
        Database db = Database.getInstance();
        // Creates a user who posted the mood
        User testPostUser = new User("TestPostUser","0");
        db.addUser(testPostUser);
        // Creates a post to be commented on
        MoodState testOriginalPost = new MoodState("Anger");
        testOriginalPost.setUser("TestPostUser");
        testOriginalPost.setVisibility(Boolean.TRUE);
        testOriginalPost.setReason("THIS TEXT IS FILLER");
        db.addMood(testOriginalPost);
        moodID = testOriginalPost.getId();

    }

    /**
     * Creates a test for commenting on your own mood post
     */
    @Test
    public void commentOnOwnPost(){
        // Opens the mood details activity
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MoodDetailsActivity.class);
        intent.putExtra("id", moodID);
        intent.putExtra("user","TestPostUser");
        launch(intent);
        // Sleeps to allow the emulator to catch up
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Types text
        onView(withId(R.id.comment_text)).perform(ViewActions.typeText("THIS IS MY COMMENT"));
        // Confirms the text
        onView(withId(R.id.confirm_comment_button)).perform(ViewActions.click());
        // Sleeps to allow the emulator to catch up
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Checks that the comment is displayed
        onView(withText("THIS IS MY COMMENT")).check(matches(isDisplayed()));
        // Checks that the user is correct
        onView(withId(R.id.comment_username_display)).check(matches(withText("TestPostUser")));
    }

    /**
     * Creates a test for commenting on someone elses comment
     */
    @Test
    public void commentOnOthersPost(){
        // Opens the mood details activity
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MoodDetailsActivity.class);
        intent.putExtra("id", moodID);
        intent.putExtra("user","MR. BLUE");
        launch(intent);
        // Sleeps to allow the emulator to catch up
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Types text
        onView(withId(R.id.comment_text)).perform(ViewActions.typeText("THIS IS MY COMMENT"));
        // Confirms the text
        onView(withId(R.id.confirm_comment_button)).perform(ViewActions.click());
        // Sleeps to allow the emulator to catch up
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Checks that the comment is displayed
        onView(withText("THIS IS MY COMMENT")).check(matches(isDisplayed()));
        // Checks that the user is correct
        onView(withId(R.id.comment_username_display)).check(matches(withText("MR. BLUE")));
    }

    /**
     * Creates a test for empty comment
     */
    @Test
    public void emptyComment() {
        // Opens the mood details activity
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MoodDetailsActivity.class);
        intent.putExtra("id", moodID);
        intent.putExtra("user", "MR. BLUE");
        launch(intent);
        // Sleeps to allow the emulator to catch up
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Clicks the confirm button with no text present
        onView(withId(R.id.confirm_comment_button)).perform(ViewActions.click());
        // Sleeps to allow the emulator to catch up
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Checks that the comment is not displayed
        onView(withId(R.id.comment_text_display)).check(doesNotExist());
        // Checks that the user is not displayed
        onView(withId(R.id.comment_username_display)).check(doesNotExist());
    }

    /**
     * Removes all the info from the emulator database
     */
    @After
    public void tearDown() {
        String projectId = "mooddatabase-4398e";
        URL url = null;
        try {
            url = new URL("http://10.0.2.2:8080/emulator/v1/projects/" + projectId + "/databases/(default)/documents");
        } catch (MalformedURLException exception) {
            Log.e("URL Error", Objects.requireNonNull(exception.getMessage()));
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            int response = urlConnection.getResponseCode();
            Log.i("Response Code", "Response Code: " + response);
        } catch (IOException exception) {
            Log.e("IO Error", Objects.requireNonNull(exception.getMessage()));
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}
