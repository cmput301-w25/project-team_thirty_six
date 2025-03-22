package com.example.androidproject;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Creates the view profile screen
 */
public class OtherProfileActivity extends AppCompatActivity {

    private UserManager userManager;
    private String currentUsername;
    private String otherUsername;
    private Database database;
    private User otherUser;

    /**
     * Runs the
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_profile);

        // Get the instance of the database that we have
        this.database = Database.getInstance();
        this.userManager = new UserManager(this);

        // Retrieve the otherUser from the Intent
        otherUsername = (String) getIntent().getStringExtra("otherUser");
        currentUsername = (String) getIntent().getStringExtra("currentUser");

        // Retrieve the otherUser object from the database and store it into otherUser
        userManager.fetchOtherUserData(otherUsername)
                .addOnSuccessListener(user ->{
                    if (user != null){
                        this.otherUser = user;
                        // populate the profile
                        populateOtherUserProfile(otherUsername, currentUsername);

                    }
                    else{
                        Log.e("OtherProfileActivity", "Failed to retrieve otherUser's contents");
                    }
                }).addOnFailureListener(e -> {
                    Log.e("OtherProfileActivity", "fetchOtherUserData error: " + e.toString());
                });

        // Set up the Navbar, passing the current signed in user.
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.nav_bar_container, NavBarFragment.newInstance(currentUsername))
                    .commit();
        }


        if (otherUsername != null){
            Log.d("OtherProfileActivity", "Other user: " + otherUsername);
        } else {
            Log.e("OtherProfileActivity", "No otherUser data found");

        }



    }

    /**
     * Populates the user in user profile info by fetching the user and storing their contents into this activity
     *
     * @param otherUsername is the username of the other user which you wish to search up
     * @param currentUsername is the username of the current user that is logged in.
     */
    private void populateOtherUserProfile(String otherUsername, String currentUsername) {

        // populate the username slot.
        TextView userNameTextView = findViewById(R.id.displayUsername);
        userNameTextView.setText(otherUsername);

        // The following code makes the sets the follow button to either "follow", "requested" or "following"
        // depending on if the currentUser is following them, or has requested them or not.
        Button followButton = findViewById(R.id.follow_following_button);
        // TODO add an unfollow option for once they unfollow someone.
        // TODO would probably have to get the current user object into this activity as well.
        if (otherUser.followRequests.contains(currentUsername)){
            followButton.setText(R.string.follow_button_requested);
        } else {
            followButton.setText(R.string.follow_button_follow);
        }
    }

    /**
     * onClick method for the follow button.
     * Calls the userManager class's sendFollowRequest function. Sends the other user a follow request
     * @param view the view of the activity
     */
    public void followOnClick(View view) {
        Log.d("temp", "temp");;
        Button followButton = findViewById(R.id.follow_following_button);
        String followButtonText = followButton.getText().toString();

        if (followButtonText.equals(getString(R.string.follow_button_follow))){
            userManager.sendFollowRequest(currentUsername, otherUsername);
            followButton.setText(R.string.follow_button_requested);

        } else if (followButtonText.equals(getString(R.string.follow_button_requested))){
            followButton.setText(R.string.follow_button_follow);
        }
    }
}