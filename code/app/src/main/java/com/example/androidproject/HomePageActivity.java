package com.example.androidproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomePageActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

    }

    public void viewMoodHistory(View view){
        Intent i = new Intent(this, MoodHistoryActivity.class);
        i.putExtra("currentUser",
                (String) getIntent().getSerializableExtra("currentUser"));
        startActivity(i);
    }

    /**
     *  Allows the user to access the add mood view
     * @param view
     */
    public void addMood(View view) {
        Intent i = new Intent(this, CreatePostActivity.class);
        i.putExtra("user", (String) getIntent().getSerializableExtra("currentUser"));
        startActivity(i);
    }
}