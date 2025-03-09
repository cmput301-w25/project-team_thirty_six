package com.example.androidproject;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.DateFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

/**
 * Activity to display details of a selected mood post.
 * Retrieves mood details from Firestore and updates the UI accordingly.
 */
public class MoodDetailsActivity extends AppCompatActivity {

    private static final String TAG = "MoodDetailsActivity";

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

        // Initialize Firebase and UI components
        db = FirebaseFirestore.getInstance();
        initializeViews();

        // Retrieve mood ID and user ID from intent
        if (getIntent().hasExtra("id") && getIntent().hasExtra("user")) {
            moodId = getIntent().getStringExtra("id");
            userId = getIntent().getStringExtra("user");
            Log.d(TAG, "Loading mood: ID=" + moodId + ", user=" + userId);
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

        // UI elements
        tvTitle = findViewById(R.id.textView_mood_details);
        tvUsername = findViewById(R.id.textView_mood_details_user);
        ivMoodIcon = findViewById(R.id.imageView_mood_details);
        tvMoodName = findViewById(R.id.textView_mood_details_mood_name);
        tvReason = findViewById(R.id.textView_mood_details_reason);
        ivMoodImage = findViewById(R.id.mood_image);
        tvSocialSituation = findViewById(R.id.textView_mood_details_social_situation);
        tvTimestamp = findViewById(R.id.mood_timestamp);

        // Hide optional fields initially
        tvReason.setVisibility(View.GONE);
        tvSocialSituation.setVisibility(View.GONE);
        ivMoodImage.setVisibility(View.GONE);

    }

    private void loadMoodDetails() {
        DocumentReference moodRef = db.collection("Moods").document(moodId);
        moodRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d(TAG, "Document found: " + document.getId());

                    // Extract the username from the mood document
                    String username = document.getString("user");
                    tvUsername.setText(username != null && !username.isEmpty() ? username : "User");

                    updateUIWithMoodData(document);
                } else {
                    Log.e(TAG, "Document not found with ID: " + moodId);
                    Toast.makeText(this, "Mood not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Log.e(TAG, "Error loading mood: " +
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                Toast.makeText(this, "Error loading mood: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateUIWithMoodData(DocumentSnapshot document) {
        try {
            // Extract mood details
            String moodName = document.getString("mood");
            String moodColor = document.getString("color");
            String reason = document.getString("reason");
            String situation = document.getString("situation");
            String imageUrl = document.getString("id");


            Log.d(TAG, "Mood name: " + moodName + ", Color: " + moodColor);

            // Set mood name and icon
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
                    Log.e(TAG, "Error parsing color: " + moodColor, e);
                    tvMoodName.setTextColor(Color.WHITE);
                }
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

            // Load image if available
            if (imageUrl != null && !imageUrl.isEmpty()) {
                try {
                    // Initialize Firebase Storage
                    FirebaseStorage storage = FirebaseStorage.getInstance();

                    // Create a reference to the image file
                    StorageReference imageRef = storage.getReference().child("images/" + imageUrl);
                    // Get the download URL and load it
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        ivMoodImage.setVisibility(View.VISIBLE);
                        Picasso.get()
                                .load(uri)
                                .placeholder(R.drawable.error_placeholder_image)
                                .error(R.drawable.error_placeholder_image)
                                .into(ivMoodImage);
                        Log.d(TAG, "Successfully loaded image: " + uri);
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get download URL: " + imageUrl, e);
                        ivMoodImage.setVisibility(View.GONE);
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error loading image from Storage: " + e.getMessage());
                    ivMoodImage.setVisibility(View.GONE);
                }
            } else {
                ivMoodImage.setVisibility(View.GONE);
            }

            // Format timestamp based on what's available
            handleTimestamp(document);

        } catch (Exception e) {
            Log.e(TAG, "Error updating UI with mood data", e);
            Toast.makeText(this, "Error displaying mood details", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleTimestamp(DocumentSnapshot document) {
        // Try multiple approaches to get the timestamp

        // Check for dayTime as map
        Object dayTimeObj = document.get("dayTime");
        if (dayTimeObj instanceof Map) {
            Map<String, Object> dayTimeMap = (Map<String, Object>) dayTimeObj;
            formatTimestampFromMap(dayTimeMap);
            return;
        }

        // Check for dayTime as string (ISO format)
        String dayTimeStr = document.getString("dayTime");
        if (dayTimeStr != null) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(dayTimeStr);
                formatTimestampFromLocalDateTime(dateTime);
                return;
            } catch (Exception e) {
                Log.e(TAG, "Error parsing dayTime string: " + dayTimeStr, e);
            }
        }

        // Check for timestamp field
        Long timestamp = document.getLong("timestamp");
        if (timestamp != null) {
            String formattedDate = new java.text.SimpleDateFormat(
                    "MMMM d, yyyy 'at' h:mm a", Locale.getDefault())
                    .format(new java.util.Date(timestamp));
            tvTimestamp.setText(formattedDate);
            return;
        }

        // Fallback - no timestamp available
        tvTimestamp.setText("Unknown time");
    }

    private void formatTimestampFromMap(Map<String, Object> dayTimeMap) {
        try {
            int year = ((Long) dayTimeMap.get("year")).intValue();
            int monthValue = ((Long) dayTimeMap.get("monthValue")).intValue();
            int day = ((Long) dayTimeMap.get("dayOfMonth")).intValue();
            int hour = ((Long) dayTimeMap.get("hour")).intValue();
            int minute = ((Long) dayTimeMap.get("minute")).intValue();

            // Convert month number to full name
            String monthName = new DateFormatSymbols().getMonths()[monthValue - 1];

            // Format time in 12-hour format
            String amPm = (hour < 12) ? "AM" : "PM";
            int displayHour = (hour == 0 || hour == 12) ? 12 : hour % 12;

            String formattedTime = String.format(Locale.getDefault(),
                    "%s %d, %d at %d:%02d %s", monthName, day, year, displayHour, minute, amPm);

            tvTimestamp.setText(formattedTime);
        } catch (Exception e) {
            Log.e(TAG, "Error formatting timestamp from map", e);
            tvTimestamp.setText("Unknown time");
        }
    }

    private void formatTimestampFromLocalDateTime(LocalDateTime dateTime) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a", Locale.getDefault());
            tvTimestamp.setText(dateTime.format(formatter));
        } catch (Exception e) {
            Log.e(TAG, "Error formatting LocalDateTime", e);
            tvTimestamp.setText("Unknown time");
        }
    }

    private void setMoodIcon(String moodName) {
        int resourceId;

        switch (moodName.toLowerCase()) {
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