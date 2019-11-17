package com.anilet.halo;

import android.content.Context;
import android.preference.PreferenceManager;
import android.location.Location;

public class Preference {

    private static Location previousLocationInfo;
    private static Location currentLocationInfo;
    private final static  String PREF_DEVICE_NAME  = "device_name";
    private final static  String PREF_HA_ADDRESS  = "ha_address";
    private final static  String PREF_HA_PORT  = "ha_port";
    private final static  String PREF_HA_ACCESS_TOKEN  = "ha_access_token";
    private final static  String PREF_DEVICE_TRACKER = "device_tracker";

    private final static  String PREF_MQTT_ADDRESS  = "mqtt_address";
    private final static  String PREF_MQTT_PORT  = "mqtt_port";
    private final static  String PREF_MQTT_TLS  = "mqtt_tls";
    private final static  String PREF_MQTT_USERNAME  = "mqtt_username";
    private final static  String PREF_MQTT_PASSWD  = "mqtt_password";

    private final static  String PREF_LOC_HIGH_PREC  = "location_high_precision";
    private final static  String PREF_LOC_DIST  = "location_distance";
    private final static  String PREF_LOC_HEARTBEAT  = "location_heartbeat";
    private final static  String PREF_LOC_HEARTBEAT_INT  = "location_heartbeat_interval";

    private static String mDeviceName;
    private static String mHaUrl;
    private static String mHaPort;
    private static boolean mDeviceTracker;

    private static String mMqttUrl;
    private static String mMqttPort;
    private static boolean mMqttTls;
    private static String mMqttUsername;
    private static String mMqttPasswd;

    private static boolean mLocHignPrecision;
    private static String mLocDistance;
    private static boolean mLocHeartbeat;
    private static String mLocHeartbeatInterval;


    public static String getDeviceName(){ return mDeviceName; }
    public static boolean getMqttTlsEnabled(){
        return mMqttTls;
    }
    public static boolean getDevieTrackerEnabled(){
        return mDeviceTracker;
    }
    public static String getHaUrl(){
        if (mHaUrl !=null && mHaPort!=null){
            return mHaUrl+":"+mHaPort;
        }
        else return null;
    }
    public static String getMqttUrl(){ return "tcp://"+mMqttUrl+":"+mMqttPort; }
    public static String getMqttUsername(){ return mMqttUsername; }
    public static String getMqttPasswd(){ return mMqttPasswd; }

    public static boolean getHighPrecisionLocationEnabled(){
        return mLocHignPrecision;
    }
    public static String getLocDistance(){ return mLocDistance; }
    public static boolean getLocHeartbeatEnabled(){
        return mLocHeartbeat;
    }
    public static String getHeartbeatInterval(){ return mLocHeartbeatInterval; }

    public static void load(Context context){
        mDeviceName = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_DEVICE_NAME, "halo");
        mHaUrl = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_HA_ADDRESS, null);
        mHaPort = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_HA_PORT, null);
        mDeviceTracker = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_DEVICE_TRACKER, true);
        mMqttUrl = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_MQTT_ADDRESS, null);
        mMqttPort = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_MQTT_PORT, "1883");
        mMqttTls = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_MQTT_TLS, false);
        mMqttUsername = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_MQTT_USERNAME, null);
        mMqttPasswd = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_MQTT_USERNAME, null);

        mLocHignPrecision = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_LOC_HIGH_PREC, false);
        mLocDistance = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_LOC_DIST, "10");
        mLocHeartbeat = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_LOC_HEARTBEAT, false);
        mLocHeartbeatInterval = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_LOC_HEARTBEAT_INT, "120");
    }

    public static Location getPreviousLocationInfo() {
        return previousLocationInfo;
    }

    public void setPreviousLocationInfo(Location loc) {
        previousLocationInfo = loc;
    }

    public static void setCurrentLocationInfo(Location loc) {
        currentLocationInfo = loc;
    }

    /**
     * @return the Location class containing latest lat-long information
     */
    public static Location getCurrentLocationInfo() {
        return currentLocationInfo;
    }

    public static void save(Context context){
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(PREF_DEVICE_NAME, mDeviceName)
                .putString(PREF_HA_ADDRESS, mHaUrl)
                .putString(PREF_HA_PORT, mHaPort)
                .putBoolean(PREF_DEVICE_TRACKER, mDeviceTracker)
                .putString(PREF_MQTT_ADDRESS, mMqttUrl)
                .putString(PREF_MQTT_PORT, mMqttPort)
                .putBoolean(PREF_MQTT_TLS, mMqttTls)
                .putString(PREF_MQTT_USERNAME, mMqttUsername)
                .putString(PREF_MQTT_USERNAME, mMqttPasswd)
                .putBoolean(PREF_LOC_HIGH_PREC, mLocHignPrecision)
                .putString(PREF_LOC_DIST, mLocDistance)
                .putBoolean(PREF_LOC_HEARTBEAT, mLocHeartbeat)
                .putString(PREF_LOC_HEARTBEAT_INT, mLocHeartbeatInterval).apply();
    }
}