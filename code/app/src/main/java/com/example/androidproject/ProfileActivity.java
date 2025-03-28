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
 * Creates the view profile screen, contains links to the followRequestFragment, FollowingListFragment and FollowerListFragment
 * Requires the currentUsername of the logged in user to be given in the intent.
 * Displays the following and follower caounts as well.
 */
public class ProfileActivity extends AppCompatActivity {

    private UserManager userManager;
    private String currentUsername;
    private String otherUsername;
    private User currentUser;

    /**
     * onCreate method for the ProfileActivity
     * Sets up the userManager and gets the currentUsername from the intent.
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
     * Populates the user in user profile info into the activity_profile.xml file.
     * @param currentUsername the username of the logged in user.
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

    /**
     * The onClick method for the followRequest sign.
     * Opens the Follow Request Fragment and passes currentUser into the bundle.
     * @param view
     */
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

    /**
     * The on click method for the following count textview.
     * opens the FollowingListFragment which displays the user's following
     * @param view The view that this was called in (Activity_profile)
     */
    public void followingOnClick(View view) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("currentUser", currentUser);

        Fragment listFragment = new FollowingListFragment();
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
        Log.d("ProfileActivity", "Reached");
        // End of taken code.

    }

    /**
     * The on click method for the follower count textview.
     * It opens the FollowListFragment which displays the user's followers
     * @param view The view that this was called in (Activity_profile)
     */
    public void followerOnClick(View view) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("currentUser", currentUser);

        Fragment listFragment = new FollowerListFragment();
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
        Log.d("ProfileActivity", "Reached");
        // End of taken code.

    }

    /**
     * Takes you to a page to view mood history
     * @param view The current view
     */
    public void viewMoodHistory(View view){
        Intent i = new Intent(this, MoodHistoryActivity.class);
        i.putExtra("currentUser",
                (String) getIntent().getSerializableExtra("currentUser"));
        startActivity(i);
    }
}