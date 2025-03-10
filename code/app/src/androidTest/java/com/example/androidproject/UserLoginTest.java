package com.example.androidproject;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withResourceName;

import android.util.Log;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public class UserLoginTest {

    private CollectionReference usersRef;

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
    }

    @Before
    public void setUp(){
        Intents.init();
    }

    @Rule
    public ActivityScenarioRule<LoginActivity> scenarioRule = new ActivityScenarioRule<LoginActivity>(LoginActivity.class);

    @Test
    public void LoginButUserAndPassAreWrongSoStaysOnLoginPage() throws InterruptedException {
        // Waits for the data to be seeded first and the activity is on the SignupPage
        // Ensures the data is seeded first
        Thread.sleep(1000);
        // Type into the username slot
        onView(withHint("Username")).perform(typeText("user2"));
        onView(withHint("Password")).perform(typeText("pass1"));
        onView(withResourceName(("signUpButton"))).perform(click());
        // Asserts that user stays on the same page:
        Assert.assertTrue(Intents.getIntents().isEmpty());
        // Reset the intent
        Intents.release();
    }

    @Test
    public void loginUsingCorrectInfoSoGoesToHomePage() throws InterruptedException {;
        // Ensures the data is seeded first
        Thread.sleep(1000);
        // Type into the username slot
        onView(withHint("Username")).perform(typeText("user1"));
        onView(withHint("Password")).perform(typeText("pass1"));
        onView(withResourceName(("signUpButton"))).perform(click());
        // Ensures that user goes to the home page (AKA they're logged in):
        intended(hasComponent(HomePageActivity.class.getName()));
        Thread.sleep(2000);
        // Reset the intent
        Intents.release();
    }

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
