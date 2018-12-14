package com.zzteck.msafe.location;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

/**
 * Created by Administrator on 2018/10/22 0022.
 */

public class LocationUtils {

    private static LocationUtils mInstance;

    private Activity mContext;


    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    private Location mCurrentLocation;

    private LocationCallback mLocationCallback;

    private LocationRequest mLocationRequest;

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private LocationSettingsRequest mLocationSettingsRequest;

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mCurrentLocation = locationResult.getLastLocation();
                //Log.e("liujw", "############################createLocationCallback : " + locationResult.getLastLocation().getLongitude() + "  #####################lat : " + locationResult.getLastLocation().getLatitude());
                getAddress();
            }
        };
    }

   /* *//**
     * Handles the Start Updates button and requests start of location updates. Does nothing if
     * updates have already been requested.
     *//*
    public void startUpdatesButtonHandler(Activity context) {
        startLocationUpdates(context);
    }

    */

    /**
     * Handles the Stop Updates button, and requests removal of location updates.
     *//*
    public void stopUpdatesButtonHandler(Activity context) {
        stopLocationUpdates(context);
    }*/
    public void stopLocationUpdates(Activity context) {

        // It is a good practice to remove location requests when the activity is in a paused or
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(context, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //setButtonsEnabledState();
                    }
                });

        // liujianwei 解决内存泄漏问题 LocationUtil mInstance ; mContext
        this.mContext = null;
        mInstance = null ;
    }


    /**
     * Represents a geographical location.
     */
    private Location mLastLocation;

    private SettingsClient mSettingsClient;

    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    private LocationUtils(Activity context) {
        this.mContext = context;
        mSettingsClient = LocationServices.getSettingsClient(context);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
    }


    public static LocationUtils getmInstance(Activity context) {
        if (mInstance == null) {
            mInstance = new LocationUtils(context);
        }
        return mInstance;
    }

    private void startLocationUpdates(final Activity context) {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(context, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i("liujw", "All location settings are satisfied.");

                        //noinspection MissingPermission
                      /*  if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }*/
                        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

                    }
                })
                .addOnFailureListener(context, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i("liujw", "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(context, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i("liujw", "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e("liujw", errorMessage);
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                             //   mRequestingLocationUpdates = false;
                        }
                    }
                });
    }

    private boolean isInstallGoogleService(Activity mContext){
        boolean googleserviceFlag = true;
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(mContext);
        if(resultCode != ConnectionResult.SUCCESS) {
            if(googleApiAvailability.isUserResolvableError(resultCode)) {
               // dialog.show() ;
                /*googleApiAvailability.getErrorDialog(mContext, resultCode, 2404).show();
                googleApiAvailability.*/
            }
            googleserviceFlag = false;
        }
        return googleserviceFlag ;

    }

    public void requestLocation(Activity context){

        if(!isInstallGoogleService(context)){
            return ;
        }

        if (!checkPermissions()) {
            requestPermissions();
        } else {
            startLocationUpdates(mContext);
        }
    }

    private void startIntentService() {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(mContext, FetchAddressIntentService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(LocationConstants.RECEIVER, new AddressResultReceiver(new Handler()));

        // Pass the location data as an extra to the service.
        intent.putExtra(LocationConstants.LOCATION_DATA_EXTRA, mLastLocation);

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mContext.startForegroundService(intent);
            //mContext.startService(intent);
        }else{
            mContext.startService(intent);
        }
    }

   /* private void startIntentService() {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(mContext, FetchAddressIntentService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(LocationConstants.RECEIVER, new AddressResultReceiver(new Handler()));

        // Pass the location data as an extra to the service.
        intent.putExtra(LocationConstants.LOCATION_DATA_EXTRA, mLastLocation);

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        mContext.startService(intent);
    }*/

    /**
     * Gets the address for the last known location.
     */
    @SuppressWarnings("MissingPermission")
    public void getAddress() {


        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(mContext, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location == null) {
                            return;
                        }

                        Log.e("liujw","##########################mLastLocation : "+mLastLocation);

                        mLastLocation = location;

                        // Determine whether a Geocoder is available.
                        if (!Geocoder.isPresent()) {

                            return;
                        }

                        // If the user pressed the fetch address button before we had the location,
                        // this will be set to true indicating that we should kick off the intent
                        // service after fetching the location.
                       startIntentService();
                    }
                })
                .addOnFailureListener(mContext, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }


    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(mContext,Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =  ActivityCompat.shouldShowRequestPermissionRationale(mContext,Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {

            ActivityCompat.requestPermissions(mContext,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);

        } else {
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(mContext,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }


}
