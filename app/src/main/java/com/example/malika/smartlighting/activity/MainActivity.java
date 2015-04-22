package com.example.malika.smartlighting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.example.malika.smartlighting.R;


public class MainActivity extends ActionBarActivity {
    SmartClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainpage);
        client = Singleton.getInstance().client;

        final Button schedule = (Button) findViewById(R.id.scheduleButton);
        schedule.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent();
                i.setClassName("com.example.malika.smartlighting", "com.example.malika.smartlighting.activity.ScheduleInfo");
                startActivity(i);
            }
        });
        SeekBar seekBar = (SeekBar) findViewById(R.id.luminosity);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        outState.putParcelable("client", client);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }
}
