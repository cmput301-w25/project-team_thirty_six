package com.example.androidproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

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

    // Request codes
    public static final int IMAGE_PICK_REQUEST_CODE = 1001;
    public static final int LOCATION_PICK_REQUEST_CODE = 1002;

    // Current data
    private Uri newImageUri;  // Only set when user selects a new image
    private String existingImageId;  // The ID of an existing image in Firebase
    private boolean hasImage = false;  // Flag to track if we have any image
    private String location;
    private String imageToDeleteId = null; // Track image ID to delete

    public MoodMediaManager(Activity activity, ImageView moodImageView, TextView locationTextView,
                            CardView imagePreviewCardView, CardView locationPreviewCardView,
                            TextView addImageText, TextView removeImageText, ImageView imageButtonIcon) {
        this.activity = activity;
        this.context = activity;
        this.moodImageView = moodImageView;
        this.locationTextView = locationTextView;
        this.imagePreviewCardView = imagePreviewCardView;
        this.locationPreviewCardView = locationPreviewCardView;
        this.addImageText = addImageText;
        this.removeImageText = removeImageText;
        this.imageButtonIcon = imageButtonIcon;
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
     * NEED TO IMPLEMENT
     */
    public void openLocationPicker() {
    }

    /**
     * Processes activity result for image selection
     */
    public void processImageResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
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
            }
        }
    }

    /**
     * Processes activity result for location selection
     * STILL NEEDS IMPLEMENTATION
     */
    public void processLocationResult(int resultCode, Intent data) {}

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
    public void setLocation(String location) {
        this.location = location;
        if (location != null && !location.isEmpty()) {
            locationTextView.setText(location);
            locationPreviewCardView.setVisibility(View.VISIBLE);
        }
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
    public String getLocation() {
        return location;
    }

    /**
     * Clears the image deletion marker
     * Call this when user cancels or after successful save
     */
    public void clearDeletionMarker() {
        imageToDeleteId = null;
    }
}