package com.example.androidproject;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import com.google.android.gms.location.LocationRequest;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.*;
import com.google.firebase.firestore.GeoPoint;
/**
 * This class helps in obtaining the user's location using Google's FusedLocationProviderClient.
 * It requests location updates at a specified interval and provides callbacks for location updates or errors.
 */
public class LocationHelper {
    private final Activity activity;
    private final FusedLocationProviderClient fusedLocationClient;
    private final LocationListener listener;
    private LocationCallback locationCallback;

    /**
     * Interface to provide callbacks for location updates or errors.
     */
    public interface LocationListener {
        void onLocationReceived(GeoPoint geoPoint);
        void onLocationError(String error);
    }

    /**
     * Constructs a LocationHelper instance.
     *
     * @param activity The activity context.
     * @param listener The listener for location updates and errors.
     */

    public LocationHelper(Activity activity, LocationListener listener) {
        this.activity = activity;
        this.listener = listener;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    /**
     * Starts requesting location updates. If location permission is not granted,
     * it requests permission and notifies the listener of an error.
     */
    public void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1001 // Request code
            );
            listener.onLocationError("Location permission not granted");
            return;
        }

        // Define the location request
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)  // Request location every 5 seconds
                .setFastestInterval(2000); // Minimum time between updates

        // Define the location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        listener.onLocationReceived(geoPoint);
                    } else {
                        listener.onLocationError("Received location is null");
                    }
                }
            }
        };

        // Start requesting location updates
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }


    /**
     * Stops location updates to prevent unnecessary battery consumption.
     */
    public void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}
