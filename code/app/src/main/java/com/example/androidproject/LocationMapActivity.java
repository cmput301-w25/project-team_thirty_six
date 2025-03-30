package com.example.androidproject;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Random;

public class LocationMapActivity extends AppCompatActivity {

    private ArrayList<MoodState> moodHistory;
    private MoodHistoryManager moodHistoryManager;
    private String currentUser;
    private static final String TAG = "LocationMapActivity";
    private static final double radius_in_km = 5.0;
    private GoogleMap mMap;
    ImageButton followingFilter;
    ImageButton moodFilter;
    private boolean isMapReady = false;
    private boolean feedScreen = Boolean.FALSE;
    private FeedManager feedManager;
    private FloatingActionButton distanceFilter;
    private CheckBox checkNearbyFollowing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_map);

        // Retrieve user from Intent
        currentUser = getIntent().getStringExtra("currentUser");
        if (currentUser == null) {
            Log.e(TAG, "Error: currentUser is NULL!");
            Toast.makeText(this, "Error: User not found!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Log.d(TAG, "User: " + currentUser);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.nav_bar_container, NavBarFragment.newInstance(currentUser))
                    .commit();
        }
        // Initialize MoodHistoryManager and list
        moodHistoryManager = new MoodHistoryManager();
        moodHistory = new ArrayList<>();

        //loads map fragment
        initializeMap();

        // Gets the filtering buttons
        followingFilter = findViewById(R.id.following_button_map);
        moodFilter = findViewById(R.id.map_filter);
        distanceFilter = findViewById(R.id.distance_filter);
        feedManager = new FeedManager();

        // Creates the on click listener for mood filters
        moodFilter.setOnClickListener(v -> showFilterDialog());
        // Creates an on click listener for the feed display
        followingFilter.setOnClickListener(new View.OnClickListener() {
            /**
             * Creates the on click listener that changes screen to feed screen
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                // If we are currently on feed switch to users own
                if (feedScreen) {
                    // Fetches the mood history of the user and updates the markers
                    fetchMoodHistory(currentUser);
                    followingFilter.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.personal,null));
                    // Sets feed status to false
                    feedScreen = Boolean.FALSE;
                } else {
                    // Fetches the mood feed and updates the markers
                    fetchMoodFeed(currentUser);
                    followingFilter.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.feed,null));
                    // Sets feed status to True
                    feedScreen = Boolean.TRUE;
                }
            }
        });
    }
    //creates the map fragment as well as its assets
    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);

        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                /**
                 * When ready initialize map aspect amd fetch the mood history of user
                 * @param googleMap
                 */
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    // Enables zoomin because it way annoying me
                    mMap.getUiSettings().setZoomControlsEnabled(Boolean.TRUE);
                    isMapReady = true;
                    Log.d(TAG, "Google Map is ready.");

                    // Once map is ready, fetch mood history
                    fetchMoodHistory(currentUser);
                }
            });
        } else {
            Log.e(TAG, "Error: SupportMapFragment is NULL!");
            Toast.makeText(this, "Error loading map.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Fetches the mood history of the user and adds markers on the map
     * @param username
     *      the name of the user who's feed we are checking
     */
    private void fetchMoodHistory(String username) {
        Log.d(TAG, "Fetching mood history for user: " + username);

        moodHistoryManager.fetchMoodHistory(username, new MoodHistoryManager.MoodHistoryCallback() {
            @Override
            public void onCallback(ArrayList<MoodState> retrievedMoods) {
                if (retrievedMoods != null) {
                    moodHistory.clear();
                    moodHistory.addAll(retrievedMoods);
                    Log.d(TAG, "Fetched " + moodHistory.size() + " moods.");
                } else {
                    Log.e(TAG, "No moods found.");
                    Toast.makeText(LocationMapActivity.this, "No moods found for user.", Toast.LENGTH_SHORT).show();
                }

                // Updates the map if the map is ready
                if (isMapReady) {
                    updateMapMarkers();
                } else {
                    Log.w(TAG, "Map is not ready yet. Markers will be added once map is loaded.");
                }
            }
        });
    }

    /**
     * Gets the feed of a user
     * @param username
     *      the name of the user who's feed we are checking
     */
    private void fetchMoodFeed(String username) {
        Log.d(TAG, "Fetching mood history for user: " + username);
        // Gets the following list
        feedManager.getFollowing(username, new FeedManager.FollowingCallback() {
            /**
             *  Callback that provides the following list
             * @param following
             *      following list
             */
            @Override
            public void onCallback(ArrayList<String> following) {
                // Gets the feed using the following list
                feedManager.fetchFeed(following, new FeedManager.FeedCallback() {
                    /**
                     * Callback for the feed
                     * @param feed
                     *      list of moods that make up the feed
                     */
                    @Override
                    public void onCallback(ArrayList<MoodState> feed) {
                        // Clears mood history and makes it equal to the feed
                        Log.e("MOOD HISTORY SIZE", String.valueOf(moodHistory.size()));
                        moodHistory.clear();
                        Log.e("MOOD HISTORY SIZE", String.valueOf(moodHistory.size()));
                        moodHistory = feed;
                        Log.e("MOOD HISTORY SIZE", String.valueOf(moodHistory.size()));

                        updateMapMarkers();
                    }
                });
            }
        });
    }

    /**
     * Adds slight variations to the latitude and longitude to prevent markers from overlapping.
     */
    private LatLng getOffsetLocation(double latitude, double longitude) {
        Random random = new Random();
        double latOffset = (random.nextDouble() - 0.5) * 0.0005; // Small offset (~50m)
        double lonOffset = (random.nextDouble() - 0.5) * 0.0005; // Small offset (~50m)
        return new LatLng(latitude + latOffset, longitude + lonOffset);
    }

    /**
     * Adds the location markers the the map using the latitude and longitude
     * Sets the camera where as long as there is one valid location it will
     * be the initial camera viewpoint
     */
    private void updateMapMarkers() {
        if (!isMapReady || mMap == null) {
            Log.e(TAG, "updateMapMarkers: Google Map is NULL or not ready!");
            return;
        }

        mMap.clear(); // Remove old markers

        if (!moodHistory.isEmpty()) {
            Log.d(TAG, "Processing " + moodHistory.size() + " moods.");

            boolean hasValidLocation = false;
            LatLng firstValidLocation = null;

            // Loops through all the moods
            for (MoodState mood : moodHistory) {
                // Only displays moods with location
                if (mood.getLocation() != null) {
                    double lat = mood.getLocation().getLatitude();
                    double lon = mood.getLocation().getLongitude();

                    // Gets the emoji to display
                    Bitmap decodedEmoji = BitmapFactory.decodeResource(getResources(),mood.getEmoji());
                    Bitmap tempEmoji = Bitmap.createScaledBitmap(decodedEmoji,200,200,Boolean.FALSE);
                    BitmapDescriptor emoji = BitmapDescriptorFactory.fromBitmap(tempEmoji);
                    // Checks that the latitude and longitude are valid
                    if (lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180) {
                        LatLng adjustedLocation = getOffsetLocation(lat, lon);
                        // Addds the marker to the map
                        mMap.addMarker(new MarkerOptions()
                                        .position(adjustedLocation)
                                        .title("@" + mood.getUser())
                                        .snippet("Mood: " + mood.getMood()))
                                // Makes emojis on the map
                                .setIcon(emoji);;

                        Log.d(TAG, "Added marker at: " + adjustedLocation.toString() + " for mood: " + mood.getMood());

                        if (!hasValidLocation) {
                            firstValidLocation = adjustedLocation;
                            hasValidLocation = true;
                        }
                    } else {
                        Log.e(TAG, "Invalid coordinates for mood: " + mood.getMood() + " [lat=" + lat + ", lon=" + lon + "]");
                    }
                } else {
                    Log.w(TAG, "Skipping mood with NULL location: " + mood.getMood());
                }
            }

            // Move camera ONLY if there is at least one valid location
            if (hasValidLocation && firstValidLocation != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstValidLocation, 10));
            }
        }
    }
    /**
     * Filters the mood history by the most recent week.
     */
    public void filterByRecentWeek() {
        moodHistory = Filter.filterByRecentWeek(moodHistory);
        updateMapMarkers();
    }

    /**
     * Filters the mood history by a specific emotional state.
     *
     * @param emotionalState
     *        The emotional state to filter by.
     */
    public void filterByEmotionalState(String emotionalState) {
        moodHistory = Filter.filterByEmotionalState(moodHistory, emotionalState);
        updateMapMarkers();
    }

    /**
     * Filters the mood history by a keyword in the reason text.
     *
     * @param keyword
     *      The keyword to filter by.
     */
    public void filterByKeyword(String keyword) {
        moodHistory = Filter.filterByKeyword(moodHistory, keyword);
        updateMapMarkers();
    }

    /**
     * Shows the filter dialog with options.
     */
    public void showFilterDialog() {
        // Inflate the custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filter, null);

        CheckBox checkRecentWeek = dialogView.findViewById(R.id.check_recent_week);
        CheckBox checkFilterMood = dialogView.findViewById(R.id.check_filter_mood);
        CheckBox checkFilterKeyword = dialogView.findViewById(R.id.check_filter_keyword);

        CheckBox checkNearbyFollowing = dialogView.findViewById(R.id.check_nearby_following); // added the 5k feature checkbox

        Spinner spinnerMoods = dialogView.findViewById(R.id.spinner_moods);
        EditText editKeyword = dialogView.findViewById(R.id.edit_keyword);

        editKeyword.setHintTextColor(Color.LTGRAY);  // Light gray for hint text for contrast
        editKeyword.setTextColor(Color.WHITE);
        spinnerMoods.setBackgroundColor(Color.GRAY);

        // Set custom title with white text color
        TextView titleTextView = new TextView(this);
        titleTextView.setText("Filter by");
        titleTextView.setTextColor(Color.WHITE);
        titleTextView.setTextSize(18);
        titleTextView.setPadding(350, 50, 16, 16);  //Add padding for better look

        // Get the mood options from the string array
        String[] moods = getResources().getStringArray(R.array.moods_array);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, moods);
        spinnerMoods.setAdapter(adapter);

        // Show/hide spinner and edit text based on checkboxes
        checkFilterMood.setOnCheckedChangeListener((buttonView, isChecked) ->
                spinnerMoods.setVisibility(isChecked ? View.VISIBLE : View.GONE)
        );

        checkFilterKeyword.setOnCheckedChangeListener((buttonView, isChecked) ->
                editKeyword.setVisibility(isChecked ? View.VISIBLE : View.GONE)
        );

        // Build the dialog with the custom style
        new MaterialAlertDialogBuilder(this, R.style.CustomDialogStyle)  // Apply custom style here
                .setCustomTitle(titleTextView)  // Set the custom title here
                .setView(dialogView)
                .setPositiveButton("Apply", (dialog, which) -> {
                    // Fetches mood history to reset the list
                    if (checkRecentWeek.isChecked()) {
                        filterByRecentWeek();
                    }
                    if (checkFilterMood.isChecked()) {
                        String selectedMood = spinnerMoods.getSelectedItem().toString();
                        filterByEmotionalState(selectedMood);
                    }
                    if (checkFilterKeyword.isChecked()) {
                        String keyword = editKeyword.getText().toString().trim();
                        if (!keyword.isEmpty()) {
                            filterByKeyword(keyword);
                        }
                    }
                    if (checkNearbyFollowing.isChecked() && checkNearbyFollowing!= null) {
                        filterByNearbyFollowing();
                    }
                })
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Reset", (dialog, which) -> {
                    if (feedScreen) {
                        // Fetches the mood history of the user and updates the markers
                        fetchMoodFeed(currentUser);
                    } else {
                        // Fetches the mood feed and updates the markers
                        fetchMoodHistory(currentUser);
                    }
                })
                .show();

    }

    private void filterByNearbyFollowing() {
        // start by getting the users current location
        LocationPermissionFragment locationFragment = new LocationPermissionFragment();
        locationFragment.show(getSupportFragmentManager(), "LocationPermissionFragment");

        locationFragment.setOnPermissionGrantedListener(() -> {
            locationFragment.startTrackingLocation();

            locationFragment.getLastKnownLocation(new LocationPermissionFragment.OnLocationReceivedListener() {
                @Override
                public void onLocationReceived(Location location) {
                    // get the users following list
                    feedManager.getFollowing(currentUser, new FeedManager.FollowingCallback() {
                        @Override
                        public void onCallback(ArrayList<String> following) {
                            feedManager.fetchFeed(following, new FeedManager.FeedCallback() {
                                @Override
                                public void onCallback(ArrayList<MoodState> feed) {
                                    // call the filter function
                                    moodHistory = Filter.filterBy5kDistance(
                                            feed, location, following, radius_in_km, currentUser);

                                    // update the map to reflect this
                                    updateMapMarkers();

                                    // The number of moods found should be informed to the user
                                    Toast.makeText(LocationMapActivity.this,
                                            moodHistory.size() + " nearby mood events found",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }

                @Override
                public void onLocationFailure(String errorMessage) {
                    Toast.makeText(LocationMapActivity.this,
                            "Failed to get your location: " + errorMessage,
                            Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    /**
     * Closes the view to conserve memory
     */
    protected void onPause(){
        super.onPause();
        finish();
    }
}