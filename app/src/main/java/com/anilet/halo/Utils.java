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

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.hypertrack.hyperlog.HyperLog;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility methods used in this app.
 */
class Utils {
    private static final String TAG = MainActivity.class.getSimpleName();
    final static String KEY_LOCATION_UPDATES_REQUESTED = "location-updates-requested";
    final static String KEY_LOCATION_UPDATES_RESULT = "location-update-result";
    final static String CHANNEL_ID = "channel_01";
    final static String KEY_OLD_LOCATION_LON = "old-location-lon";
    final static String KEY_OLD_LOCATION_LAT = "old-location-lat";
    final static String KEY_CURRENT_LOCATION = "current-location";
    final static String KEY_LATEST_TIMESTAMP ="latestTimeStamp";
    static MqttClient client;

    static void setRequestingLocationUpdates(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_LOCATION_UPDATES_REQUESTED, value)
                .apply();
    }

    static boolean getRequestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_LOCATION_UPDATES_REQUESTED, false);
    }

    static void setLatestTimeStamp(Context context, long value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putLong(KEY_LATEST_TIMESTAMP, value)
                .apply();
    }

    static long getLatestTimeStamp(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(KEY_LATEST_TIMESTAMP, 0);
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    static void sendNotification(Context context, String notificationDetails) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.putExtra("from_notification", true);
        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);
        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);
        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        // Define the notification settings.
        builder.setSmallIcon(R.drawable.logo)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.logo))
                .setColor(Color.WHITE)
                .setContentTitle("Location updated")
                .setContentText(notificationDetails)
                .setContentIntent(notificationPendingIntent);
        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);
        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW);
            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
            // Channel ID
            builder.setChannelId(CHANNEL_ID);
        }
        // Issue the notification
        mNotificationManager.notify(0, builder.build());

    }


    /**
     * Returns the title for reporting about a list of {@link Location} objects.
     *
     * @param context The {@link Context}.
     */
    static String getLocationResultTitle(Context context, Location locations) {
        String locationString = convert(locations.getLatitude(), locations.getLongitude());
        Double distance = calcDistance(getOldLocationLat(context), getOldLocationLon(context),locations.getLatitude(),locations.getLongitude());
        locationString = locationString+ " Distance: "+String.format("%.2f",distance)+"meters";
        setOldLocationLatLon(context, locations);
        return locationString;
    }

    /**
     * Returns te text for reporting about a list of  {@link Location} objects.
     */
    private static String getLocationResultText(Context context, Location location) {
        StringBuilder sb = new StringBuilder();
            sb.append(location.getLatitude());
            sb.append(",");
            sb.append(location.getLongitude());
            sb.append(",");
            sb.append(location.getAccuracy());
            sb.append(",");
            sb.append(location.getAccuracy());
        return sb.toString();
    }

    static void setLocationUpdatesResult(Context context, Location locations) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(KEY_LOCATION_UPDATES_RESULT, getLocationResultText(context, locations))
                .apply();
    }


    static void setOldLocationLatLon(Context context, Location location) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(KEY_OLD_LOCATION_LAT, String.valueOf(location.getLatitude()))
                .apply();
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(KEY_OLD_LOCATION_LON, String.valueOf(location.getLongitude()))
                .apply();
    }

    //battery
    public static int getBatteryPercentage(Context context) {

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        float batteryPct = level / (float) scale;

        return (int) (batteryPct * 100);
    }

    public static double calcDistance(String oldLat, String OldLon, double newLatitude, double newLongitude) {
        //return newLocation.distanceTo(oldLocation);
        Location selected_location=new Location("locationA");
        selected_location.setLatitude(Double.valueOf(oldLat));
        selected_location.setLongitude(Double.valueOf(OldLon));
        Location near_locations=new Location("locationB");
        near_locations.setLatitude(newLatitude);
        near_locations.setLongitude(newLongitude);
        double distance=selected_location.distanceTo(near_locations);
        return distance;
    }


    private static String convert(double latitude, double longitude) {
        StringBuilder builder = new StringBuilder();
        String latitudeDegrees = Location.convert(Math.abs(latitude), Location.FORMAT_SECONDS);
        String[] latitudeSplit = latitudeDegrees.split(":");
        builder.append(latitudeSplit[0]);
        builder.append("°");
        builder.append(latitudeSplit[1]);
        builder.append("'");
        builder.append(latitudeSplit[2]);
        builder.append("\"");

        builder.append(" ");
        if (latitude < 0) {
            builder.append("S, ");
        } else {
            builder.append("N, ");
        }


        String longitudeDegrees = Location.convert(Math.abs(longitude), Location.FORMAT_SECONDS);
        String[] longitudeSplit = longitudeDegrees.split(":");
        builder.append(longitudeSplit[0]);
        builder.append("°");
        builder.append(longitudeSplit[1]);
        builder.append("'");
        builder.append(longitudeSplit[2]);
        builder.append("\"");
        if (longitude < 0) {
            builder.append(" W");
        } else {
            builder.append(" E");
        }
        return builder.toString();
    }

    static String getLocationUpdatesResult(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_LOCATION_UPDATES_RESULT, "");
    }

    static String getOldLocationLon(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_OLD_LOCATION_LON, "0");
    }

    static String getOldLocationLat(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_OLD_LOCATION_LAT, "0");
    }

    static void publishMessage(Context context, Location locations) {

        /* {"battery_level":44,"longitude":153.1122779,"latitude":-27.8869922,"altitude":86,"gps_accuracy":22,"tst":1572752503}
         */
        int currentBatteryLevel = getBatteryPercentage(context);
        String mServerUrl = Preference.getMqttUrl(); //"ssl://homemonitor.duckdns.org:8883";//
        //String mTopic = "halo/halo_pixel3/location";
        String mTopic = "halo/"+Preference.getDeviceName()+"/location";
        HyperLog.d(TAG, "Sending location to MQTT server");
        try {
            client = new MqttClient(mServerUrl, "halo", new MemoryPersistence());
                //client.setCallback(this);
            client.connect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
            String message = "";
            if (client != null) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("battery_level", currentBatteryLevel);
                    message = "Battery level: "+currentBatteryLevel;
                    jsonObject.put("longitude", locations.getLongitude());//Float.parseFloat(lon));
                    message = message+" Lon: "+ locations.getLongitude();
                    jsonObject.put("latitude", locations.getLatitude()); //Float.parseFloat(lat));
                    message = message+" Lat: "+ locations.getLatitude();
                    jsonObject.put("altitude", locations.getAltitude()); //Float.parseFloat(alt));
                    message = message+" Alt: "+ locations.getAltitude();
                    jsonObject.put("gps_accuracy", Math.round(locations.getAccuracy())); //Integer.parseInt(acc));
                    MqttMessage mqttMessage = new MqttMessage();
                    mqttMessage.setPayload(jsonObject.toString().getBytes());
                    try {
                        client.publish(mTopic, mqttMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                HyperLog.d(TAG, "Send location: "+message);
            } else {
                Log.i(TAG, "Failed setting up");
            }
            try {
                client.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

}
