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

    public void setOnPermissionGrantedListener(OnPermissionGrantedListener listener) {
        this.permissionGrantedListener = listener;
    }

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG, "Location permission granted.");
                    if (permissionGrantedListener != null) {
                        permissionGrantedListener.onPermissionGranted();
                    }
                    dismiss(); // ✅ Close fragment only after handling permission
                } else {
                    Log.w(TAG, "Location permission denied by the user.");
                }
            });

    public LocationPermissionFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Fragment created.");
        View view = inflater.inflate(R.layout.fragment_location_permission, container, false);

        permissionMessage = view.findViewById(R.id.permissionMessage);
        Button allowButton = view.findViewById(R.id.allowButton);
        Button denyButton = view.findViewById(R.id.denyButton);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        allowButton.setOnClickListener(v -> requestLocationPermission());
        denyButton.setOnClickListener(v -> dismiss());

        return view;
    }

    /**
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
            dismiss(); // ✅ Close fragment only if permission is already granted
        }
    }

    /**
     * Starts location tracking and updates the last known location.
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

        Log.d(TAG, "Requesting location updates...");
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    /**
     * Retrieves the last known location and passes it to the callback listener.
     */
    public void getLastKnownLocation(OnLocationReceivedListener listener) {
        Log.d(TAG, "Fetching last known location...");

        if (lastKnownLocation != null) {
            Log.d(TAG, "Using last known location from memory.");
            listener.onLocationReceived(lastKnownLocation);
            return;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Permission not granted, cannot fetch location.");
            listener.onLocationFailure("Permission not granted.");
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        Log.d(TAG, "Last known location: Lat=" + location.getLatitude() + ", Lng=" + location.getLongitude());
                        lastKnownLocation = location; // Cache the location
                        listener.onLocationReceived(location);
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