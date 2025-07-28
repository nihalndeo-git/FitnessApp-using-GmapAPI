package com.example.gmapapi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap gMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private Location previousLocation;
    private float totalDistance = 0f;
    private long startTime;
    private Button startRunButton, endRunButton, showSummaryButton;
    private boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startRunButton = findViewById(R.id.start_run_button);
        endRunButton = findViewById(R.id.end_run_button);
        showSummaryButton = findViewById(R.id.show_summary_button);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.id_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up button listeners
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        startRunButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRun();
            }
        });

        endRunButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endRun();
            }
        });

        showSummaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRunSummary();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableUserLocation();
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                }
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        gMap.setMyLocationEnabled(true);
    }

    private void startRun() {
        isRunning = true;
        startRunButton.setVisibility(View.GONE);
        endRunButton.setVisibility(View.VISIBLE);
        showSummaryButton.setVisibility(View.GONE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng startLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                gMap.addMarker(new MarkerOptions().position(startLatLng).title("Start Location"));
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLatLng, 15));
            }
        });

        startTime = SystemClock.elapsedRealtime();
        requestLocationUpdates();
    }

    private void endRun() {
        isRunning = false;
        endRunButton.setVisibility(View.GONE);
        startRunButton.setVisibility(View.VISIBLE);
        showSummaryButton.setVisibility(View.VISIBLE);

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

        if (previousLocation != null) {
            LatLng endLatLng = new LatLng(previousLocation.getLatitude(), previousLocation.getLongitude());
            gMap.addMarker(new MarkerOptions().position(endLatLng).title("End Location"));
        }
    }

    private void showRunSummary() {
        long totalTime = (SystemClock.elapsedRealtime() - startTime) / 1000;

        float stepLength = 0.78f;

        float caloriesBurned = totalDistance * 0.05f;
        int stepsTaken = (int) (totalDistance / stepLength);

        Intent intent = new Intent(MainActivity.this, RunSummaryActivity.class);
        intent.putExtra("totalDistance", totalDistance);
        intent.putExtra("totalTime", totalTime);
        intent.putExtra("caloriesBurned", caloriesBurned);
        intent.putExtra("stepsTaken", stepsTaken);
        startActivity(intent);
    }

    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                Location currentLocation = locationResult.getLastLocation();
                LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                if (isRunning && previousLocation != null) {
                    float distance = previousLocation.distanceTo(currentLocation);
                    totalDistance += distance;

                    gMap.addPolyline(new PolylineOptions().add(
                            new LatLng(previousLocation.getLatitude(), previousLocation.getLongitude()),
                            currentLatLng
                    ).width(10).color(ContextCompat.getColor(MainActivity.this, R.color.bluebutbetter)));
                }

                previousLocation = currentLocation;
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation();
            }
        }
    }
}