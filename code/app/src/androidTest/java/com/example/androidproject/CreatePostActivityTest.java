package com.example.androidproject;
import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.junit.Assert.assertEquals;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Creates the create post activity test
 */
@RunWith (AndroidJUnit4.class)
@LargeTest
public class CreatePostActivityTest {
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
     * Switches to the create post activity
     */
    @Before
    public void switchActivity(){
        // Switches to activity
        ActivityScenario.launch(CreatePostActivity.class);
    }

    /**
     * Tests a successful addition
     */
    @Test
    public void successfulAddTest() {
        // Gets the mood dropdown and selects anger
        onView(withId(R.id.AddMoodSelectMood)).perform(ViewActions.click());
        onView(withText("Anger")).perform(ViewActions.click());

        //Sets the text and group for mood view
        onView(withId(R.id.addReason)).perform(ViewActions.typeText("TEST TEXT"));
        closeSoftKeyboard();
        onView(withId(R.id.AddradioGroup)).perform(ViewActions.click());
        onView(withId(R.id.add_done_button)).perform(ViewActions.click());
        // Checks the database
        Task<QuerySnapshot> query = FirebaseFirestore.getInstance().collection("Moods").get();
        query.addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            /**
             * Checks that the mood is in the database now
             * @param queryDocumentSnapshots
             */
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                // Ensures there is only one mood
                assertEquals(queryDocumentSnapshots.size(),1);
                // Turns mood into mood state
                DocumentSnapshot moodDoc = queryDocumentSnapshots.getDocuments().get(0);
                // Asserts all of its values are correct
                assertEquals(moodDoc.get("mood"),"Anger");
                assertEquals(moodDoc.get("reason"),"TEST TEXT");
                assertEquals(moodDoc.get("situation"),"Group");
                // Deletes the mood from the database
                FirebaseFirestore.getInstance().collection("Moods").document(moodDoc.getId()).delete();
            }
        });
        // Delays result
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests without mood selected
     */
    @Test
    public void noMoodAddTest() {

        //Sets the text and group for mood view
        onView(withId(R.id.addReason)).perform(ViewActions.typeText("TEST TEXT"));
        closeSoftKeyboard();
        onView(withId(R.id.AddradioGroup)).perform(ViewActions.click());
        // Clicks the button
        onView(withId(R.id.add_done_button)).perform(ViewActions.click());
        // Checks the database to ensure it was not added
        Task<QuerySnapshot> query = FirebaseFirestore.getInstance().collection("Moods").get();
        query.addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                // Ensures there is no moods in database
                assertEquals(queryDocumentSnapshots.size(),0);
            }
        });
        // Delays result
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
