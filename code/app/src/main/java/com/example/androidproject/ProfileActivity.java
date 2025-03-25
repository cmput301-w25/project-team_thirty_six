package com.example.androidproject;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.androidproject.databinding.ActivityProfileBinding;

/**
 * Creates the view profile screen
 */
public class ProfileActivity extends AppCompatActivity {

    private UserManager userManager;
    private String currentUsername;
    private String otherUsername;
    private User currentUser;

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
        setContentView(R.layout.activity_profile);

        // Retrieve the currentUser from the Intent
        currentUsername = (String) getIntent().getStringExtra("currentUser");

        // If it is looking at your own profile
        if (currentUsername != null) {
            Log.d("ProfileActivity", "Current user: " + currentUsername);

        } else {
            Log.e("ProfileActivity", "No currentUser data found");
        }

        userManager = new UserManager(this);

        // Retrieve the otherUser object from the database and store it into otherUser
        //userManager.fetchOtherUserData(currentUsername)
        //        .addOnSuccessListener(user ->{
        //            if (user != null){
        //                this.currentUser = user;
        //                // populate the profile
        //                populateCurrentUserProfile(currentUsername);
        //
        //            }
        //            else{
        //                Log.e("CurrentProfileActivity", "Failed to retrieve currentUser's contents");
        //            }
        //        }).addOnFailureListener(e -> {
        //            Log.e("CurrentProfileActivity", "fetchCurrentUserData error: " + e.toString());
        //        });

        this.currentUser = UserManager.getCurrentUser();
        populateCurrentUserProfile(currentUsername);


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.nav_bar_container, NavBarFragment.newInstance(currentUsername))
                    .commit();
        }


    }

    /**
     * Populates the user in user profile info
     * @param currentUsername
     */
    private void populateCurrentUserProfile(String currentUsername) {
        TextView userNameTextView = findViewById(R.id.displayUsername);
        userNameTextView.setText(currentUsername);

        // Setup follower and following strings
        int followingCount = currentUser.getFollowing().size();
        int followerCount = currentUser.getFollowers().size();
        String followingString = String.format("%d following", followingCount);
        String followersString = String.format("%d followers", followerCount);

        // Populate the follower and following strings
        TextView followersTextView = findViewById(R.id.followerAmountTextView);
        TextView followingTextView = findViewById(R.id.followingAmountTextView);
        followersTextView.setText(followersString);
        followingTextView.setText(followingString);
    }

    public void updateFollowerCount(String currentUsername){
        int followerCount = currentUser.getFollowers().size();
        String followersString = String.format("%d followers", followerCount);

    }

    public void followRequestsOnClick(View view) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("currentUser", currentUser);

        Fragment listFragment = new FollowRequestFragment();
        listFragment.setArguments(bundle);

        // The folowing code was obtained from ChatGPT on March 22, 2025.
        // Taken by Rhiyon Naderi
        // Query: how to open a list view fragment in an activities function

        // Get the FragmentManager and start a transaction.
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // Replace the container with the new fragment.
        transaction.replace(R.id.fragment_container, listFragment);
        // Optionally add to back stack if you want to allow the user to navigate back.
        transaction.addToBackStack(null);
        // Commit the transaction.
        transaction.commit();
        // End of taken code. 

    }
}