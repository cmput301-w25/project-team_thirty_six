package com.example.androidproject;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditMoodActivity extends AppCompatActivity {
    private Button doneButton, cancelButton;
    private Button moodDropdown;
    private EditText reasonText;
    private ListView dropdownList;
    private RadioGroup socialSituationRadioGroup;
    private RadioButton radioAlone, radioPair, radioGroup;
    private LinearLayout addImageButton, addLocationButton, datePickerButton, timePickerButton;
    private CardView imagePreviewCardView, locationPreviewCardView;
    private ImageView moodImageView;
    private TextView locationTextView, textViewSelectedDate, textViewSelectedTime;
    private MoodDropdownManager moodDropdownManager;
    private String chosenMood, chosenSituation, moodId, location, imageUrl;
    private FirebaseFirestore db;
    private MoodSelectionAdapter dropdownAdapter;
    private Boolean dropdownStatus = false;
    private Calendar selectedDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_mood);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.nav_bar_container, new NavBarFragment())
                    .commit();
        }

        db = FirebaseFirestore.getInstance();
        selectedDateTime = Calendar.getInstance();

        // Initialize UI Elements
        doneButton = findViewById(R.id.done_button);
        cancelButton = findViewById(R.id.cancel_button);
        moodDropdown = findViewById(R.id.btnEditMoodSelectMood);
        reasonText = findViewById(R.id.editReason);
        dropdownList = findViewById(R.id.edit_mood_select_mood_list);


        // Initialize Social situation elements
        socialSituationRadioGroup = findViewById(R.id.socialSituationRadioGroup);
        radioAlone = findViewById(R.id.radioAlone);
        radioPair = findViewById(R.id.radioPair);
        radioGroup = findViewById(R.id.radioGroup);

        // Initialize date and Time elements
        datePickerButton = findViewById(R.id.datePickerButton);
        timePickerButton = findViewById(R.id.timePickerButton);
        textViewSelectedDate = findViewById(R.id.textViewSelectedDate);
        textViewSelectedTime = findViewById(R.id.textViewSelectedTime);

        // Initialize button and image elements
        addImageButton = findViewById(R.id.add_image_button);
        addLocationButton = findViewById(R.id.add_location_button);
        imagePreviewCardView = findViewById(R.id.imagePreviewCardView);
        locationPreviewCardView = findViewById(R.id.locationPreviewCardView);
        moodImageView = findViewById(R.id.moodImageView);
        locationTextView = findViewById(R.id.locationTextView);

        // Get existing mood data from Intent
        Intent intent = getIntent();
        moodId = intent.getStringExtra("id");
        chosenMood = intent.getStringExtra("mood");
        chosenSituation = intent.getStringExtra("situation");
        String reason = intent.getStringExtra("reason");
        location = intent.getStringExtra("location");
        imageUrl = intent.getStringExtra("imageUrl");

        // Handle date and time from intent
        long timestamp = intent.getLongExtra("timestamp", System.currentTimeMillis());
        selectedDateTime.setTimeInMillis(timestamp);
        updateDateTimeDisplay();

        // Pre-fill fields with existing data
        if (chosenMood != null) moodDropdown.setText(chosenMood);
        if (reason != null) reasonText.setText(reason);

        // Set social situation
        if (chosenSituation != null) {
            switch (chosenSituation) {
                case "Alone":
                    radioAlone.setChecked(true);
                    break;
                case "Pair":
                    radioPair.setChecked(true);
                    break;
                case "Group":
                    radioGroup.setChecked(true);
                    break;
            }
        }

        // Show location if available
        if (location != null && !location.isEmpty()) {
            locationTextView.setText(location);
            locationPreviewCardView.setVisibility(View.VISIBLE);
        }

        // Show image if available
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Implement image loading logic (e.g., using Glide or Picasso)
            // Glide.with(this).load(imageUrl).into(moodImageView);
            imagePreviewCardView.setVisibility(View.VISIBLE);
        }

        // Setup mood dropdown manager
        moodDropdownManager = new MoodDropdownManager(this, moodDropdown, new MoodDropdownManager.MoodSelectedListener() {
            @Override
            public void onMoodSelected(String mood) {
                chosenMood = mood;
            }
        });

        // Set dropdown button listener
        moodDropdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moodDropdownManager.isShowing()) {
                    moodDropdownManager.hideDropdown();
                } else {
                    moodDropdownManager.showDropdown();
                }
            }
        });

        // Social situation radio group listener
        socialSituationRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioAlone) {
                chosenSituation = "Alone";
            } else if (checkedId == R.id.radioPair) {
                chosenSituation = "Pair";
            } else if (checkedId == R.id.radioGroup) {
                chosenSituation = "Group";
            }
        });

        // Date picker listener
        datePickerButton.setOnClickListener(v -> showDatePicker());

        // Time picker listener
        timePickerButton.setOnClickListener(v -> showTimePicker());

        // Image button listener
        addImageButton.setOnClickListener(v -> {
            openImagePicker();
        });

        // Location button listener
        addLocationButton.setOnClickListener(v -> {
            // Implement location picker
            openLocationPicker();
        });

        // Save changes to Firestore when clicking done
        doneButton.setOnClickListener(v -> updateMoodInFirestore());

        // Close activity without saving when clicking cancel
        cancelButton.setOnClickListener(v -> finish());
    }

    /**
     * Shows date picker dialog with custom dark theme
     */
    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                new ContextThemeWrapper(this, R.style.CustomDateTimePickerTheme),
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateTimeDisplay();
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    /**
     * Shows time picker dialog
     */
    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                new ContextThemeWrapper(this, R.style.CustomDateTimePickerTheme),
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    updateDateTimeDisplay();
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    /**
     * Updates the date and time display
     */
    private void updateDateTimeDisplay() {
        // Update date display
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        textViewSelectedDate.setText(dateFormat.format(selectedDateTime.getTime()));

        // Update time display
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        textViewSelectedTime.setText(timeFormat.format(selectedDateTime.getTime()));
    }

    /**
     * Opens image picker
     */
    private void openImagePicker() {
    }

    /**
     * Opens location picker
     */
    private void openLocationPicker() {
    }

    /**
     * Updates the mood post in Firestore.
     */
    private void updateMoodInFirestore() {
        // Prevent saving if the mood is not selected
        if (chosenMood == null || chosenMood.isEmpty()) {
            Toast.makeText(this, "Please select a mood before saving.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("mood", chosenMood);
        updatedData.put("timestamp", selectedDateTime.getTimeInMillis());

        // Only add fields to Firestore if they are not empty
        if (chosenSituation != null && !chosenSituation.isEmpty()) {
            updatedData.put("situation", chosenSituation);
        }

        if (reasonText.getText() != null && !reasonText.getText().toString().trim().isEmpty()) {
            updatedData.put("reason", reasonText.getText().toString().trim());
        }

        if (location != null && !location.isEmpty()) {
            updatedData.put("location", location);
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            updatedData.put("imageUrl", imageUrl);
        }

        // Update Firestore
        db.collection("moods").document(moodId)
                .update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Mood updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update mood. Please try again.", Toast.LENGTH_SHORT).show()
                );
    }

    // You would also need to implement onActivityResult to handle responses from the image and location pickers
    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_REQUEST_CODE && data != null) {
                // Handle image selection
                Uri selectedImage = data.getData();
                imageUrl = selectedImage.toString();
                moodImageView.setImageURI(selectedImage);
                imagePreviewCardView.setVisibility(View.VISIBLE);
            } else if (requestCode == LOCATION_PICK_REQUEST_CODE && data != null) {
                // Handle location selection
                location = data.getStringExtra("location");
                locationTextView.setText(location);
                locationPreviewCardView.setVisibility(View.VISIBLE);
            }
        }
    }
    */
}