package com.example.malika.smartlighting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.malika.smartlighting.R;


public class MainActivity extends ActionBarActivity implements ClientThread.ClientInterface {
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

        seekBar.setOnSeekBarChangeListener(new LuminosityListener(this));

    }

    class LuminosityListener implements SeekBar.OnSeekBarChangeListener {

        ClientThread.ClientInterface app;

        public LuminosityListener(ClientThread.ClientInterface parent )
        {
            app = parent;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

            //Start a thread to connect the client
            if(client.isConnected()) {
                ClientThread thread = new ClientThread(app, client);
                thread.execute(ClientThread.SEND, SmartClient.LUM + " " + seekBar.getProgress());
                System.out.println("Luminosity bar is " + seekBar.getProgress() + "%");
            }
            else {
                app.noConnection();
            }

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
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

    @Override
    public void update(String result) {
        System.out.println("Luminosity result: " + result);
    }

    public void noConnection() {
        Toast.makeText(this, "You are not connected to the server. Try restarting the app.", Toast.LENGTH_LONG).show();
    }
}
