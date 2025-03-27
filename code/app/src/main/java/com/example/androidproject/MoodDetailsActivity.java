package com.example.androidproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.DateFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * MoodDetailsActivity displays comprehensive information about a selected mood post.
 * Retrieves mood details from Firestore and updates the UI
 *
 * This activity interacts with the following components:
 * - EditMoodActivity: Target activity for handling edit operations
 * - Database: Manages commment storage
 * - NavbarFragment: Provides navigation capabilities
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
    private TextView btnEdit;
    private TextView tvLocation;
    // Button for adding comments
    private ImageButton confirm_comment;
    private TextInputEditText comment_text;

    // Firebase instance
    private FirebaseFirestore db;

    // Data values
    private String moodId;
    private String userId;
    // Collection reference for the comments
    private CollectionReference commentCollection;
    private ArrayList<Comment> comments = new ArrayList<Comment>();
    private LinearLayout commentListView;
    // Creates a copy of the current class to toast inside a on click listener
    private MoodDetailsActivity current = this;

    private static final String TAG = "MoodDetailsActivity";

    /**
     * Called when the activity is starting. Initializes UI components and loads mood details.
     *
     * @param savedInstanceState If the activity is being reinitialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_details);

        // Retrieve the currentUser from the Intent
        String currentLoggedInUser = getIntent().getStringExtra("user");

        if (currentLoggedInUser != null) {
            userId = currentLoggedInUser;
        } else {
            Toast.makeText(this, "Error: User not found!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.nav_bar_container, NavBarFragment.newInstance(userId))
                    .commit();
        }

        // Initialize Firebase and UI components
        db = FirebaseFirestore.getInstance();
        initializeViews();

        // Sets the collection reference of comment
        commentCollection = db.collection("Comments");
        commentLoop();

        // Retrieve mood ID from intent
        if (getIntent().hasExtra("id")) {
            moodId = getIntent().getStringExtra("id");
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
        btnEdit = findViewById(R.id.button_edit);
        confirm_comment = findViewById(R.id.confirm_comment_button);
        comment_text = findViewById(R.id.comment_text);
        commentListView = findViewById(R.id.linearCommentView);
        tvLocation = findViewById(R.id.textView_mood_details_location);

        // Hide optional fields initially
        tvReason.setVisibility(View.GONE);
        tvSocialSituation.setVisibility(View.GONE);
        ivMoodImage.setVisibility(View.GONE);
        tvLocation.setVisibility(View.GONE);

        // only if the current user is the owner of the mood
        btnEdit.setVisibility(View.GONE);

        /**
         * Sets an OnClickListener for the edit button.
         * When clicked, this listener starts the EditMoodActivity,
         * passing the mood ID as an extra in the intent.
         *
         * @param v The view that was clicked.
         */
        btnEdit.setOnClickListener(v -> {
            Intent editIntent = new Intent(MoodDetailsActivity.this, EditMoodActivity.class);
            editIntent.putExtra("moodId", moodId);
            editIntent.putExtra("user", userId);
            startActivity(editIntent);
        });

        // Creates the functionality for the add comment button
        confirm_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            /**
             * When add commment is clicked post the comment to the database
             */
            public void onClick(View v) {
                if (comment_text.getText().length() == 0) {
                    // If the text is empty prevents from submitting
                    CharSequence error_text = "Cannot post empty comment.";
                    Toast.makeText(current, error_text, Toast.LENGTH_SHORT).show();
                } else {
                    // Gets the comment info
                    String commentTextData = comment_text.getText().toString();
                    // Adds the comment to the database
                    Database.getInstance().addComment(userId, moodId, commentTextData);
                    comment_text.setText("");
                }
            }
        });
    }

    /**
     * Creates the comment loop functionality
     */
    private void commentLoop() {
        // Adds a snapshot listener to the comment collection
        commentCollection.addSnapshotListener((value, error) -> {
            // Queries the database for only comments with mood id of the mood
            commentCollection.whereEqualTo("moodID", moodId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                /**
                 * On success listener for the query
                 * @param queryDocumentSnapshots
                 */
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    // Removes everything from comments
                    comments.clear();
                    commentListView.removeAllViews();
                    // Creates a list of documents
                    List<DocumentSnapshot> docList = queryDocumentSnapshots.getDocuments();
                    // Loops through the documents
                    for (int i = 0; i < docList.size(); i++) {
                        // Gets the current document
                        DocumentSnapshot currentDoc = docList.get(i);
                        // Makes a comment out of the document
                        Comment comment = new Comment(currentDoc.get("username").toString(), currentDoc.get("moodID").toString(), currentDoc.get("text").toString());
                        // Adds comment to temp list
                        comments.add(comment);
                    }
                    // Loops through all the comments
                    for (int i = 0; i < comments.size(); i++) {
                        Comment comment = comments.get(i);
                        View view = LayoutInflater.from(current).inflate(R.layout.comment_layout, null);
                        // Gets the elements of the display
                        TextView username_text = view.findViewById(R.id.comment_username_display);
                        TextView text_text = view.findViewById(R.id.comment_text_display);
                        // Changes the views
                        username_text.setText(comment.getUsername());
                        text_text.setText(comment.getText());
                        commentListView.addView(view);
                    }
                }
            });
        });
    }

    /**
     * Loads mood details from Firestore using the mood document ID.
     */
    private void loadMoodDetails() {
        DocumentReference moodRef = db.collection("Moods").document(moodId);

        moodRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Extract the username from the mood document
                    String moodOwner = document.getString("user");

                    tvUsername.setText(moodOwner != null && !moodOwner.isEmpty() ? moodOwner : "User");

                    // Only show edit button if the current user is the owner of the mood
                    if (moodOwner != null && moodOwner.equals(userId)) {
                        btnEdit.setVisibility(View.VISIBLE);
                    } else {
                        btnEdit.setVisibility(View.GONE);
                    }

                    // Call updateUIWithMoodData with the new document
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

    @Override
    /**
     * Reload mood details when activity becomes active again
     */
    protected void onResume() {
        super.onResume();
        if (moodId != null) {
            loadMoodDetails();
        }
    }

    /**
     * Updates the UI with mood data from the Firestore document.
     *
     * @param document The DocumentSnapshot containing the mood data
     */
    @SuppressLint("SetTextI18n")
    private void updateUIWithMoodData(DocumentSnapshot document) {
        try {
            // Extract mood details
            String moodName = document.getString("mood");
            String moodColor = document.getString("color");
            String reason = document.getString("reason");
            String situation = document.getString("situation");
            String imageUrl = document.getString("id");
            Map<String, Object> locationMap = (Map<String, Object>) document.get("location");

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
                tvReason.setText("Reason: " + reason);
            }

            // Show social situation if available
            if (situation != null && !situation.isEmpty()) {
                tvSocialSituation.setVisibility(View.VISIBLE);
                tvSocialSituation.setText("Social Situation: " + situation);
            }

            // Load image if available
            if (imageUrl != null && !imageUrl.isEmpty()) {
                try {
                    // Initialize Firebase Storage
                    FirebaseStorage storage = FirebaseStorage.getInstance();

                    // Create a reference to the image file
                    StorageReference imageRef = storage.getReference().child("images/" + imageUrl);
                    // Get the download URL and load it
                    imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                ivMoodImage.setVisibility(View.VISIBLE);
                                Picasso.get()
                                        .load(uri)
                                        .placeholder(R.drawable.error_placeholder_image)
                                        .error(R.drawable.error_placeholder_image)
                                        .into(ivMoodImage);
                            })
                            .addOnFailureListener(e -> {
                                // Log the error but don't crash
                                ivMoodImage.setVisibility(View.GONE);
                            });
                } catch (Exception e) {
                    ivMoodImage.setVisibility(View.GONE);
                }
            } else {
                ivMoodImage.setVisibility(View.GONE);
            }

            if (locationMap != null) {
                // Get the longitude and latitude of the location
                Double latitude = (Double) locationMap.get("latitude");
                Double longitude = (Double) locationMap.get("longitude");

                if (latitude != null && longitude != null) {
                    try {
                        // Use geocoder to convert longitude and latitude to an address
                        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                        if (addresses != null && !addresses.isEmpty()) {
                            Address address = addresses.get(0);
                            StringBuilder sb = new StringBuilder();

                            // Set the locality/city
                            if (address.getLocality() != null) {
                                sb.append(address.getLocality());
                            }
                            // Set the area/province
                            if (address.getAdminArea() != null) {
                                if (sb.length() > 0) sb.append(", ");
                                sb.append(address.getAdminArea());
                            }
                            // Set country name
                            if (address.getCountryName() != null) {
                                if (sb.length() > 0) sb.append(", ");
                                sb.append(address.getCountryName());
                            }

                            String locationText = sb.toString();
                            tvLocation.setText(locationText);
                            tvLocation.setVisibility(View.VISIBLE);
                        } else {
                            throw new Exception("No addresses found");
                        }
                    } catch (Exception e) {
                        // Displays the coordinates if geocoder fails or if no addresses were found
                        String locationText = String.format(Locale.getDefault(),
                                "Location: %.6f, %.6f", latitude, longitude);
                        tvLocation.setText(locationText);
                        tvLocation.setVisibility(View.VISIBLE);
                    }
                } else {
                    tvLocation.setVisibility(View.GONE);
                }
            } else {
                tvLocation.setVisibility(View.GONE);
            }

            // Format timestamp based on what's available
            handleTimestamp(document);

        } catch (Exception e) {
            Toast.makeText(this, "Error displaying mood details", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles timestamp extraction and formatting from the mood document.
     *
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

        // Check for dayTime as string (ISO format)
        String dayTimeStr = document.getString("dayTime");
        if (dayTimeStr != null) {
            try {
                LocalDateTime dateTime = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    dateTime = LocalDateTime.parse(dayTimeStr);
                }
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
            DateTimeFormatter formatter = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a", Locale.getDefault());
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                tvTimestamp.setText(dateTime.format(formatter));
            }
        } catch (Exception e) {
            Log.w(TAG, "Error formatting LocalDateTime: " + e.getMessage());
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