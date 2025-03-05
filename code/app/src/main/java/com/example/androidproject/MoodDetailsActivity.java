package com.example.androidproject;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.Locale;
import java.util.Map;

public class MoodDetailsActivity extends AppCompatActivity {

    // UI elements
    private TextView tvTitle;
    private TextView tvUsername;
    private ImageView ivMoodIcon;
    private TextView tvMoodName;
    private TextView tvReason;
    private ImageView ivMoodImage;
    private TextView tvSocialSituation;
    private TextView tvTimestamp;

    // Firebase instance
    private FirebaseFirestore db;

    // Data values
    private String moodId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_details);

        // Initialize Firebase and views
        db = FirebaseFirestore.getInstance();
        initializeViews();

        // Get mood ID from intent
        if (getIntent().hasExtra("id") && getIntent().hasExtra("user")) {
            moodId = getIntent().getStringExtra("id");
            userId = getIntent().getStringExtra("user");
            loadMoodDetails();
        } else {
            Toast.makeText(this, "Error: Mood details not available", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        // Back button
        ImageButton btnBack = findViewById(R.id.button_back);
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // Initialize text & image views
        tvTitle = findViewById(R.id.textView_mood_details);
        tvUsername = findViewById(R.id.textView_mood_details_user);
        ivMoodIcon = findViewById(R.id.imageView_mood_details);
        tvMoodName = findViewById(R.id.textView_mood_details_mood_name);
        tvReason = findViewById(R.id.textView_mood_details_reason);
        ivMoodImage = findViewById(R.id.mood_image);
        tvSocialSituation = findViewById(R.id.textView_mood_details_social_situation);
        tvTimestamp = findViewById(R.id.mood_timestamp);

        // Hide the optional fields
        tvReason.setVisibility(View.GONE);
        tvSocialSituation.setVisibility(View.GONE);
        ivMoodImage.setVisibility(View.GONE);
    }

    private void loadMoodDetails() {
        // Reference to the mood document
        DocumentReference moodRef = db.collection("users").document(userId)
                .collection("moods").document(moodId);

        moodRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Extract the username directly from the mood document
                    String username = document.getString("id"); // "id" contains the username
                    if (username != null && !username.isEmpty()) {
                        tvUsername.setText(username);
                    } else {
                        tvUsername.setText("User"); // Default value if username is missing
                    }

                    updateUIWithMoodData(document);
                } else {
                    Toast.makeText(this, "Mood not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(this, "Error loading mood: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateUIWithMoodData(DocumentSnapshot document) {
        // Extract mood details
        String moodName = document.getString("mood");
        String moodColor = document.getString("color");
        Long emojiResId = document.getLong("emoji");
        String reason = document.getString("reason");
        String situation = document.getString("situation");
        String imageUrl = document.getString("image");
        Map<String, Object> timeMap = (Map<String, Object>) document.get("time");

        // Get username
        getUsername(userId);

        // Set mood name
        if (moodName != null) {
            tvMoodName.setText(moodName);
            setMoodIcon(moodName);
        }

        // Set mood color
        if (moodColor != null && moodColor.length() == 6) {
            try {
                int color = Color.parseColor("#" + moodColor);
                tvMoodName.setTextColor(color);
            } catch (IllegalArgumentException e) {
                tvMoodName.setTextColor(Color.WHITE);
            }
        }

        // Set emoji icon
        if (emojiResId != null) {
            ivMoodIcon.setImageResource(emojiResId.intValue());
        }

        // Show reason if available
        if (reason != null && !reason.isEmpty()) {
            tvReason.setVisibility(View.VISIBLE);
            tvReason.setText(reason);
        }

        // Show social situation if available
        if (situation != null && !situation.isEmpty()) {
            tvSocialSituation.setVisibility(View.VISIBLE);
            tvSocialSituation.setText(situation);
        }

        // Show image if available
        if (imageUrl != null && !imageUrl.isEmpty()) {
            ivMoodImage.setVisibility(View.VISIBLE);
            Picasso.get().load(imageUrl).into(ivMoodImage);
        }

        // Format timestamp
        if (timeMap != null) {
            int year = ((Long) timeMap.get("year")).intValue();
            int monthValue = ((Long) timeMap.get("monthValue")).intValue();
            int day = ((Long) timeMap.get("dayOfMonth")).intValue();
            int hour = ((Long) timeMap.get("hour")).intValue();
            int minute = ((Long) timeMap.get("minute")).intValue();

            // Convert month number to full month name
            String monthName = new java.text.DateFormatSymbols().getMonths()[monthValue - 1];

            // Format time in 12-hour format with AM/PM
            String amPm = (hour < 12) ? "AM" : "PM";
            int displayHour = (hour == 0 || hour == 12) ? 12 : hour % 12;

            String formattedTime = String.format(Locale.getDefault(),
                    "%s %d, %d at %d:%02d %s", monthName, day, year, displayHour, minute, amPm);

            tvTimestamp.setText(formattedTime);
        }
    }

    private void getUsername(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        tvUsername.setText(username != null ? username : "User");
                    }
                })
                .addOnFailureListener(e -> tvUsername.setText("User"));
    }

    private void setMoodIcon(String moodName) {
        int resourceId;

        switch (moodName.toLowerCase()) { // Convert to lowercase to avoid case mismatches
            case "anger":
                resourceId = R.drawable.anger;
                break;
            case "confusion":
                resourceId = R.drawable.confusion;
                break;
            case "disgust":
                resourceId = R.drawable.disgust;
                break;
            case "fear":
                resourceId = R.drawable.fear;
                break;
            case "happiness":
                resourceId = R.drawable.happiness;
                break;
            case "sadness":
                resourceId = R.drawable.sadness;
                break;
            case "shame":
                resourceId = R.drawable.shame;
                break;
            case "surprise":
                resourceId = R.drawable.surprise;
                break;
            default:
                resourceId = R.drawable.error_placeholder_image;
                break;
        }

        ivMoodIcon.setImageResource(resourceId);
    }

}
