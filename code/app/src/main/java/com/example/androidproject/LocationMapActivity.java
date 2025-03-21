package com.example.androidproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.ArrayList;
import java.util.Random;

public class LocationMapActivity extends AppCompatActivity {

    private ArrayList<MoodState> moodHistory;
    private MoodHistoryManager moodHistoryManager;
    private String currentUser;
    private static final String TAG = "LocationMapActivity";
    private GoogleMap mMap;
    private boolean isMapReady = false;

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

//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.nav_bar_container, NavBarFragment.newInstance(currentUser))
//                    .commit();
//        }
        // Initialize MoodHistoryManager and list
        moodHistoryManager = new MoodHistoryManager();
        moodHistory = new ArrayList<>();

        // ✅ Load map fragment before fetching moods
        initializeMap();
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);

        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
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

                // ✅ Only update markers if the map is ready
                if (isMapReady) {
                    updateMapMarkers();
                } else {
                    Log.w(TAG, "Map is not ready yet. Markers will be added once map is loaded.");
                }
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

            for (MoodState mood : moodHistory) {
                if (mood.getLocation() != null) {  // ✅ Fix: Check if location exists
                    double lat = mood.getLocation().getLatitude();
                    double lon = mood.getLocation().getLongitude();

                    // ✅ Log every mood's location data
                    Log.d(TAG, "Mood: " + mood.getMood() + " | Lat: " + lat + " | Lon: " + lon);

                    // ✅ Ensure latitude and longitude are valid
                    if (lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180) {
                        LatLng adjustedLocation = getOffsetLocation(lat, lon);
                        mMap.addMarker(new MarkerOptions()
                                .position(adjustedLocation)
                                .title(mood.getMood()));

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

            // ✅ Move camera ONLY if there is at least one valid location
            if (hasValidLocation && firstValidLocation != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstValidLocation, 10));
            }
        }
    }
}