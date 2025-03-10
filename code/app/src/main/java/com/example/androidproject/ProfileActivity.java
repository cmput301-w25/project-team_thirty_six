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

public class ProfileActivity extends AppCompatActivity {

    private UserManager userManager;
    private String currentUsername;

    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Retrieve the currentUser from the Intent
        currentUsername = (String) getIntent().getStringExtra("currentUser");

        if (currentUsername != null) {
            Log.d("ProfileActivity", "Current user: " + currentUsername);
        } else {
            Log.e("ProfileActivity", "No user data found");
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.nav_bar_container, NavBarFragment.newInstance(currentUsername))
                    .commit();
        }

        populateProfile(currentUsername);
        userManager = new UserManager(this);

    }

    private void populateProfile(String currentUsername) {
        TextView userNameTextView = findViewById(R.id.displayUsername);
        userNameTextView.setText(currentUsername);
    }

}