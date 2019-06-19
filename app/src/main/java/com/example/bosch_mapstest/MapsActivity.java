package com.example.bosch_mapstest;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 123;
    private static final float DEFAULT_ZOOM = 15f;
    private static final double CENTER_LOCATION_OFFSET_TO_NORTH = 0.004;

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest mLocationRequest;
    private Marker mMarker;
    private MarkerOptions mMarkerOptions;

    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Toast.makeText(MapsActivity.this, "New position received", Toast.LENGTH_SHORT).show();
                    updateMarkerPosition(location);
                }
            }
        };

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void updateMarkerPosition(Location location) {
        LatLng newLocation = new LatLng(location.getLatitude(), location.getLongitude());

        mMarker.setPosition(newLocation);
        LatLng centerLocationWithOffset = new LatLng(newLocation.latitude + CENTER_LOCATION_OFFSET_TO_NORTH, newLocation.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerLocationWithOffset, DEFAULT_ZOOM));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getLocationPermission();
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
//            boolean isLocationEnabled = isLocationEnabled(this);
            drawLastKnownOwnLocation();
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }

//        boolean isLocationEnabled = isLocationEnabled(this);
        drawLastKnownOwnLocation();
        startLocationUpdates();
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }

    private void drawLastKnownOwnLocation() {
        if (mMap == null) {
            return;
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            drawMarker(location);
                        }
                    }
                });
    }

    private void drawMarker(Location newLocation) {
        LatLng newLocationLatLng = new LatLng(newLocation.getLatitude(), newLocation.getLongitude());

        if(mMarkerOptions == null) {
            Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("truck", "drawable", getPackageName()));
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, 100, 100, false);
            mMarkerOptions = new MarkerOptions().position(newLocationLatLng).title("LOCATION").icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap));
        }


        mMarker = mMap.addMarker(mMarkerOptions);
        LatLng dispositionFromCenter = new LatLng(newLocationLatLng.latitude + CENTER_LOCATION_OFFSET_TO_NORTH, newLocationLatLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dispositionFromCenter, DEFAULT_ZOOM));
    }

    @Override
    protected void onResume() {
        super.onResume();
//        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }

        mFusedLocationClient.requestLocationUpdates(new LocationRequest()
                        .setInterval(5000)
                        .setFastestInterval(1000)
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY),
                locationCallback,
                null /* Looper */);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    }
}
