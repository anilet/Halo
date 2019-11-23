package com.anilet.halo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toolbar;

import com.baviux.homeassistant.HassWebView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.hypertrack.hyperlog.HyperLog;
import com.anilet.halo.log.CustomLogFormat;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL = 60000; // Every 60 seconds.

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value, but they may be less frequent.
     */
    private static final long FASTEST_UPDATE_INTERVAL = 30000; // Every 30 seconds

    /**
     * The max time before batched results are delivered by location services. Results may be
     * delivered sooner than this interval.
     */
    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 5; // Every 5 minutes.

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private HassWebView mWebView;
    private String mUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigationView);
        initLogger();
        HyperLog.d(TAG, "Setting up user interface ...");
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId())
                {
                    case R.id.nav_settings:
                        startSettingActivity();
                        drawerLayout.closeDrawers();
                        return true;

                    case R.id.nav_logs:
                        startLogActivity();
                        drawerLayout.closeDrawers();
                        return true;

                }

                return false;
            }
        });
        //load preferences
        Preference.load(this);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        if (!checkPermissions()) {
            requestPermissions();
        }
        setupWebView();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        requestLocationUpdates(null);
    }

    public  void startSettingActivity()
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public  void startLogActivity()
    {
        Intent intent = new Intent(this, LogActivity.class);
        startActivity(intent);
    }

    private void initLogger() {
        HyperLog.initialize(this, new CustomLogFormat(this));
        if (BuildConfig.DEBUG) {
            HyperLog.setLogLevel(Log.VERBOSE);
        } else {
            HyperLog.setLogLevel(Log.INFO);
        }
    }
    private void setupWebView(){
        if(mWebView == null) {
            mWebView = findViewById(R.id.webView);
            mWebView.setOnFinishEventHandler(new HassWebView.IOnFinishEventHandler() {
                @Override
                public void onFinish() {
                    MainActivity.this.finish();
                }
            });
        }
        String url = Preference.getHaUrl();
        if (url!=null && url.trim().length() > 0 ){
            mUrl = url;
            mWebView.loadUrl(url);
        } else {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        HyperLog.d(TAG, "Completed Setting up user interface ...");
    }

    /**
     * Handles the Request Updates button and requests start of location updates.
     */
    public void requestLocationUpdates(View view) {
        try {
            HyperLog.d(TAG, "Starting location updates");
            Utils.setRequestingLocationUpdates(this, true);
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, getPendingIntent());
        } catch (SecurityException e) {
            Utils.setRequestingLocationUpdates(this, false);
            e.printStackTrace();
        }
    }

    /**
     * Handles the Remove Updates button, and requests removal of location updates.
     */
    public void removeLocationUpdates(View view) {
        HyperLog.i(TAG, "Removing location updates");
        Utils.setRequestingLocationUpdates(this, false);
        mFusedLocationClient.removeLocationUpdates(getPendingIntent());
    }

    @Override
    public void onBackPressed() {
        // If it's handled by WebView -> it's done!
        if (mWebView != null && mWebView.onBackPressed()){
            return;
        }

        // If not, if we can go back -> let's go back!
        if (mWebView != null && mWebView.canGoBack()){
            mWebView.goBack();
            return;
        }

        // Else -> Let parent class handle it
        super.onBackPressed();
    }

    /**
     * Handles the Request Updates button and requests start of location updates.
     */
    public void requestLocationUpdates() {
        try {
            //Log.i(TAG, "Starting location updates");
            HyperLog.i(TAG, "Starting location updates");
            Utils.setRequestingLocationUpdates(this, true);
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, getPendingIntent());
        } catch (SecurityException e) {
            Utils.setRequestingLocationUpdates(this, false);
            e.printStackTrace();
        }
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        // Note: apps running on "O" devices (regardless of targetSdkVersion) may receive updates
        // less frequently than this interval when the app is no longer in the foreground.


        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.

        if (Preference.getHighPrecisionLocationEnabled()){
            HyperLog.i(TAG, "High accuracy location updates");
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        } else {
            HyperLog.i(TAG, "Passive location updates");
            mLocationRequest.setInterval(60*30*1000);
            mLocationRequest.setFastestInterval(60*1000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        }


        // Sets the maximum time when batched location updates are delivered. Updates may be
        // delivered sooner than this interval.
        mLocationRequest.setMaxWaitTime(MAX_WAIT_TIME);
        mLocationRequest.setSmallestDisplacement(10);
    }

    private PendingIntent getPendingIntent() {
        // Note: for apps targeting API level 25 ("Nougat") or lower, either
        // PendingIntent.getService() or PendingIntent.getBroadcast() may be used when requesting
        // location updates. For apps targeting API level O, only
        // PendingIntent.getBroadcast() should be used. This is due to the limits placed on services
        // started in the background in "O".

        // TODO(developer): uncomment to use PendingIntent.getService().
//        Intent intent = new Intent(this, LocationUpdatesIntentService.class);
//        intent.setAction(LocationUpdatesIntentService.ACTION_PROCESS_UPDATES);
//        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intent = new Intent(this, LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int fineLocationPermissionState = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION);

        int backgroundLocationPermissionState = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION);

        int internetPermissionState = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.INTERNET);

        return (fineLocationPermissionState == PackageManager.PERMISSION_GRANTED) &&
                (internetPermissionState == PackageManager.PERMISSION_GRANTED)&&
                (backgroundLocationPermissionState == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermissions() {

        boolean permissionAccessFineLocationApproved =
                ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;

        boolean backgroundLocationPermissionApproved =
                ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;

        boolean internetPermissionApproved =
                ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.INTERNET)
                        == PackageManager.PERMISSION_GRANTED;

        boolean shouldProvideRationale =
                permissionAccessFineLocationApproved && backgroundLocationPermissionApproved && internetPermissionApproved;

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
           // Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    findViewById(R.id.fragment_container),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[] {
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                                            Manifest.permission.INTERNET},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
           // HyperLog.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                            Manifest.permission.INTERNET},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }



    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        //HyperLog.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                //HyperLog.i(TAG, "User interaction was cancelled.");

            } else if ((grantResults[0] == PackageManager.PERMISSION_GRANTED) &&
                    (grantResults[1] == PackageManager.PERMISSION_GRANTED) &&
                    (grantResults[2] == PackageManager.PERMISSION_GRANTED)
            ) {
                // Permission was granted.
                requestLocationUpdates();

            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                Snackbar.make(
                        findViewById(R.id.drawer_layout),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }
}
