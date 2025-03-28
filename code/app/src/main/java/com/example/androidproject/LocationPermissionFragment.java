package com.example.androidproject;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

/**
 * Fragment for requesting location permissions and retrieving location data.
 */
public class LocationPermissionFragment extends DialogFragment {
    private static final String TAG = "LocationPermissionFragment";
    private TextView permissionMessage;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private static Location lastKnownLocation = null; // Stores last known location
    private OnPermissionGrantedListener permissionGrantedListener;

    //creates event for permissions for callback when location is granted
    public void setOnPermissionGrantedListener(OnPermissionGrantedListener listener) {
        this.permissionGrantedListener = listener;
    }
    //creates second location permission seen on the initial popup using android api
    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG, "Location permission granted.");
                    if (permissionGrantedListener != null) {
                        permissionGrantedListener.onPermissionGranted();
                    }
                    dismiss();
                } else {
                    Log.w(TAG, "Location permission denied by the user.");
                }
            });

    public LocationPermissionFragment() {}

    /**
     * Inflates the layout and sets up button listeners.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Fragment created.");
        View view = inflater.inflate(R.layout.fragment_location_permission, container, false);

        permissionMessage = view.findViewById(R.id.permissionMessage);
        Button allowButton = view.findViewById(R.id.allowButton);
        Button denyButton = view.findViewById(R.id.denyButton);

        //creates client to access location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        allowButton.setOnClickListener(v -> requestLocationPermission());
        denyButton.setOnClickListener(v -> dismiss());

        return view;
    }

    /**
     * checks if location permission is granted
     * Requests location permission if not already granted.
     */
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Requesting location permission.");
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            Log.d(TAG, "Permission already granted.");
            if (permissionGrantedListener != null) {
                permissionGrantedListener.onPermissionGranted();
            }
            dismiss();
        }
    }

    /**
     * Starts location tracking and updates the last known location.
     * checks for permissions and periodically update the last known location
     * every 5 seconds
     */
    public void startTrackingLocation() {
        Log.d(TAG, "Starting location tracking...");
        if (getContext() == null) return;

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Cannot start tracking: Permission not granted.");
            return;
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(0);

        //callback to get the last known location every time it updates
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        Log.d(TAG, "Location received: Lat=" + location.getLatitude() + ", Lng=" + location.getLongitude());
                        lastKnownLocation = location; // Store last known location
                    } else {
                        Log.e(TAG, "Received location result but location is NULL.");
                    }
                } else {
                    Log.e(TAG, "Location result is NULL.");
                }
            }
        };
        //request for continuous updates
        Log.d(TAG, "Requesting location updates...");
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    /**
     * Retrieves the last known location and passes it to the callback listener.
     */
    public void getLastKnownLocation(OnLocationReceivedListener listener) {
        Log.d(TAG, "Fetching last known location...");
        //return cached location
        if (lastKnownLocation != null) {
            Log.d(TAG, "Using last known location from memory.");
            listener.onLocationReceived(lastKnownLocation);
            return;
        }
        //checks permissions again before retrieval
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Permission not granted, cannot fetch location.");
            listener.onLocationFailure("Permission not granted.");
            return;
        }
        //fetches last known location from the the client
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        Log.d(TAG, "Last known location: Lat=" + location.getLatitude() + ", Lng=" + location.getLongitude());
                        lastKnownLocation = location; // Cache the location
                        listener.onLocationReceived(location);//passes it here
                    } else {
                        Log.e(TAG, "No last known location available.");
                        listener.onLocationFailure("No location found.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching last known location: " + e.getMessage());
                    listener.onLocationFailure("Error fetching location: " + e.getMessage());
                });
    }

    /**
     * Interface to handle location retrieval.
     */
    public interface OnLocationReceivedListener {
        void onLocationReceived(Location location);
        void onLocationFailure(String errorMessage);
    }

    /**
     * Interface to notify CreatePostActivity when permission is granted.
     */
    public interface OnPermissionGrantedListener {
        void onPermissionGranted();
    }
}