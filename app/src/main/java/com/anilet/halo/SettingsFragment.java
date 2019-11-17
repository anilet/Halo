package com.anilet.halo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import androidx.annotation.Nullable;

import java.util.prefs.Preferences;


public class SettingsFragment extends PreferenceFragment
{
    public static final  String PREF_HA_ADDRESS  = "ha_address";
    public static final  String PREF_HA_PORT  = "ha_port";
    public static final  String PREF_DEVICE_TRACKER = "device_tracker";
    public static final  String PREF_DEVICE_NAME  = "device_name";
    public static final  String PREF_MQTT_ADDRESS  = "mqtt_address";
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);


        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
            {

                if(key.equals(PREF_HA_ADDRESS))
                {
                    Preference haAddressPref = findPreference(key);
                    haAddressPref.setSummary(sharedPreferences.getString(key, ""));
                }

                if(key.equals(PREF_HA_PORT))
                {
                    Preference haPortPref = findPreference(key);
                    haPortPref.setSummary(sharedPreferences.getString(key, "8123"));
                }

              /*  if(key.equals(PREF_DEVICE_TRACKER))
                {
                    Preference deviceTrackerPref = findPreference(key);
                    deviceTrackerPref.setSummary(sharedPreferences.getBoolean(key, true));
                }*/

                if(key.equals(PREF_DEVICE_NAME))
                {
                    Preference deviceNamePref = findPreference(key);
                    deviceNamePref.setSummary(sharedPreferences.getString(key, "halo"));
                }

                if(key.equals(PREF_MQTT_ADDRESS))
                {
                    Preference mqttAddressPref = findPreference(key);
                    mqttAddressPref.setSummary(sharedPreferences.getString(key, ""));
                }

            }
        };




    }

    @Override
    public void onResume() {
        super.onResume();
       getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        Preference haAddressPref = findPreference(PREF_HA_ADDRESS);
        haAddressPref.setSummary(getPreferenceScreen().getSharedPreferences().getString(PREF_HA_ADDRESS, ""));
        Preference haPortPref = findPreference(PREF_HA_PORT);
        haPortPref.setSummary(getPreferenceScreen().getSharedPreferences().getString(PREF_HA_PORT, "8123"));
        Preference deviceNamePref = findPreference(PREF_DEVICE_NAME);
        deviceNamePref.setSummary(getPreferenceScreen().getSharedPreferences().getString(PREF_DEVICE_NAME, ""));
        Preference mqttAddressPref = findPreference(PREF_MQTT_ADDRESS);
        mqttAddressPref.setSummary(getPreferenceScreen().getSharedPreferences().getString(PREF_MQTT_ADDRESS, ""));

    }

    @Override
    public void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }
}
