package com.example.androidproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class EditMoodActivity extends AppCompatActivity {
    // UI elements
    private Button doneButton, cancelButton;
    private Button moodDropdown;
    private EditText reasonText;
    private RadioGroup socialSituationRadioGroup;
    private RadioButton radioAlone;
    private RadioButton radioPair;
    private RadioButton radioGroup;
    private RadioButton radioCrowd;
    private LinearLayout addImageButton, addLocationButton, datePickerButton, timePickerButton;
    private CardView imagePreviewCardView, locationPreviewCardView;
    private ImageView moodImageView, imageButtonIcon;
    private TextView locationTextView, textViewSelectedDate, textViewSelectedTime, addImageText, removeImageText;
    private RadioButton lastSelectedButton = null;
    private FloatingActionButton deleteButton;

    // Data
    private String chosenMood, chosenSituation, id;

    // Managers
    private MoodDropdownManager moodDropdownManager;
    private DateTimeManager dateTimeManager;
    private MoodRepository moodRepository;
    private MoodMediaManager mediaManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_mood);


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.nav_bar_container, new NavBarFragment())
                    .commit();
        }

        // Initialize UI elements
        initializeViews();
        // Initialize managers
        initializeManagers();
        // Load data from intent
        loadDataFromIntent();
        // Setup event listeners
        setupEventListeners();
    }

    /**
     * Initialize all UI elements by finding their views
     */
    private void initializeViews() {
        // Buttons
        doneButton = findViewById(R.id.done_button);
        cancelButton = findViewById(R.id.cancel_button);
        moodDropdown = findViewById(R.id.btnEditMoodSelectMood);
        deleteButton = findViewById(R.id.delete_button);

        // Text inputs
        reasonText = findViewById(R.id.editReason);
        reasonText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20)});

        // Social situation elements
        socialSituationRadioGroup = findViewById(R.id.socialSituationRadioGroup);
        radioAlone = findViewById(R.id.radioAlone);
        radioPair = findViewById(R.id.radioPair);
        radioGroup = findViewById(R.id.radioGroup);
        radioCrowd = findViewById(R.id.radioCrowd);

        // Date and time elements
        datePickerButton = findViewById(R.id.datePickerButton);
        timePickerButton = findViewById(R.id.timePickerButton);
        textViewSelectedDate = findViewById(R.id.textViewSelectedDate);
        textViewSelectedTime = findViewById(R.id.textViewSelectedTime);

        // Media elements
        addImageButton = findViewById(R.id.add_image_button);
        addImageText = findViewById(R.id.add_image_text);
        removeImageText = findViewById(R.id.remove_image_text);
        imageButtonIcon = findViewById(R.id.image_button_icon);
        addLocationButton = findViewById(R.id.add_location_button);
        imagePreviewCardView = findViewById(R.id.imagePreviewCardView);
        locationPreviewCardView = findViewById(R.id.locationPreviewCardView);
        moodImageView = findViewById(R.id.moodImageView);
        locationTextView = findViewById(R.id.locationTextView);
    }

    /**
     * Initialize all managers needed for this activity
     */
    private void initializeManagers() {
        // Create repository
        moodRepository = new MoodRepository();

        // Create date/time manager
        dateTimeManager = new DateTimeManager(this, textViewSelectedDate, textViewSelectedTime);

        // Create media manager
        mediaManager = new MoodMediaManager(this, moodImageView, locationTextView,
                imagePreviewCardView, locationPreviewCardView,
                addImageText, removeImageText, imageButtonIcon);

        // Create mood dropdown manager
        moodDropdownManager = new MoodDropdownManager(this, moodDropdown,
                mood -> chosenMood = mood);
    }

    /**
     * Load data from intent
     * Extracts mood information, images, location, and timestamps
     */
    private void loadDataFromIntent() {
        Intent intent = getIntent();

        // Get mood ID and essential data
        id = intent.getStringExtra("id");
        chosenMood = intent.getStringExtra("mood");
        chosenSituation = intent.getStringExtra("situation");
        String reason = intent.getStringExtra("reason");

        // Get media data
        String location = intent.getStringExtra("location");
        String imageUrl = intent.getStringExtra("image");

        // Get timestamp
        long timestamp = intent.getLongExtra("timestamp", System.currentTimeMillis());

        // Update UI with data
        if (chosenMood != null) moodDropdown.setText(chosenMood);
        if (reason != null) reasonText.setText(reason);

        // Set date and time
        dateTimeManager.setCalendarFromTimestamp(timestamp);

        // Set social situation
        if (chosenSituation != null) {
            switch (chosenSituation) {
                case "Alone":
                    radioAlone.setChecked(true);
                    lastSelectedButton = radioAlone;
                    break;
                case "Pair":
                    radioPair.setChecked(true);
                    lastSelectedButton = radioPair;
                    break;
                case "Group":
                    radioGroup.setChecked(true);
                    lastSelectedButton = radioGroup;
                    break;
                case "Crowd":
                    radioCrowd.setChecked(true);
                    lastSelectedButton = radioCrowd;
                    break;
            }
        } else {
            // No selection
            socialSituationRadioGroup.clearCheck();
            lastSelectedButton = null;
        }

        // Set media
        mediaManager.setLocation(location);
        mediaManager.setImageUrl(imageUrl);
    }

    /**
     * Setup all event listeners for user interactions
     */
    private void setupEventListeners() {
        // Show or hide the mood dropdown menu when mood selection button it clicked
        moodDropdown.setOnClickListener(v -> {
            if (moodDropdownManager.isShowing()) {
                moodDropdownManager.hideDropdown();
            } else {
                moodDropdownManager.showDropdown();
            }
        });


        // Handle social situation listeners
        View.OnClickListener situationClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton clickedButton = (RadioButton) v;

                if (clickedButton == lastSelectedButton) {
                    clickedButton.setChecked(false);
                    lastSelectedButton = null;
                    chosenSituation = null;
                } else {
                    if (lastSelectedButton != null) {
                        lastSelectedButton.setChecked(false);
                    }
                    // Select the new button
                    clickedButton.setChecked(true);
                    lastSelectedButton = clickedButton;

                    // Set the chosen situation
                    int id = v.getId();
                    if (id == R.id.radioAlone) {
                        chosenSituation = "Alone";
                    } else if (id == R.id.radioPair) {
                        chosenSituation = "Pair";
                    } else if (id == R.id.radioGroup) {
                        chosenSituation = "Group";
                    } else if (id == R.id.radioCrowd) {
                        chosenSituation = "Crowd";
                    }
                }
            }
        };

        // on click listeners for displaying calendar and time dialogs
        datePickerButton.setOnClickListener(v -> dateTimeManager.showDatePicker());
        timePickerButton.setOnClickListener(v -> dateTimeManager.showTimePicker());

        // Add media buttons
        addImageButton.setOnClickListener(v -> {
            if (imagePreviewCardView.getVisibility() == View.VISIBLE) {
                // If image is already visible it now shows remove image
                mediaManager.removeImage();
            } else {
                mediaManager.openImagePicker();
            }
        });
        addLocationButton.setOnClickListener(v -> mediaManager.openLocationPicker());

        // Confirm, cancel or delete changes made to mood event
        doneButton.setOnClickListener(v -> saveMood());
        cancelButton.setOnClickListener(v -> finish());
        radioAlone.setOnClickListener(situationClickListener);
        radioPair.setOnClickListener(situationClickListener);
        radioGroup.setOnClickListener(situationClickListener);
        radioCrowd.setOnClickListener(situationClickListener);
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    /**
     * Brings up dialog to confirm deletion of a mood
     */
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Mood")
                .setMessage("Are you sure you want to delete this mood?")
                .setPositiveButton("Delete", (dialog, which) -> deleteMood())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    /**
     * Save updated mood to Firestore
     */
    private void saveMood() {
        // Validate mood selection
        if (chosenMood == null || chosenMood.isEmpty()) {
            Toast.makeText(this, "Please select a mood before saving.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Ensures that there is a mood id for the corresponding mood
        if (id == null) {
            Toast.makeText(this, "Error: Invalid mood ID", Toast.LENGTH_SHORT).show();
            return;
        }
        // Get reason text and validate length
        String reason = reasonText.getText() != null ? reasonText.getText().toString().trim() : "";
        if (reason.length() > 20) {
            Toast.makeText(this, "Reason must be 20 characters or less.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Call repository to update mood
        moodRepository.updateMood(
                id,
                chosenMood,
                chosenSituation,
                reason,
                mediaManager.getLocation(),
                mediaManager.getImageUrl(),
                dateTimeManager.getCalendar(),
                new MoodRepository.OnMoodUpdateListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(EditMoodActivity.this,
                                "Mood updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(EditMoodActivity.this,
                                "Failed to update mood. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
    /**
     * Delete mood from firestore
     */
    private void deleteMood() {
        moodRepository.deleteMood(id, new MoodRepository.OnMoodDeleteListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(EditMoodActivity.this,
                        "Mood deleted successfully", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EditMoodActivity.this,
                        "Failed to delete mood: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Handles activity results for image and location pickers
     * handles the selected media and updates the UI
     * @param requestCode The request code passed to startActivityForResult()
     * @param resultCode The result code returned by the child activity
     * @param data An Intent containing the result data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MoodMediaManager.IMAGE_PICK_REQUEST_CODE) {
            mediaManager.processImageResult(resultCode, data);
        } else if (requestCode == MoodMediaManager.LOCATION_PICK_REQUEST_CODE) {
            mediaManager.processLocationResult(resultCode, data);
        }
    }
}