package com.example.androidproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
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
}