package com.example.androidproject;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Activity for signing up
 */
public class SignUpActivity extends AppCompatActivity {
    private UserManager userManager; // This is so that the signUpOnClick method can access the userManager function without having to instantiate it each time

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        this.userManager = new UserManager(this); // Setting this class' user manager to a userManager and giving it the activity context
    }
    /**
     * Handles the login button click event.
     * Starts the LoginActivity.
     *
     * @param v
     *      The view that was clicked.
     */
    public void loginPage(View v){
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

    /**
     * The onClick method of the sign up button
     * Creates a new user and adds them to the database
     * @param view the view that was clicked
     */
    public void signUpOnClick(View view) {
        EditText editUsername = findViewById(R.id.username);
        EditText editPassword = findViewById(R.id.password);
        String username = editUsername.getText().toString();
        String password = editPassword.getText().toString();
        userManager.addUser(username, password,  new UserManager.SignUpCallback(){
            @Override
            public void onSignUpSuccess() {
                Intent i = new Intent(SignUpActivity.this, HomePageActivity.class);
                startActivity(i);

            }
        });

    }
}