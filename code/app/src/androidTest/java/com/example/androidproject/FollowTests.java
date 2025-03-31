package com.example.androidproject;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withResourceName;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;

import android.content.Intent;
import android.util.Log;
import android.widget.EditText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class FollowTests {

    private CollectionReference usersRef;
    private User currentUser;

    @BeforeClass
    public static void setup(){
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";

        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }

    /**
     * Seeds the database with 2 users
     */
    @Before
    public void seedDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.usersRef = db.collection("Users");
        User user1 = new User("user1", "pass1");
        User user2 = new User("user2", "pass2");

        user1.followRequests.add("user2");

        usersRef.document(user1.getUsername()).set(user1);
        usersRef.document(user2.getUsername()).set(user2);

        // Set user1 as the logged in user
        this.currentUser = user1;

    }

    @Before
    public void setUp(){
        Intents.init();
    }

    @Rule
    public ActivityScenarioRule<LoginActivity> scenarioRule = new ActivityScenarioRule<LoginActivity>(LoginActivity.class);


    /**
     * This is a UI test to check if the user can accept a follow request then go on their following and
     * click on the user that just followed them and send them a follow request.
     * Checks that follower amounts and following amounts change accordingly.
     * Checks that when you click follow on another user, that the follow button changes its text to "Requested"
     * Checks that a follower is removed when you click "remove" from the follower fragment.
     * @throws InterruptedException
     */
    @Test
    public void acceptFollowRequestTestUI() throws InterruptedException {
        // Waits for the data to be seeded first

        // Ensures the data is seeded first
        Thread.sleep(1000);
        // Type into the username slot
        onView(withHint("Username")).perform(typeText("user1"));
        onView(withHint("Password")).perform(typeText("pass1"));
        onView(withResourceName(("signUpButton"))).perform(click());
        // Wait to get to homePage
        Thread.sleep(1000);
        // Click on the profile button
        onView(withId(R.id.btn_profile)).perform(click());
        // Click on the heart icon
        onView(withId(R.id.view_follow_requests_image_button)).perform(click());
        Thread.sleep(1000);

        // Accepts the follow request and goes back
        onView(withId(R.id.accept_button)).perform(click());
        onView(withId(R.id.button_back)).perform(click());

        // Asserts that user1 now has 1 follower:
        onView(withId(R.id.followerAmountTextView)).check(matches(withText("1 followers")));

        //opens the follower fragment
        onView(withId(R.id.followerAmountTextView)).perform(click());

        // Goes to user2's profile
        onView(withId(R.id.username_follower_text_view)).perform(click());
        Thread.sleep(1000);

        // Asserts that we can get to user2's profile from the followers page after accepting a follow request
        // and also that user2 now has 1 following:
        onView(withId(R.id.displayUsername)).check(matches(withText("user2")));
        onView(withId(R.id.followingAmountTextView)).check(matches(withText("1 following")));

        // Click Follow on user2
        onView(withId(R.id.follow_following_button)).perform(click());

        // Check that the follow button's text changed to "requested"
        onView(withId(R.id.follow_following_button)).check(matches(withText("Requested")));

        // Reset the intent
        Intents.release();
    }

    /**
     * This test rejectsa a follow request and checks to see whether the follower count changes
     * @throws InterruptedException For Thread.sleep()
     */
    @Test
    public void rejectFollowRequestTestUI() throws InterruptedException {
        // Waits for the data to be seeded first.

        // Ensures the data is seeded first
        Thread.sleep(1000);
        // Type into the username slot
        onView(withHint("Username")).perform(typeText("user1"));
        onView(withHint("Password")).perform(typeText("pass1"));
        onView(withResourceName(("signUpButton"))).perform(click());
        // Wait to get to homePage
        Thread.sleep(1000);
        // Click on the profile button
        onView(withId(R.id.btn_profile)).perform(click());
        // Click on the heart icon
        onView(withId(R.id.view_follow_requests_image_button)).perform(click());
        Thread.sleep(1000);

        // Rejects the follow request and goes back
        onView(withId(R.id.cancel_button)).perform(click());
        onView(withId(R.id.button_back)).perform(click());

        // Ensures that no follower was added.
        onView(withId(R.id.followerAmountTextView)).check(matches(withText("0 followers")));

        // Release the intents
        Intents.release();

    }

    /**
     * This test cancels a follow request and checks to see whether the text says "Requested"
     * and after clicking on it whether it says "Follow"
     * @throws InterruptedException For Thread.sleep()
     */
    @Test
    public void cancelFollowRequestTestUI() throws InterruptedException {

        // Waits for the data to be seeded first.

        // Ensures the data is seeded first
        Thread.sleep(1000);
        // Type into the username slot
        onView(withHint("Username")).perform(typeText("user2"));
        onView(withHint("Password")).perform(typeText("pass2"));
        onView(withResourceName(("signUpButton"))).perform(click());
        // Wait to get to homePage
        Thread.sleep(1000);
        // Click on the search button
        onView(withId(R.id.btn_search)).perform(click());
        Thread.sleep(1000);

        // click the search bar
        onView(withId(R.id.searchView)).perform(click());

        // Type out "user1"
        onView(isAssignableFrom(EditText.class))
                .perform(typeText("user1"), closeSoftKeyboard());

        // Click on "user1" and go to their profile
        onView(allOf(
                withText("user1"),
                isDescendantOfA(withId(R.id.recyclerView))
        )).perform(click());

        // Wait
        Thread.sleep(1000);

        // Checks to see if the text says "Requested"
        onView(withId(R.id.follow_following_button)).check(matches(withText("Requested")));

        // Click on the follow button
        onView(withId(R.id.follow_following_button)).perform(click());

        // Checks to see if the text changed to "Follow"
        onView(withId(R.id.follow_following_button)).check(matches(withText("Follow")));

        // release the intent
        Intents.release();
    }



    /**
     * This test removes a follower from a user and checks to see whether they were removed or not in
     * the user object
     * @throws InterruptedException For Thread.sleep()
     */
    @Test
    public void removeFollowerTestUI() throws InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.usersRef = db.collection("Users");

        User user1 = new User("user1", "pass1");
        User user2 = new User("user2", "pass2");

        user1.addFollower("user2");

        usersRef.document(user1.getUsername()).set(user1);
        usersRef.document(user2.getUsername()).set(user2);

        // Ensures the data is seeded first
        Thread.sleep(1000);
        // Type into the username slot
        onView(withHint("Username")).perform(typeText("user1"));
        onView(withHint("Password")).perform(typeText("pass1"));
        onView(withResourceName(("signUpButton"))).perform(click());
        // Wait to get to homePage
        Thread.sleep(1000);
        // Click on the profile button
        onView(withId(R.id.btn_profile)).perform(click());

        //opens the follower fragment
        onView(withId(R.id.followerAmountTextView)).perform(click());

        // Removes user2 as a follower and goes back
        onView(withId(R.id.remove_follower_button)).perform(click());
        onView(withId(R.id.button_back)).perform(click());

        // Asserts that user1 now has 0 followers:
        onView(withId(R.id.followerAmountTextView)).check(matches(withText("0 followers")));

        // Makes sure that the follower was removed thus the follower list is empty
        Assert.assertTrue("The followers list should be empty", currentUser.getFollowers().isEmpty());

        // Reset the intent
        Intents.release();
    }

    /**
     * This test unfollows a user and checks to see whether they were removed or not in the following list
     *  in the user object
     * @throws InterruptedException For Thread.sleep()
     */
    @Test
    public void unfollowTestUI() throws InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.usersRef = db.collection("Users");

        User user1 = new User("user1", "pass1");
        User user2 = new User("user2", "pass2");

        user1.addFollowing("user2");

        usersRef.document(user1.getUsername()).set(user1);
        usersRef.document(user2.getUsername()).set(user2);

        // Waits for the data to be seeded first
        Thread.sleep(1000);
        // Type into the username slot
        onView(withHint("Username")).perform(typeText("user1"));
        onView(withHint("Password")).perform(typeText("pass1"));
        onView(withResourceName(("signUpButton"))).perform(click());
        // Wait to get to homePage
        Thread.sleep(1000);
        // Click on the profile button
        onView(withId(R.id.btn_profile)).perform(click());

        //opens the follower fragment
        onView(withId(R.id.followingAmountTextView)).perform(click());

        // Removes user2 as a follower and goes back
        onView(withId(R.id.unfollow_button)).perform(click());
        onView(withId(R.id.button_back)).perform(click());

        // Asserts that user1 now has 0 followers:
        onView(withId(R.id.followingAmountTextView)).check(matches(withText("0 following")));

        // Makes sure that the follower was removed thus the follower list is empty
        Assert.assertTrue("The following list should be empty", currentUser.getFollowing().isEmpty());

        // Reset the intent
        Intents.release();
    }



    /**
     * Sends a Follow Request from user1 to user2 and ensures that that change was reflected in the database.
     * @throws InterruptedException For Thread.sleep()
     */
    @Test
    public void sendFollowRequestDatabaseTest() throws InterruptedException {

        // Set up the database

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.usersRef = db.collection("Users");

        User user1 = new User("user1", "pass1");
        User user2 = new User("user2", "pass2");

        usersRef.document(user1.getUsername()).set(user1);
        usersRef.document(user2.getUsername()).set(user2);

        // Send a follow request
        Thread.sleep(1000);
        UserManager userManager = new UserManager(ApplicationProvider.getApplicationContext());
        userManager.sendFollowRequest("user1", "user2");
        Thread.sleep(1000);


        // Retrieves user2 from the database and ensures that it receieved a follow request from user1
        userManager.fetchOtherUserData("user2")
                .addOnSuccessListener(user ->{
                    if (user != null){
                        this.currentUser = user;
                        // Checks that the user2 has a follow request from user 1
                        Assert.assertEquals("There should be a follow request from 'user1'.", "user1" ,currentUser.getFollowRequests().get(0));

                    }
                    else{
                        Log.e("Follow Tests", "Failed to retrieve otherUser's contents");
                    }
                }).addOnFailureListener(e -> {
                    Log.e("Follow Tests", "fetchOtherUserData error: " + e.toString());
                });

        // Reset the intent
        Intents.release();

    }

    /**
     * User 1 accepts a follow request from user2 and ensures that that change was reflected in the database for both user1 and user2.
     * @throws InterruptedException For Thread.sleep()
     */
    @Test
    public void acceptFollowRequestDatabaseTest() throws InterruptedException {

        // Set up the data

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.usersRef = db.collection("Users");

        User user1 = new User("user1", "pass1");
        User user2 = new User("user2", "pass2");

        // Adds a follow request from user2 to user1
        user1.followRequests.add("user2");

        // Seeds the database
        usersRef.document(user1.getUsername()).set(user1);
        usersRef.document(user2.getUsername()).set(user2);

        // Send a follow request
        Thread.sleep(1000);
        UserManager userManager = new UserManager(ApplicationProvider.getApplicationContext());
        userManager.acceptFollowRequest("user1", "user2");
        Thread.sleep(1000);


        // Retrieves user2 from the database and ensures that they now follow user 1
        userManager.fetchOtherUserData("user2")
                .addOnSuccessListener(user ->{
                    if (user != null){
                        this.currentUser = user;

                        // Checks that user 2 is following user 1
                        Assert.assertEquals("They should follow 'user1'.", "user1" ,currentUser.getFollowing().get(0));

                    }
                    else{
                        Log.e("Follow Tests", "Failed to retrieve otherUser's contents");
                    }
                }).addOnFailureListener(e -> {
                    Log.e("Follow Tests", "fetchOtherUserData error: " + e.toString());
                });

        // Retrieves user1 from the database and ensures that they have user2 as a follower
        userManager.fetchOtherUserData("user1")
                .addOnSuccessListener(user ->{
                    if (user != null){
                        this.currentUser = user;

                        // Checks that the user2 has a follow request from user 1
                        Assert.assertEquals("They should have user2 as a folloer.", "user2" ,currentUser.getFollowers().get(0));

                    }
                    else{
                        Log.e("Follow Tests", "Failed to retrieve otherUser's contents");
                    }
                }).addOnFailureListener(e -> {
                    Log.e("Follow Tests", "fetchOtherUserData error: " + e.toString());
                });

        // Reset the intent
        Intents.release();

    }

    /**
     * User 1 unfollows user 2. Ensures that the correct changes are made to the database
     * That is that user1 now has an empty following list, and user2 now has an empty follower list
     * @throws InterruptedException for Thread.sleep()
     */
    @Test
    public void unfollowUserDatabaseTest() throws InterruptedException {

        // Set up the data

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.usersRef = db.collection("Users");

        User user1 = new User("user1", "pass1");
        User user2 = new User("user2", "pass2");

        // Adds a follow request from user2 to user1
        user1.getFollowing().add("user2");

        // Seeds the database
        usersRef.document(user1.getUsername()).set(user1);
        usersRef.document(user2.getUsername()).set(user2);

        // Send a follow request
        Thread.sleep(1000);
        UserManager userManager = new UserManager(ApplicationProvider.getApplicationContext());
        userManager.unfollowUser("user1", "user2");
        Thread.sleep(1000);

        // Retrieves user2 from the database and ensures that they do not have user 1 as a follower
        userManager.fetchOtherUserData("user2")
                .addOnSuccessListener(user ->{
                    if (user != null){
                        this.currentUser = user;

                        // Checks that user 2 is following user 1
                        Assert.assertTrue("They should have an empty follower list", currentUser.getFollowers().isEmpty());

                    }
                    else{
                        Log.e("Follow Tests", "Failed to retrieve otherUser's contents");
                    }
                }).addOnFailureListener(e -> {
                    Log.e("Follow Tests", "fetchOtherUserData error: " + e.toString());
                });

        // Retrieves user1 from the database and ensures that they have an empty following list
        userManager.fetchOtherUserData("user2")
                .addOnSuccessListener(user ->{
                    if (user != null){
                        this.currentUser = user;

                        // Checks that user 1 has an empty following list
                        Assert.assertTrue("They should have an empty following list", currentUser.getFollowing().isEmpty());

                    }
                    else{
                        Log.e("Follow Tests", "Failed to retrieve otherUser's contents");
                    }
                }).addOnFailureListener(e -> {
                    Log.e("Follow Tests", "fetchOtherUserData error: " + e.toString());
                });

        // Reset the intent
        Intents.release();

    }

    /**
     * User 1 rejects user2's follow request. Ensures that the correct changes are made to the database
     * That is that user1 now has an empty followRequest list, and user2 keeps and empty following list
     * @throws InterruptedException for Thread.sleep()
     */
    @Test
    public void rejectFollowRequestDatabaseTest() throws InterruptedException {

        // Set up the data

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.usersRef = db.collection("Users");

        User user1 = new User("user1", "pass1");
        User user2 = new User("user2", "pass2");

        // Adds a follow request from user2 to user1
        user1.followRequests.add("user2");

        // Seeds the database
        usersRef.document(user1.getUsername()).set(user1);
        usersRef.document(user2.getUsername()).set(user2);

        // Send a follow request
        Thread.sleep(1000);
        UserManager userManager = new UserManager(ApplicationProvider.getApplicationContext());
        userManager.rejectFollowRequest("user1", "user2");
        Thread.sleep(1000);

        // Retrieves user2 from the database and ensures that they have an empty following lust
        userManager.fetchOtherUserData("user2")
                .addOnSuccessListener(user ->{
                    if (user != null){
                        this.currentUser = user;

                        // Checks that user2 has an empty following list
                        Assert.assertTrue("They should have an empty following list", currentUser.getFollowing().isEmpty());

                    }
                    else{
                        Log.e("Follow Tests", "Failed to retrieve otherUser's contents");
                    }
                }).addOnFailureListener(e -> {
                    Log.e("Follow Tests", "fetchOtherUserData error: " + e.toString());
                });

        // Retrieves user1 from the database and ensures that they have an empty followRequest lust
        userManager.fetchOtherUserData("user2")
                .addOnSuccessListener(user ->{
                    if (user != null){
                        this.currentUser = user;

                        // Checks that user1 has an empty followRequest list
                        Assert.assertTrue("They should have an empty followRequest list", currentUser.getFollowRequests().isEmpty());

                    }
                    else{
                        Log.e("Follow Tests", "Failed to retrieve otherUser's contents");
                    }
                }).addOnFailureListener(e -> {
                    Log.e("Follow Tests", "fetchOtherUserData error: " + e.toString());
                });

        // Reset the intent
        Intents.release();
    }

    /**
     * User 1 rejects user2's follow request. Ensures that the correct changes are made to the database
     * That is that user1 now has an empty followRequest list, and user2 keeps and empty following list
     * @throws InterruptedException for Thread.sleep()
     */
    @Test
    public void cancelFollowRequestDatabaseTest() throws InterruptedException {

        // Set up the database and the data
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.usersRef = db.collection("Users");

        User user1 = new User("user1", "pass1");
        User user2 = new User("user2", "pass2");

        // Adds a follow request from user2 to user1
        user1.followRequests.add("user2");

        // Seeds the database
        usersRef.document(user1.getUsername()).set(user1);
        usersRef.document(user2.getUsername()).set(user2);

        // Send a follow request
        Thread.sleep(1000);
        UserManager userManager = new UserManager(ApplicationProvider.getApplicationContext());
        userManager.cancelFollowRequest("user2", "user1");
        Thread.sleep(1000);

        // Retrieves user1 from the database and ensures that they have an empty followRequest List
        userManager.fetchOtherUserData("user1")
                .addOnSuccessListener(user ->{
                    if (user != null){
                        this.currentUser = user;

                        // Checks that user1 has an empty followRequest list
                        Assert.assertTrue("They should have an empty followRequest list", currentUser.getFollowRequests().isEmpty());

                    }
                    else{
                        Log.e("Follow Tests", "Failed to retrieve otherUser's contents");
                    }
                }).addOnFailureListener(e -> {
                    Log.e("Follow Tests", "fetchOtherUserData error: " + e.toString());
                });

        // Reset the intent
        Intents.release();
    }




}