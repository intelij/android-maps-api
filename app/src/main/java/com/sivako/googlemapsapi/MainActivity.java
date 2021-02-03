package com.sivako.googlemapsapi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.google.android.gms.location.FusedLocationProviderClient;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int GPS_REQUEST_CODE = 9001;
    private static final float DEFAULT_ZOOM = 18;
    GoogleMap googleMap;
    boolean isPermissionGranted;
    FloatingActionButton floatingActionButton;
    private FusedLocationProviderClient fusedlocationProviderClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        floatingActionButton = findViewById(R.id.currentlocation);

        checkPermissions();

        initialiseMap();

        fusedlocationProviderClient = new FusedLocationProviderClient(this);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocation();
            }
        });

    }

    private void initialiseMap() {
        if (isPermissionGranted) {
            if (checkAppGPSEnabled()) {
                SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
                supportMapFragment.getMapAsync(this);
            }
        }
    }

    private boolean checkAppGPSEnabled() {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        boolean isProviderEnabled = locationManager.isProviderEnabled(locationManager.GPS_PROVIDER);

        if (isProviderEnabled) {
            return true;
        }

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("GPS Permission Required")
                .setMessage("Please enable the GPS permissions for this App")
                .setPositiveButton("Yes", ((dialogInterface, i) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, GPS_REQUEST_CODE);
                })).setCancelable(false)
                .show();

        return false;
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        fusedlocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
            try {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    Log.d("ERROR", "Something fishy");
                    setMapPinToLocation(location.getLatitude(), location.getLongitude());
                }
            } catch (Exception e) {
                Log.d("LOC", e.getMessage());
                Toast.makeText(this, "Device could not find default location, please configure if using Simulator", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setMapPinToLocation(double latitude, double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
        googleMap.moveCamera(cameraUpdate);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    private void checkPermissions() {

        Dexter.withContext(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                isPermissionGranted = true;

            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), "");
                intent.setData(uri);
                startActivity(intent);

            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap = googleMap;
        /**
         *  Commented this out as the location will be active and will consume battery
         *  googleMap.setMyLocationEnabled(true);
         */

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GPS_REQUEST_CODE) {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            boolean isProviderEnabled = locationManager.isProviderEnabled(locationManager.GPS_PROVIDER);

            if (isProviderEnabled) {
                Toast.makeText(this, "GPS is Enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "GPS is Disabled", Toast.LENGTH_SHORT).show();
            }


        }
    }
}