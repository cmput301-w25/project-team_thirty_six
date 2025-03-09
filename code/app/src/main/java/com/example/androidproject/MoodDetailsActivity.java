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
 * Retrieves mood details from Firestore and update UI
 */
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

    /**
     * Initializes the activity, sets up UI components, and loads mood details.
     * @param savedInstanceState
     */
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
            loadMoodDetails();
        } else {
            Toast.makeText(this, "Mood details not available", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Initializes all of the UI components
     */
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

    /**
     * Loads mood details from Firestore using the mood document ID.
     * Retrieves the document and calls {@link #updateUIWithMoodData(DocumentSnapshot)}
     * if the document exists.
     */
    private void loadMoodDetails() {
        DocumentReference moodRef = db.collection("Moods").document(moodId);
        moodRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Extract the username from the mood document
                    String username = document.getString("user");
                    tvUsername.setText(username != null && !username.isEmpty() ? username : "User");

                    updateUIWithMoodData(document);
                } else {
                    Toast.makeText(this, "Mood not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(this, "Error loading mood details", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Updates the UI with mood data from the Firestore document.
     * Populate UI elements with mood details
     *
     * @param document The DocumentSnapshot containing the mood data
     */
    private void updateUIWithMoodData(DocumentSnapshot document) {
        try {
            // Extract mood details
            String moodName = document.getString("mood");
            String moodColor = document.getString("color");
            String reason = document.getString("reason");
            String situation = document.getString("situation");
            String imageUrl = document.getString("id");

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
                    }).addOnFailureListener(e -> {
                        ivMoodImage.setVisibility(View.GONE);
                    });
                } catch (Exception e) {
                    ivMoodImage.setVisibility(View.GONE);
                }
            } else {
                ivMoodImage.setVisibility(View.GONE);
            }

            // Format timestamp based on what's available
            handleTimestamp(document);

        } catch (Exception e) {
            Toast.makeText(this, "Error displaying mood details", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles timestamp extraction and formatting from the mood document.
     * @param document The DocumentSnapshot containing potential timestamp information
     */
    private void handleTimestamp(DocumentSnapshot document) {
        // Check for dayTime as map
        Object dayTimeObj = document.get("dayTime");
        if (dayTimeObj instanceof Map) {
            Map<String, Object> dayTimeMap = (Map<String, Object>) dayTimeObj;
            formatTimestampFromMap(dayTimeMap);
            return;
        }

        // Check for dayTime as string
        String dayTimeStr = document.getString("dayTime");
        if (dayTimeStr != null) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(dayTimeStr);
                formatTimestampFromLocalDateTime(dateTime);
                return;
            } catch (Exception e) {
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

    /**
     * Formats timestamp from a map representation of date and time.
     *
     * @param dayTimeMap A map containing date and time components
     */
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
            tvTimestamp.setText("Unknown time");
        }
    }

    /**
     * Formats timestamp from a LocalDateTime object.
     *
     * @param dateTime The LocalDateTime to be formatted
     */
    private void formatTimestampFromLocalDateTime(LocalDateTime dateTime) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a", Locale.getDefault());
            tvTimestamp.setText(dateTime.format(formatter));
        } catch (Exception e) {
            tvTimestamp.setText("Unknown time");
        }
    }

    /**
     * Sets the mood icon based on the mood name.
     *
     * @param moodName The name of the mood to set an icon for
     */
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