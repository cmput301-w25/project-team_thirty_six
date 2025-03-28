package com.example.androidproject;

import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.Map;

/**
 * EditMoodActivity provides functionality to modify existing mood events.
 * The activity allows the user to modify the mood state, visibility, reason, social situation,
 * date and time, and media attachments
 *
 * This class interacts with the following components:
 * - MoodDropDownManager: Handles the interface for changing a mood
 * - DataTimeManager: Manages the date and time selection
 * - SocialSituationManager: Manges the social situation
 * - MoodMediaManager: Manages the images and location processes
 * - MoodRepository: Interfaces with database to update mood event
 * - NavBarFragment: Provides navigation capabilities
 */
public class EditMoodActivity extends AppCompatActivity {
    // UI elements
    private Button doneButton, cancelButton;
    private Button moodDropdown;
    private EditText reasonText;
    private RadioGroup socialSituationRadioGroup, visibilityRadioGroup;
    private RadioButton radioAlone, radioPair, radioGroup, radioCrowd, radioPrivate, radioPublic;
    private LinearLayout addImageButton, addLocationButton, datePickerButton, timePickerButton;
    private CardView imagePreviewCardView, locationPreviewCardView;
    private ImageView moodImageView, imageButtonIcon;
    private TextView locationTextView, textViewSelectedDate, textViewSelectedTime, addImageText, removeImageText, addLocationText, removeLocationText;
    private FloatingActionButton deleteButton;

    // Data
    private String chosenMood, chosenColor, id;
    private boolean isPublic = false;

    // Managers
    private MoodDropdownManager moodDropdownManager;
    private DateTimeManager dateTimeManager;
    private SocialSituationManager socialSituationManager;
    private MoodRepository moodRepository;
    private MoodMediaManager mediaManager;

    /**
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_mood);

        // Retrieve the currentUser from the Intent
        String userId = (String) getIntent().getSerializableExtra("user");

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.nav_bar_container, NavBarFragment.newInstance(userId))
                    .commit();
        }

        // Initialize UI elements and managers
        initializeViews();
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
        reasonText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(200)});

        // Social situation elements
        socialSituationRadioGroup = findViewById(R.id.socialSituationRadioGroup);
        radioAlone = findViewById(R.id.radioAlone);
        radioPair = findViewById(R.id.radioPair);
        radioGroup = findViewById(R.id.radioGroup);
        radioCrowd = findViewById(R.id.radioCrowd);

        // Visibility elements
        visibilityRadioGroup = findViewById(R.id.visibilityRadioGroup);
        radioPrivate = findViewById(R.id.radioPrivate);
        radioPublic = findViewById(R.id.radioPublic);

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
        addLocationText = findViewById(R.id.add_location_text);
        removeLocationText = findViewById(R.id.remove_location_text);
    }

    /**
     * Initialize all managers needed for this activity
     */
    private void initializeManagers() {
        // Create repository
        moodRepository = new MoodRepository();

        // Create date/time manager
        dateTimeManager = new DateTimeManager(this, textViewSelectedDate, textViewSelectedTime);

        // Create social situation manager
        socialSituationManager = new SocialSituationManager(radioAlone, radioPair, radioGroup, radioCrowd);

        // Create media manager with updated implementation
        mediaManager = new MoodMediaManager(this, moodImageView, locationTextView,
                imagePreviewCardView, locationPreviewCardView,
                addImageText, removeImageText, imageButtonIcon, addLocationText, removeLocationText);

        // Create mood dropdown manager
        moodDropdownManager = new MoodDropdownManager(this, moodDropdown, mood -> {
            chosenMood = mood;
            chosenColor = new MoodState(mood).getColor();
        });
    }

    /**
     * Load data from intent
     * Extracts mood information, images, location, and timestamps
     */
    private void loadDataFromIntent() {
        Intent intent = getIntent();
        id = intent.getStringExtra("moodId");

        if (id == null) {
            Toast.makeText(this, "No mood ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Query Firestore to get the mood details
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference moodRef = db.collection("Moods").document(id);

        moodRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Extract all mood details from Firestore
                    chosenMood = document.getString("mood");
                    String color = document.getString("color");
                    String chosenSituation = document.getString("situation");
                    String reason = document.getString("reason");
                    Location location = null;
                    // Checks that the location isn't null
                    if (document.get("location") != null) {
                        // Gets the longitude and latitude
                        Double latitude = document.get("location.latitude", Double.class);
                        Double longitude = document.get("location.longitude", Double.class);
                        // makes the location
                        location = new Location(LocationManager.GPS_PROVIDER);
                        // Set sea level so that there is no invocation error
                        location.setMslAltitudeAccuracyMeters(0);
                        location.setMslAltitudeMeters(0);

                        location.setLatitude(latitude);
                        location.setLongitude(longitude);
                    }
                    String imageUrl = document.getString("id");
                    Object dayTimeObj = document.get("dayTime");
                    Boolean visibility = document.getBoolean("visibility");

                    // Set visibility (default to private/false if not set)
                    isPublic = (visibility != null) ? visibility : true;

                    Calendar calendar = Calendar.getInstance();

                    // Handle timestamp
                    if (dayTimeObj instanceof Map) {
                        Map<String, Object> dayTimeMap = (Map<String, Object>) dayTimeObj;

                        try {
                            // Safely extract each component with null checks
                            Integer year = dayTimeMap.containsKey("year") ?
                                    ((Long) dayTimeMap.get("year")).intValue() :
                                    calendar.get(Calendar.YEAR);

                            Integer monthValue = dayTimeMap.containsKey("monthValue") ?
                                    ((Long) dayTimeMap.get("monthValue")).intValue() - 1 :
                                    calendar.get(Calendar.MONTH);

                            Integer day = dayTimeMap.containsKey("dayOfMonth") ?
                                    ((Long) dayTimeMap.get("dayOfMonth")).intValue() :
                                    calendar.get(Calendar.DAY_OF_MONTH);

                            Integer hour = dayTimeMap.containsKey("hour") ?
                                    ((Long) dayTimeMap.get("hour")).intValue() :
                                    calendar.get(Calendar.HOUR_OF_DAY);

                            Integer minute = dayTimeMap.containsKey("minute") ?
                                    ((Long) dayTimeMap.get("minute")).intValue() :
                                    calendar.get(Calendar.MINUTE);

                            // Set the calendar with extracted or default values
                            calendar.set(
                                    year,
                                    monthValue,
                                    day,
                                    hour,
                                    minute
                            );
                        } catch (Exception e) {
                        }
                    }

                    // Update UI components
                    if (chosenMood != null) {
                        moodDropdown.setText(chosenMood);
                    }

                    if (reason != null) {
                        reasonText.setText(reason);
                    }

                    // Set visibility radio buttons
                    if (isPublic) {
                        radioPublic.setChecked(true);
                    } else {
                        radioPrivate.setChecked(true);
                    }

                    // Set date and time
                    dateTimeManager.setCalendar(calendar);

                    // Set social situation using manager
                    socialSituationManager.setSituation(chosenSituation);

                    // Use MoodMediaManager to handle image and location
                    mediaManager.setLocation(location);
                    mediaManager.setImageUrl(imageUrl);

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

        // on click listeners for displaying calendar and time dialogs
        datePickerButton.setOnClickListener(v -> dateTimeManager.showDatePicker());
        timePickerButton.setOnClickListener(v -> dateTimeManager.showTimePicker());

        // Visibility radio button listener
        visibilityRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            isPublic = (checkedId == R.id.radioPublic);
        });

        // Add media buttons
        addImageButton.setOnClickListener(v -> {
            if (imagePreviewCardView.getVisibility() == View.VISIBLE) {
                // If image is already visible it now shows remove image
                mediaManager.removeImage();
            } else {
                mediaManager.openImagePicker();
            }
        });

        addLocationButton.setOnClickListener(v -> {
            if (locationPreviewCardView.getVisibility() == View.VISIBLE) {
                // existing added location gets removed
                mediaManager.removeLocation();
            } else {
                // location picker/current location accessed
                mediaManager.openLocationPicker();
            }
        });

        // Confirm, cancel or delete changes made to mood event
        doneButton.setOnClickListener(v -> saveMood());
        cancelButton.setOnClickListener(v -> {
            mediaManager.clearDeletionMarker();
            finish();
        });
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    /**
     * Shows delete confirmation dialog
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
     * Function for when location needs to be picked and is done well
     */
    public void onLocationPicked(Location location) {
        // popup message acknowledging confirmation
        Toast.makeText(this, "Location updated", Toast.LENGTH_SHORT).show();
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
        // Get reason text and validate length
        String reason = reasonText.getText() != null ? reasonText.getText().toString().trim() : "";
        if (reason.length() > 200) {
            Toast.makeText(this, "Reason must be 20 characters or less.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get social situation from manager
        String chosenSituation = socialSituationManager.getChosenSituation();

        // Check if there was an existing image that was removed
        String imageToDeleteId = mediaManager.getImageToDeleteId();

        if (imageToDeleteId != null) {
            // Delete the image from Firebase Storage
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference imageRef = storage.getReference().child("images/" + imageToDeleteId);

            imageRef.delete().addOnSuccessListener(aVoid -> {
                moodRepository.updateMood(
                        id,
                        chosenMood,
                        chosenColor,
                        chosenSituation,
                        reason,
                        mediaManager.getLocation(),
                        mediaManager.getNewImageUri(),
                        null,
                        dateTimeManager.getCalendar(),
                        isPublic,
                        new MoodRepository.OnMoodUpdateListener() {
                            @Override
                            public void onSuccess() {
                                mediaManager.clearDeletionMarker();
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
            }).addOnFailureListener(e -> {
                moodRepository.updateMood(
                        id,
                        chosenMood,
                        chosenColor,
                        chosenSituation,
                        reason,
                        mediaManager.getLocation(),
                        mediaManager.getNewImageUri(),
                        null, // Still mark as removed even if deletion failed
                        dateTimeManager.getCalendar(),
                        isPublic,
                        new MoodRepository.OnMoodUpdateListener() {
                            @Override
                            public void onSuccess() {
                                mediaManager.clearDeletionMarker();
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
            });
        } else {
            // update the mood directly when there is no image to delete
            moodRepository.updateMood(
                    id,
                    chosenMood,
                    chosenColor,
                    chosenSituation,
                    reason,
                    mediaManager.getLocation(),
                    mediaManager.getNewImageUri(),
                    mediaManager.getExistingImageId(),
                    dateTimeManager.getCalendar(),
                    isPublic,
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
    }

    /**
     * Delete mood from Firestore
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
     * Process location result from the activity
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode The integer result code returned by the child activity
     *                   through its setResult().
     * @param data An Intent, which can return result data to the caller
     *               (various data can be attached to Intent "extras").
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MoodMediaManager.LOCATION_PICK_REQUEST_CODE) {
            mediaManager.processLocationResult(resultCode, data);
        }
    }
}