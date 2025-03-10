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

import java.text.DateFormatSymbols;
import java.util.Locale;
import java.util.Map;

/**
 * Activity to display details of a selected mood post.
 * Retrieves mood details from Firestore and updates the UI accordingly.
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
     * Called when the activity is first created.
     * Initializes Firebase, UI elements, and fetches mood details.
     *
     * @param savedInstanceState Saved state data if activity is recreated.
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
            Toast.makeText(this, "Error: Mood details not available", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Initializes UI elements and sets up the back button functionality.
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
     * Fetches mood details from Firestore based on mood ID and user ID.
     */
    private void loadMoodDetails() {
        DocumentReference moodRef = db.collection("users").document(userId)
                .collection("moods").document(moodId);

        moodRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Extract the username from the mood document
                    String username = document.getString("id");
                    tvUsername.setText(username != null && !username.isEmpty() ? username : "User");

                    // First load the main mood data
                    updateUIWithMoodData(document);

                    // Then fetch the dayTime subcollection for timestamp formatting
                    moodRef.collection("dayTime").document("time_data").get()
                            .addOnSuccessListener(timeDoc -> {
                                if (timeDoc.exists()) {
                                    formatAndDisplayTimestamp(timeDoc);
                                }
                            })
                            .addOnFailureListener(e -> {
                                // If we can't get the dayTime data, use the timestamp from the main document
                                Long timestamp = document.getLong("timestamp");
                                if (timestamp != null) {
                                    displayTimestampFromMillis(timestamp);
                                }
                            });
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

    /**
     * Updates the UI with mood data retrieved from Firestore.
     *
     * @param document The Firestore document containing mood data.
     */
    private void updateUIWithMoodData(DocumentSnapshot document) {
        // Extract mood details
        String moodName = document.getString("mood");
        String moodColor = document.getString("color");
        Long emojiResId = document.getLong("emoji");
        String reason = document.getString("reason");
        String situation = document.getString("situation");
        String imageUrl = document.getString("image");

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

        // Load image if available
        if (imageUrl != null && !imageUrl.isEmpty()) {
            ivMoodImage.setVisibility(View.VISIBLE);
            Picasso.get().load(imageUrl).into(ivMoodImage);
        }
    }

    /**
     * Formats and displays timestamp from dayTime document data
     *
     * @param timeDoc The Firestore document containing dayTime data
     */
    private void formatAndDisplayTimestamp(DocumentSnapshot timeDoc) {
        Long year = timeDoc.getLong("year");
        Long monthValue = timeDoc.getLong("monthValue");
        Long day = timeDoc.getLong("dayOfMonth");
        Long hour = timeDoc.getLong("hour");
        Long minute = timeDoc.getLong("minute");

        if (year != null && monthValue != null && day != null && hour != null && minute != null) {
            // Convert month number to full name
            String monthName = new DateFormatSymbols().getMonths()[monthValue.intValue() - 1];

            // Format time in 12-hour format
            String amPm = (hour < 12) ? "AM" : "PM";
            int displayHour = (hour.intValue() == 0 || hour.intValue() == 12) ? 12 : hour.intValue() % 12;

            String formattedTime = String.format(Locale.getDefault(),
                    "%s %d, %d at %d:%02d %s", monthName, day, year, displayHour, minute, amPm);

            tvTimestamp.setText(formattedTime);
        }
    }

    /**
     * Displays timestamp from milliseconds as fallback
     *
     * @param timestamp The timestamp in milliseconds
     */
    private void displayTimestampFromMillis(long timestamp) {
        java.util.Date date = new java.util.Date(timestamp);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMMM d, yyyy 'at' h:mm a", Locale.getDefault());
        tvTimestamp.setText(sdf.format(date));
    }

    /**
     * Sets the appropriate mood icon based on the mood name.
     *
     * @param moodName The name of the mood.
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
