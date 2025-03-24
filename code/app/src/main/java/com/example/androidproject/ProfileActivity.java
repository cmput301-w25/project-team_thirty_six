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
        userManager.fetchOtherUserData(currentUsername)
                .addOnSuccessListener(user ->{
                    if (user != null){
                        this.currentUser = user;
                        // populate the profile
                        populateCurrentUserProfile(currentUsername);

                    }
                    else{
                        Log.e("CurrentProfileActivity", "Failed to retrieve currentUser's contents");
                    }
                }).addOnFailureListener(e -> {
                    Log.e("CurrentProfileActivity", "fetchCurrentUserData error: " + e.toString());
                });




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
    }

    public void followRequestsOnClick(View view) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("currentUser", currentUser);

        Fragment listFragment = new FollowRequestFragment();
        listFragment.setArguments(bundle);
        // Get the FragmentManager and start a transaction.
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // Replace the container with the new fragment.
        transaction.replace(R.id.fragment_container, listFragment);
        // Optionally add to back stack if you want to allow the user to navigate back.
        transaction.addToBackStack(null);
        // Commit the transaction.
        transaction.commit();
        Log.d("Profile Activity", "Reached");
    }
    /**
     * Takes you to a page to view mood history
     * @param view
     */
    public void viewMoodHistory(View view){
        Intent i = new Intent(this, MoodHistoryActivity.class);
        i.putExtra("currentUser",
                (String) getIntent().getSerializableExtra("currentUser"));
        startActivity(i);
    }
}