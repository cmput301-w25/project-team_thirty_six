package com.example.androidproject;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Allows the user to select a location
 */
public class LocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker selectedMarker;
    private Button confirmButton;
    private LatLng initialLocation;
    private static final float DEFAULT_ZOOM = 15f;

    /**
     * Main body of the location picker
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);

        // original location if it exists
        if (getIntent().hasExtra("latitude") && getIntent().hasExtra("longitude")) {
            double lat = getIntent().getDoubleExtra("latitude", 0);
            double lng = getIntent().getDoubleExtra("longitude", 0);
            initialLocation = new LatLng(lat, lng);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // The confirm button
        confirmButton = findViewById(R.id.confirm_location_button);
        confirmButton.setOnClickListener(v -> confirmLocationSelection());
    }

    /**
     * Once the map is ready to use let the user select a location
     * @param googleMap
     *      Map dislpay
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Start at the existing location incase user wants to traverse the vicinity
        if (initialLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, DEFAULT_ZOOM));
            selectedMarker = mMap.addMarker(new MarkerOptions().position(initialLocation).title("Selected Location"));
            confirmButton.setEnabled(true); // Enable the button
        } else {
            // get current location otherwise
            tryGetCurrentLocation();
        }
        confirmButton = findViewById(R.id.confirm_location_button);
        confirmButton.setEnabled(false); // makes it greyed out to look disabled
        confirmButton.setAlpha(0.5f);


        // Set up map click listener
        mMap.setOnMapClickListener(latLng -> {
            if (selectedMarker != null) {
                selectedMarker.remove();
            }
            selectedMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            confirmButton.setEnabled(true);
            confirmButton.setAlpha(1.0f); // gives it full color to look accesible
        });
    }

    /**
     * Gets the users current location
     */
    private void tryGetCurrentLocation() {
        LocationPermissionFragment locationFragment = new LocationPermissionFragment();
        locationFragment.show(getSupportFragmentManager(), "LocationPermissionFragment");

        locationFragment.setOnPermissionGrantedListener(() -> {
            locationFragment.startTrackingLocation();

            locationFragment.getLastKnownLocation(new LocationPermissionFragment.OnLocationReceivedListener() {
                @Override
                public void onLocationReceived(Location location) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM));
                }

                @Override
                public void onLocationFailure(String errorMessage) {
                    Toast.makeText(LocationPickerActivity.this,
                            "Could not get your location. Please pick a location manually.",
                            Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    /**
     * Confirms the users selection
     */
    private void confirmLocationSelection() {
        if (selectedMarker != null) {
            LatLng location = selectedMarker.getPosition();

            Location result = new Location("map-selection");
            result.setLatitude(location.latitude);
            result.setLongitude(location.longitude);
            result.setTime(System.currentTimeMillis());
            Intent resultIntent = new Intent();
            resultIntent.putExtra("latitude", location.latitude);
            resultIntent.putExtra("longitude", location.longitude);

            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "Please select a location on the map first",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
