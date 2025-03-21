package com.example.androidproject;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
            populateCurrentUserProfile(currentUsername);

        } else {
            Log.e("ProfileActivity", "No currentUser data found");
        }


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.nav_bar_container, NavBarFragment.newInstance(currentUsername))
                    .commit();
        }

        userManager = new UserManager(this);


    }

    /**
     * Populates the user in user profile info
     * @param currentUsername
     */
    private void populateCurrentUserProfile(String currentUsername) {
        TextView userNameTextView = findViewById(R.id.displayUsername);
        userNameTextView.setText(currentUsername);
    }



}