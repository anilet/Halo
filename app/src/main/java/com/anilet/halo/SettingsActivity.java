package com.anilet.halo;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Settings");

        if(findViewById(R.id.fragment_container) !=null)
        {
            if(savedInstanceState !=null)
                return;
            getFragmentManager().beginTransaction().add(R.id.fragment_container, new SettingsFragment()).commit();
            ///getFragmentManager().beginTransaction().add(R.id.fragment_container, new SettingsFragment()).commit();
           // getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new SettingsFragment()).commit();
        }
    }

}
