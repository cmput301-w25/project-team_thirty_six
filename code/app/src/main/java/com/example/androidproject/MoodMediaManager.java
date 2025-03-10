package com.example.androidproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

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
    private String imageUrl;
    private String location;

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
                imageUrl = selectedImage.toString();

                Picasso.get()
                        .load(selectedImage)
                        .into(moodImageView);

                imagePreviewCardView.setVisibility(View.VISIBLE);

                // switch to remove image mode
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
     * Sets image URL from existing data
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Load image using Picasso
            Picasso.get()
                    .load(imageUrl)
                    .into(moodImageView);

            imagePreviewCardView.setVisibility(View.VISIBLE);

            // Toggle to "Remove Image" mode
            showRemoveImageOption();
        } else {
            showAddImageOption();
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
     * Removes the current image
     */
    public void removeImage() {
        imageUrl = null;
        imagePreviewCardView.setVisibility(View.GONE);
        showAddImageOption();
    }

    /**
     * Shows the "Add Image" option
     */
    private void showAddImageOption() {
        addImageText.setVisibility(View.VISIBLE);
        removeImageText.setVisibility(View.GONE);
        imageButtonIcon.setImageResource(R.drawable.image); // You may need to use a different icon for add
    }

    /**
     * Shows the "Remove Image" option
     */
    private void showRemoveImageOption() {
        addImageText.setVisibility(View.GONE);
        removeImageText.setVisibility(View.VISIBLE);
        imageButtonIcon.setImageResource(R.drawable.image); // You may need to use a different icon for remove
    }

    /**
     * Gets the current image URL
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Gets the current location
     */
    public String getLocation() {
        return location;
    }
}