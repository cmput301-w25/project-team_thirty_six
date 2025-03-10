package com.example.androidproject;

//
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import androidx.test.espresso.intent.Intents;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withResourceName;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.graphics.Movie;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.hamcrest.Matchers;
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

import kotlin.jvm.JvmField;

public class UserSignupTest {

    private CollectionReference usersRef;
    private User currentUser;

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

    @Rule
    public ActivityScenarioRule<SignUpActivity> scenarioRule = new ActivityScenarioRule<SignUpActivity>(SignUpActivity.class);

    @Before
    public void setUp(){
        Intents.init();
    }



    @Test
    public void signUpAttemptUserAlreadyExistsShouldReject() throws InterruptedException {
        // Waits for the data to be seeded first and the activity is on the SignupPage
        // Ensures the data is seeded first
        Thread.sleep(1000);
        // Type into the username slot
        onView(withHint("Username")).perform(typeText("user1"));
        onView(withHint("Password")).perform(typeText("pass1"));
        onView(withResourceName(("signUpButton"))).perform(click());
        // Asserts that user stays on the same page:
        Assert.assertTrue(Intents.getIntents().isEmpty());
        // Reset the intent
        Intents.release();
    }

    @Test
    public void signUpAttemptShouldGoToHomePageSinceUserExistsInDatabase() throws InterruptedException {;
        // Ensures the data is seeded first
        Thread.sleep(1000);
        // Type into the username slot
        onView(withHint("Username")).perform(typeText("user2"));
        onView(withHint("Password")).perform(typeText("pass2"));
        onView(withResourceName(("signUpButton"))).perform(click());
        // Ensures that user goes to the home page:
        intended(hasComponent(HomePageActivity.class.getName()));

        // CHECK IF THE USER IS IN THE DATABASE NOW
        // Creates the query for a matching username and password
        Task<QuerySnapshot> query = usersRef.whereEqualTo(FieldPath.documentId(), "user2").whereEqualTo("password", "pass2").get();
        // Perform the query and checks
        query.addOnSuccessListener(queryDocumentSnapshots ->{
            int i = queryDocumentSnapshots.size();
            currentUser = queryDocumentSnapshots.toObjects(User.class).get(0);
        }).addOnFailureListener(error -> {
            Log.w("UserManager", "Could not connect to database");
        });
        Thread.sleep(2000);
        Assert.assertEquals("user2", currentUser.username);
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
