package com.example.androidproject;
import static androidx.test.core.app.ActivityScenario.launch;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withResourceName;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static androidx.test.rule.GrantPermissionRule.grant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.internal.platform.content.PermissionGranter;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import com.example.androidproject.Database;
import com.example.androidproject.LocationMapActivity;
import com.example.androidproject.MoodDetailsActivity;
import com.example.androidproject.MoodState;
import com.example.androidproject.User;
import com.google.android.gms.maps.SupportMapFragment;
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
import java.security.Permission;
import java.util.Objects;

/**
 * Creates the comment tests
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MapTest {
    private MoodState mood;
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
        mood = new MoodState("Anger");
        mood.setUser("TestPostUser");
        mood.setVisibility(Boolean.TRUE);
        mood.setReason("THIS TEXT IS FILLER");
        Location location = new Location(LocationManager.GPS_PROVIDER);
        // Sets location info
        location.setMslAltitudeAccuracyMeters(0);
        location.setMslAltitudeMeters(0);
        location.setLatitude(30.9);
        location.setLongitude(40.5);
        mood.setLocation(location);
        // Adds the mood
        db.addMood(mood);
    }

    /**
     *  Display the map with a mood
     */
    @Test
    public void displayPersonalMap() throws UiObjectNotFoundException {
        // Opens the map activity
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), LocationMapActivity.class);
        intent.putExtra("currentUser", "TestPostUser");
        launch(intent);
        // Lets teh emulator catch up
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Taken from https://stackoverflow.com/questions/25747514/how-can-i-programmatically-select-an-android-map-v2-marker-to-test-the-onclick-b
        // Authored By: kalin
        // Taken By: Dalton Low
        // Taken On: March 28, 2025
        UiDevice uiDevice = UiDevice.getInstance(getInstrumentation());
        // Checks if the marker exists
        assert(uiDevice.findObject(new UiSelector().descriptionContains("@TestPostUser")).exists());
        //Clicks it to get the details
        UiObject angryMarker = uiDevice.findObject(new UiSelector().descriptionContains("@TestPostUser"));
        angryMarker.click();
    }

    /**
     *  Display the map with a mood from the feed
     */
    @Test
    public void displayFeedMap() throws UiObjectNotFoundException {
        // Creates a new user for the feed to test
        User newUser = new User("MR BLUE","0");
        newUser.addFollowing("TestPostUser");
        Database.getInstance().addUser(newUser);
        // Opens the map activity
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), LocationMapActivity.class);
        intent.putExtra("currentUser", "MR BLUE");
        launch(intent);
        // Lets the emulator catch up
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Moves to the following map screen
        onView(withId(R.id.following_button_map)).perform(ViewActions.click());
        // Lets the emulator catch up
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Taken from https://stackoverflow.com/questions/25747514/how-can-i-programmatically-select-an-android-map-v2-marker-to-test-the-onclick-b
        // Authored By: kalin
        // Taken By: Dalton Low
        // Taken On: March 28, 2025
        UiDevice uiDevice = UiDevice.getInstance(getInstrumentation());
        // Checks if the marker exists
        assert(uiDevice.findObject(new UiSelector().descriptionContains("@TestPostUser")).exists());
        //Clicks it to get the details
        UiObject angryMarker = uiDevice.findObject(new UiSelector().descriptionContains("@TestPostUser"));
        angryMarker.click();
    }

    /**
     *  Display the map filtered by time to remove an item
     */
    @Test
    public void displayOneWeekFilterRemovingItem() throws UiObjectNotFoundException {
        // Subtracts 2 weeks from the mood
        mood.setDayTime(mood.getDayTime().minusWeeks(2));
        // Updates the document in firestore
        FirebaseFirestore.getInstance().collection("Moods").document(mood.getId()).set(mood);
        // Opens the map activity
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), LocationMapActivity.class);
        intent.putExtra("currentUser", "TestPostUser");
        launch(intent);
        // Lets teh emulator catch up
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Pulls up the filter
        onView(withId(R.id.map_filter)).perform(ViewActions.click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // FIlters by week
        onView(withId(R.id.check_recent_week)).perform(ViewActions.click());
        onView(withText("APPLY")).perform(ViewActions.click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Taken from https://stackoverflow.com/questions/25747514/how-can-i-programmatically-select-an-android-map-v2-marker-to-test-the-onclick-b
        // Authored By: kalin
        // Taken By: Dalton Low
        // Taken On: March 28, 2025
        UiDevice uiDevice = UiDevice.getInstance(getInstrumentation());
        // Checks if the marker exists
        assertFalse(uiDevice.findObject(new UiSelector().descriptionContains("@TestPostUser")).exists());
    }

    /**
     *  Display the map filtered by time without removing an time
     */
    @Test
    public void displayOneWeekFilterItemRemains() throws UiObjectNotFoundException {
        // Opens the map activity
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), LocationMapActivity.class);
        intent.putExtra("currentUser", "TestPostUser");
        launch(intent);
        // Lets teh emulator catch up
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Pulls up the filter
        onView(withId(R.id.map_filter)).perform(ViewActions.click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Filters by week
        onView(withId(R.id.check_recent_week)).perform(ViewActions.click());
        onView(withText("APPLY")).perform(ViewActions.click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Taken from https://stackoverflow.com/questions/25747514/how-can-i-programmatically-select-an-android-map-v2-marker-to-test-the-onclick-b
        // Authored By: kalin
        // Taken By: Dalton Low
        // Taken On: March 28, 2025
        UiDevice uiDevice = UiDevice.getInstance(getInstrumentation());
        // Checks if the marker exists
        assert(uiDevice.findObject(new UiSelector().descriptionContains("@TestPostUser")).exists());
    }

    /**
     *  Display the map with a mood
     */
    @Test
    public void displayReasonFilterRemovingItem() throws UiObjectNotFoundException {
        // Opens the map activity
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), LocationMapActivity.class);
        intent.putExtra("currentUser", "TestPostUser");
        launch(intent);
        // Lets teh emulator catch up
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Pulls up the filter
        onView(withId(R.id.map_filter)).perform(ViewActions.click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Filters by word
        onView(withId(R.id.check_filter_keyword)).perform(ViewActions.click());
        onView(withId(R.id.edit_keyword)).perform(ViewActions.typeText("jjjjjjjjj"));

        onView(withText("APPLY")).perform(ViewActions.click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Taken from https://stackoverflow.com/questions/25747514/how-can-i-programmatically-select-an-android-map-v2-marker-to-test-the-onclick-b
        // Authored By: kalin
        // Taken By: Dalton Low
        // Taken On: March 28, 2025
        UiDevice uiDevice = UiDevice.getInstance(getInstrumentation());
        // Checks if the marker exists
        assertFalse(uiDevice.findObject(new UiSelector().descriptionContains("@TestPostUser")).exists());
    }
    /**
     *  Display the map with a mood
     */
    @Test
    public void displayReasonFilterItemRemains() throws UiObjectNotFoundException {
        // Opens the map activity
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), LocationMapActivity.class);
        intent.putExtra("currentUser", "TestPostUser");
        launch(intent);
        // Lets teh emulator catch up
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Pulls up the filter
        onView(withId(R.id.map_filter)).perform(ViewActions.click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Filters by word
        onView(withId(R.id.check_filter_keyword)).perform(ViewActions.click());
        onView(withId(R.id.edit_keyword)).perform(ViewActions.typeText("filler"));

        onView(withText("APPLY")).perform(ViewActions.click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Taken from https://stackoverflow.com/questions/25747514/how-can-i-programmatically-select-an-android-map-v2-marker-to-test-the-onclick-b
        // Authored By: kalin
        // Taken By: Dalton Low
        // Taken On: March 28, 2025
        UiDevice uiDevice = UiDevice.getInstance(getInstrumentation());
        // Checks if the marker exists
        assert(uiDevice.findObject(new UiSelector().descriptionContains("@TestPostUser")).exists());
    }

    /**
     *  Display the map with a mood filtering out an item
     */
    @Test
    public void displayMoodFilterRemovingItem() throws UiObjectNotFoundException {
        // Opens the map activity
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), LocationMapActivity.class);
        intent.putExtra("currentUser", "TestPostUser");
        launch(intent);
        // Lets teh emulator catch up
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Pulls up the filter
        onView(withId(R.id.map_filter)).perform(ViewActions.click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Filters by mood
        onView(withId(R.id.check_filter_mood)).perform(ViewActions.click());
        onView(withId(R.id.spinner_moods)).perform(ViewActions.click());
        UiDevice.getInstance(getInstrumentation()).findObject(new UiSelector().textContains("Fear")).click();

        onView(withText("APPLY")).perform(ViewActions.click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Taken from https://stackoverflow.com/questions/25747514/how-can-i-programmatically-select-an-android-map-v2-marker-to-test-the-onclick-b
        // Authored By: kalin
        // Taken By: Dalton Low
        // Taken On: March 28, 2025
        UiDevice uiDevice = UiDevice.getInstance(getInstrumentation());
        // Checks if the marker exists
        assertFalse(uiDevice.findObject(new UiSelector().descriptionContains("@TestPostUser")).exists());
    }

    /**
     *  Display the map with a mood filtering out an item
     */
    @Test
    public void displayMoodFilterItemRemains() throws UiObjectNotFoundException {
        // Opens the map activity
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), LocationMapActivity.class);
        intent.putExtra("currentUser", "TestPostUser");
        launch(intent);
        // Lets teh emulator catch up
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Pulls up the filter
        onView(withId(R.id.map_filter)).perform(ViewActions.click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Filters by mood
        onView(withId(R.id.check_filter_mood)).perform(ViewActions.click());
        onView(withId(R.id.spinner_moods)).perform(ViewActions.click());
        UiDevice.getInstance(getInstrumentation()).findObject(new UiSelector().textContains("Anger")).click();

        onView(withText("APPLY")).perform(ViewActions.click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Taken from https://stackoverflow.com/questions/25747514/how-can-i-programmatically-select-an-android-map-v2-marker-to-test-the-onclick-b
        // Authored By: kalin
        // Taken By: Dalton Low
        // Taken On: March 28, 2025
        UiDevice uiDevice = UiDevice.getInstance(getInstrumentation());
        // Checks if the marker exists
        assert(uiDevice.findObject(new UiSelector().descriptionContains("@TestPostUser")).exists());
    }

    /**
     *  Display the map with a mood filtering out an item
     */
    @Test
    public void displayLocationFilterRemovingItem() throws UiObjectNotFoundException {
        // Creates a new user for the feed to test
        User newUser = new User("MR BLUE","0");
        newUser.addFollowing("TestPostUser");
        Database.getInstance().addUser(newUser);
        // Opens the map activity
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), LocationMapActivity.class);
        intent.putExtra("currentUser", "MR BLUE");
        launch(intent);
        // Lets teh emulator catch up
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Switches to following map
        onView(withId(R.id.following_button_map)).perform(ViewActions.click());
        // Lets teh emulator catch up
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Pulls up the filter
        onView(withId(R.id.map_filter)).perform(ViewActions.click());
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Filters by distance
        onView(withId(R.id.check_nearby_following)).perform(ViewActions.click());
        onView(withText("APPLY")).perform(ViewActions.click());
        // Confirms the location request
        onView(withId(R.id.allowButton)).perform(ViewActions.click());
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Taken from https://stackoverflow.com/questions/25747514/how-can-i-programmatically-select-an-android-map-v2-marker-to-test-the-onclick-b
        // Authored By: kalin
        // Taken By: Dalton Low
        // Taken On: March 28, 2025
        UiDevice uiDevice = UiDevice.getInstance(getInstrumentation());
        // Grants the phone permissions
        uiDevice.findObject(new UiSelector().textContains("Only")).click();
        // Lets the emulator catch up
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Checks if the marker exists
        assertFalse(uiDevice.findObject(new UiSelector().descriptionContains("@TestPostUser")).exists());
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

