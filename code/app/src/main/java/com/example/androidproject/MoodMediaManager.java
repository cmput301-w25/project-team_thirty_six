package com.example.androidproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;

/**
 * Manages media operations like images and location for mood editing
 */
public class MoodMediaManager {
    private final Context context;
    private final Activity activity;
    private final ImageView moodImageView;
    private final TextView locationTextView;
    private final CardView imagePreviewCardView;
    private final CardView locationPreviewCardView;
    private final TextView removeImageText;
    private final TextView addImageText;
    private final ImageView imageButtonIcon;
    private final TextView addLocationText;
    private final TextView removeLocationText;

    // Request codes
    public static final int IMAGE_PICK_REQUEST_CODE = 1001;
    public static final int LOCATION_PICK_REQUEST_CODE = 1002;

    // Current data
    private Uri newImageUri;  // Only set when user selects a new image
    private String existingImageId;  // The ID of an existing image in Firebase
    private boolean hasImage = false;  // Flag to track if we have any image
    private Location location;
    private String imageToDeleteId = null; // Track image ID to delete

    /**
     *  Creats a constructor for mood media manager
     * @param activity
     *      current activity
     * @param moodImageView
     *      image button
     * @param locationTextView
     *      location button
     * @param imagePreviewCardView
     *      card where preview image is displayed
     * @param locationPreviewCardView
     *      preview of the location
     * @param addImageText
     *      text that says to add image
     * @param removeImageText
     *      text that displays temove image
     * @param imageButtonIcon
     *      image button
     */
    public MoodMediaManager(Activity activity, ImageView moodImageView, TextView locationTextView,
                            CardView imagePreviewCardView, CardView locationPreviewCardView,
                            TextView addImageText, TextView removeImageText, ImageView imageButtonIcon,
                            TextView addLocationText, TextView removeLocationText) { //updated parameters
        this.activity = activity;
        this.context = activity;
        this.moodImageView = moodImageView;
        this.locationTextView = locationTextView;
        this.imagePreviewCardView = imagePreviewCardView;
        this.locationPreviewCardView = locationPreviewCardView;
        this.addImageText = addImageText;
        this.removeImageText = removeImageText;
        this.imageButtonIcon = imageButtonIcon;
        this.addLocationText = addLocationText;
        this.removeLocationText = removeLocationText;
    }

    /**
     * Opens image picker
     */
    public void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        activity.startActivityForResult(intent, IMAGE_PICK_REQUEST_CODE);
    }

    /**
     * Opens location picker
     * In progress
     */
    public void openLocationPicker() {
        // If user is editing an existing location, the picker is opened on the map
        if (activity instanceof EditMoodActivity) {
            // map picker
            Intent mapPickerIntent = new Intent(context, LocationPickerActivity.class);

            // existing location is the starting point for vicinity traversing
            if (location != null) {
                mapPickerIntent.putExtra("latitude", location.getLatitude());
                mapPickerIntent.putExtra("longitude", location.getLongitude());
            }

            activity.startActivityForResult(mapPickerIntent, LOCATION_PICK_REQUEST_CODE);
        }
        // if adding location for the first time then we just take current location without map picker
        else {
            // location permission fragment to get current location
            LocationPermissionFragment locationFragment = new LocationPermissionFragment();
            locationFragment.show(((AppCompatActivity)context).getSupportFragmentManager(),
                    "LocationPermissionFragment");

            // Permission check (if granted or not)
            locationFragment.setOnPermissionGrantedListener(() -> {
                locationFragment.startTrackingLocation();

                locationFragment.getLastKnownLocation(new LocationPermissionFragment.OnLocationReceivedListener() {
                    @Override
                    public void onLocationReceived(Location location) {
                        location.setMslAltitudeAccuracyMeters(0);
                        location.setMslAltitudeMeters(0);
                        // Save the location and update the display to show this
                        setLocation(location);

                        if (activity instanceof EditMoodActivity) {
                            ((EditMoodActivity) activity).onLocationPicked(location);
                        }
                    }

                    @Override
                    public void onLocationFailure(String errorMessage) {
                        Toast.makeText(context, "Failed to get location: " + errorMessage,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
    }

    /**
     * Processes activity result for image selection
     */
    public void processImageResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                // Check image size before proceeding
                InputStream inputStream = null;
                try {
                    inputStream = context.getContentResolver().openInputStream(selectedImage);
                    if (inputStream == null) {
                        Toast.makeText(context, "Unable to access the selected image",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int imageSize = inputStream.available();

                    if (imageSize >= 65536) {
                        // Image is too large
                        Toast.makeText(context, "Image size exceeds 64KB limit. Please select a smaller image.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Clear any pending deletion
                    imageToDeleteId = null;

                    // Store the new image URI and clear any existing image ID
                    newImageUri = selectedImage;
                    existingImageId = null;
                    hasImage = true;

                    // Display the selected image
                    Picasso.get()
                            .load(selectedImage)
                            .into(moodImageView);

                    imagePreviewCardView.setVisibility(View.VISIBLE);

                    // switch to the remove image mode
                    showRemoveImageOption();

                } catch (IOException e) {
                    Toast.makeText(context, "Error checking image size: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            // Log the error but continue
                            Log.e("MoodMediaManager", "Error closing input stream", e);
                        }
                    }
                }
            }
        }
    }

    /**
     * Processes activity result for location selection
     * Initially unused but gained knowledge from https://www.youtube.com/watch?v=mbQd6frpC3g
     */
    public void processLocationResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (data.hasExtra("latitude") && data.hasExtra("longitude")) {
                double latitude = data.getDoubleExtra("latitude", 0);
                double longitude = data.getDoubleExtra("longitude", 0);

                // Create location object
                Location selectedLocation = new Location("map-selection");
                selectedLocation.setLatitude(latitude);
                selectedLocation.setLongitude(longitude);

                selectedLocation.setMslAltitudeAccuracyMeters(0);
                selectedLocation.setMslAltitudeMeters(0);

                // Update the location
                setLocation(selectedLocation);

                // Notify activity if necessary
                if (activity instanceof EditMoodActivity) {
                    ((EditMoodActivity) activity).onLocationPicked(selectedLocation);
                }
            }
        }
    }

    /**
     * Sets image ID from existing data in Firebase
     */
    public void setImageUrl(String imageId) {
        // Clear any new image selection and deletion tracking
        newImageUri = null;
        imageToDeleteId = null;

        if (imageId != null && !imageId.isEmpty()) {
            existingImageId = imageId;
            hasImage = true;

            // Load image from firebase
            StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                    .child("images/" + imageId);

            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Picasso.get()
                        .load(uri)
                        .into(moodImageView);

                imagePreviewCardView.setVisibility(View.VISIBLE);
                // Toggle to remove image mode
                showRemoveImageOption();
            }).addOnFailureListener(e -> {
                // Handle failure to load image
                hasImage = false;
                existingImageId = null;
                imagePreviewCardView.setVisibility(View.GONE);
                showAddImageOption();
            });
        } else {
            existingImageId = null;
            hasImage = false;
            showAddImageOption();
            imagePreviewCardView.setVisibility(View.GONE);
        }
    }

    /**
     * Sets location from existing data
     */
    public void setLocation(Location location) {
        this.location = location;
        if (location != null) {
            locationTextView.setText(location.toString());
            locationPreviewCardView.setVisibility(View.VISIBLE);
            showRemoveLocationOption();

        } else {
            locationPreviewCardView.setVisibility((View.GONE));
            showAddLocationOption();
        }
    }

    /**
     * Removes location
     */
    public void removeLocation() {
        this.location = null;
        locationPreviewCardView.setVisibility(View.GONE);
        showAddLocationOption();
    }

    /**
     * Removes the current image from UI and marks it for deletion
     */
    public void removeImage() {
        // Store the existing ID for later deletion
        if (existingImageId != null) {
            imageToDeleteId = existingImageId;
        }

        newImageUri = null;
        existingImageId = null;
        hasImage = false;
        imagePreviewCardView.setVisibility(View.GONE);
        showAddImageOption();
    }

    /**
     * Shows the "Add Image" option
     */
    private void showAddImageOption() {
        addImageText.setVisibility(View.VISIBLE);
        removeImageText.setVisibility(View.GONE);
        imageButtonIcon.setImageResource(R.drawable.image);
    }

    /**
     * Shows the "Remove Image" option
     */
    private void showRemoveImageOption() {
        addImageText.setVisibility(View.GONE);
        removeImageText.setVisibility(View.VISIBLE);
        imageButtonIcon.setImageResource(R.drawable.image);
    }

    /**
     * Gets the new image URI for uploading to Firebase
     */
    public Uri getNewImageUri() {
        return newImageUri;
    }

    /**
     * Gets the existing image ID in Firebase
     */
    public String getExistingImageId() {
        return existingImageId;
    }

    /**
     * Gets the ID of the image to delete when saving
     */
    public String getImageToDeleteId() {
        return imageToDeleteId;
    }

    /**
     * Gets the current location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * The visual prompt to add a location if user wishes to
     */
    private void showAddLocationOption() {
        addLocationText.setVisibility(View.VISIBLE);
        removeLocationText.setVisibility(View.GONE);
    }

    /**
     * If location is already added, the visual prompt to show them they can remove it
     */
    private void showRemoveLocationOption() {
        addLocationText.setVisibility(View.GONE);
        removeLocationText.setVisibility(View.VISIBLE);
    }

    /**
     * Clears the image deletion marker
     * Call this when user cancels or after successful save
     */
    public void clearDeletionMarker() {
        imageToDeleteId = null;
    }
}