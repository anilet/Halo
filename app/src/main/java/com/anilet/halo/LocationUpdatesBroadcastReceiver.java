/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anilet.halo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

import java.util.List;

/**
 * Receiver for handling location updates.
 *
 * For apps targeting API level O
 * {@link android.app.PendingIntent#getBroadcast(Context, int, Intent, int)} should be used when
 * requesting location updates. Due to limits on background services,
 * {@link android.app.PendingIntent#getService(Context, int, Intent, int)} should not be used.
 *
 *  Note: Apps running on "O" devices (regardless of targetSdkVersion) may receive updates
 *  less frequently than the interval specified in the
 *  {@link com.google.android.gms.location.LocationRequest} when the app is no longer in the
 *  foreground.
 */
public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "LUBroadcastReceiver";
    static final String ACTION_PROCESS_UPDATES =
            "com.anilet.halo.action" +
                    ".PROCESS_UPDATES";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    Location locations = result.getLastLocation();
                    onLocationChanged(context, locations);
                    Log.i(TAG, Utils.getLocationUpdatesResult(context));
                }
            }
        }
    }
    /**
     * This event is raised when the GeneralLocationListener has a new location.
     * This method in turn updates notification, writes to file, reobtains
     * preferences, notifies main service client and resets location managers.
     *
     * @param loc Location object
     */
    public void  onLocationChanged(Context context, Location loc) {

       // boolean isPassiveLocation = loc.getExtras().getBoolean(BundleConstants.PASSIVE);
        long currentTimeStamp = System.currentTimeMillis();
        // Don't log a point until the user-defined time has elapsed

        if ((currentTimeStamp - Utils.getLatestTimeStamp(context)) < (Integer.parseInt(Preference.getHeartbeatInterval()) * 1000)) {
            Log.i(TAG, "Don't log a point until the user-defined time has elapsed");
            return;
        }

        //Don't log a point if user has been still

       // if(userHasBeenStillForTooLong()) {
       //     LOG.info("Received location but the user hasn't moved, ignoring");
       //     return;
      //  }


        //Check if a ridiculous distance has been travelled since previous point - could be a bad GPS jump
        if(Preference.getCurrentLocationInfo() != null) {
            double distanceTravelled = Maths.calculateDistance(loc.getLatitude(), loc.getLongitude(), Preference.getCurrentLocationInfo().getLatitude(), Preference.getCurrentLocationInfo().getLongitude());
            long timeDifference = (int) Math.abs(loc.getTime() - Preference.getCurrentLocationInfo().getTime()) / 1000;
            if (timeDifference > 0 && (distanceTravelled / timeDifference) > 357) { //357 m/s ~=  1285 km/h
                Log.i(TAG, String.format("Very large jump detected - %d meters in %d sec - discarding point", (long) distanceTravelled, timeDifference));
                return;
            }
        }




        //Don't do anything until the user-defined distance has been traversed

      /*  if ( preferenceHelper.getMinimumDistanceInterval() > 0 && session.hasValidLocation()) {

            double distanceTraveled = Maths.calculateDistance(loc.getLatitude(), loc.getLongitude(),
                    session.getCurrentLatitude(), session.getCurrentLongitude());

            if (preferenceHelper.getMinimumDistanceInterval() > distanceTraveled) {
                LOG.warn(String.format(getString(R.string.not_enough_distance_traveled), String.valueOf(Math.floor(distanceTraveled))) + ", point discarded");
                stopManagerAndResetAlarm();
                return;
            }
        }*/

        Utils.setLatestTimeStamp(context,System.currentTimeMillis());
        //session.setFirstRetryTimeStamp(0);
        Preference.setCurrentLocationInfo(loc);
        Utils.setLocationUpdatesResult(context, loc);
        Utils.sendNotification(context, Utils.getLocationResultTitle(context, loc));
        Utils.publishMessage(context, loc);
        //setDistanceTraveled(loc);
        //showNotification();
    }

}
