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
import android.location.Address;
import android.location.Geocoder;
import java.util.List;
import java.util.Locale;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
    private final AppCompatActivity activity;
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
    public static final int LOCATION_PICK_REQUEST_CODE = 1002;

    // Current data
    private Uri newImageUri;
    private String existingImageId;
    private boolean hasImage = false;
    private Location location;
    private String imageToDeleteId = null;

    // Image picker launcher
    private ActivityResultLauncher<String> imageLauncher;

    /**
     * Creates a constructor for mood media manager
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
     *      text that displays remove image
     * @param imageButtonIcon
     *      image button
     */
    public MoodMediaManager(AppCompatActivity activity, ImageView moodImageView, TextView locationTextView,
                            CardView imagePreviewCardView, CardView locationPreviewCardView,
                            TextView addImageText, TextView removeImageText, ImageView imageButtonIcon,
                            TextView addLocationText, TextView removeLocationText) {
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

        initializeImageLauncher();
    }

    /**
     * Initializes the image picker launcher using ActivityResultLauncher
     */
    private void initializeImageLauncher() {
        imageLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            // Check image size before proceeding
                            if (!isImageSizeValid(uri)) {
                                return;
                            }

                            // Clear any pending deletion
                            imageToDeleteId = null;

                            // Store the new image URI and clear any existing image ID
                            newImageUri = uri;
                            existingImageId = null;
                            hasImage = true;

                            // Display the selected image
                            Picasso.get()
                                    .load(uri)
                                    .into(moodImageView);

                            imagePreviewCardView.setVisibility(View.VISIBLE);

                            // switch to the remove image mode
                            showRemoveImageOption();
                        }
                    }
                }
        );
    }

    /**
     * Validates if image size is within the 64KB limit
     * @param imageUri URI of the selected image
     * @return true if valid, false otherwise
     */
    private boolean isImageSizeValid(Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Toast.makeText(context, "Unable to access the selected image",
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            int imageSize = inputStream.available();
            inputStream.close();

            if (imageSize >= 65536) {
                // Image is too large
                Toast.makeText(context, "Image size exceeds 64KB limit. Please select a smaller image.",
                        Toast.LENGTH_LONG).show();
                return false;
            }

            return true;
        } catch (IOException e) {
            Toast.makeText(context, "Error checking image size: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * Opens image picker using the ActivityResultLauncher
     */
    public void openImagePicker() {
        imageLauncher.launch("image/*");
    }

    /**
     * Opens location picker
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
     * Processes activity result for location selection
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
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(
                        location.getLatitude(), location.getLongitude(), 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    StringBuilder sb = new StringBuilder();
                    String thoroughfare = address.getThoroughfare();
                    String locality = address.getLocality();
                    String adminArea = address.getAdminArea();
                    String countryName = address.getCountryName();

                    if (thoroughfare != null) {
                        sb.append(thoroughfare);
                    }
                    if (locality != null) {
                        if (sb.length() > 0) sb.append(", ");
                        sb.append(locality);
                    }
                    if (adminArea != null) {
                        if (sb.length() > 0) sb.append(", ");
                        sb.append(adminArea);
                    }
                    if (countryName != null) {
                        if (sb.length() > 0) sb.append(", ");
                        sb.append(countryName);
                    }
                    String locationText = sb.toString();
                    locationTextView.setText(locationText);
                } else {
                    String locationText = String.format(Locale.getDefault(),
                            "Location: %.6f, %.6f", location.getLatitude(), location.getLongitude());
                    locationTextView.setText(locationText);
                }
            } catch (IOException e) {
                String locationText = String.format(Locale.getDefault(),
                        "Location: %.6f, %.6f", location.getLatitude(), location.getLongitude());
                locationTextView.setText(locationText);
            }

            locationPreviewCardView.setVisibility(View.VISIBLE);
            showRemoveLocationOption();
        } else {
            locationPreviewCardView.setVisibility(View.GONE);
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
     * If location is already added, the visual prompt to show they can remove it
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