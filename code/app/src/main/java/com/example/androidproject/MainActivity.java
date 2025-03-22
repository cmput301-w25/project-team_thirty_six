package com.example.androidproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.androidproject.LocationPermissionFragment;
import com.example.androidproject.LoginActivity;
import com.example.androidproject.SignUpActivity;

public class MainActivity extends AppCompatActivity {
    private LocationPermissionFragment locationPermissionFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.splash_screen);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void loginPage(View v) {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

    public void signUpPage(View v) {
        Intent i = new Intent(this, SignUpActivity.class);
        startActivity(i);
    }
}