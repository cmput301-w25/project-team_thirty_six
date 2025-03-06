package com.example.androidproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {

    private UserManager userManager;  // This is so that the signUpOnClick method can access the userManager function without having to instantiate it each time

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.userManager = new UserManager(this); // Setting this class' user manager to a userManager and giving it the activity context
    }

    /**
     * Handles the sign-up button click event.
     * Starts the SignUpActivity.
     *
     * @param v
     *      The view that was clicked.
     */
    public void signUpPage(View v) {
        Intent i = new Intent(this, SignUpActivity.class);
        startActivity(i);
    }

    /**
     * Handles the onClick event of the login button.
     * Obtains the username and password entered and then calls the userManager.loginUser method
     * to see if the username and passwords match.
     * @param view
     */
    public void loginOnClick(View view) {
        EditText editUsername = findViewById(R.id.username);
        EditText editPassword = findViewById(R.id.password);
        String username = editUsername.getText().toString();
        String password = editPassword.getText().toString();
        userManager.loginUser(username, password);
    }
}