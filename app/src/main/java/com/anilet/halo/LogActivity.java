package com.anilet.halo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class LogActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Fragment fragment = new LogFragment();
    private LogRecyclerViewAdapter adapter;
    private  RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recyclerView = findViewById(R.id.list);
        // adapter = R.
       // getSupportActionBar().setTitle("Logs");

        if(findViewById(R.id.log_fragment_container) !=null)
        {
            if(savedInstanceState !=null)
                return;
           getSupportFragmentManager().beginTransaction().add(R.id.log_fragment_container, fragment).commit();
           // getFragmentManager().beginTransaction().add(R.id.log_fragment_container, new LogFragment()).commit();
        }


    }

}
