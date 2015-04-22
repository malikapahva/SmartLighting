package com.example.malika.smartlighting.activity;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;

import com.example.malika.smartlighting.R;


public class Dashboard extends Fragment {

    DashboardInterface listener;
    public SmartClient client;

    public static Dashboard newInstance() {
        Dashboard fragment = new Dashboard();
        Bundle args = new Bundle();
        return fragment;
    }

    public Dashboard() {
        //Required
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.mainpage, container, false);


        final Button schedule = (Button) view.findViewById(R.id.scheduleButton);
        schedule.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Switch to schedule info
                listener.schedule();
            }
        });

        SeekBar seekBar = (SeekBar) view.findViewById(R.id.luminosity);

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

        return view;
    }



    /////////////
    //Interface//
    /////////////

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (DashboardInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ConnectInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    //implement this interface and it functions in main if you want the fragment to send data to the
    //main activity
    public interface DashboardInterface {

        public void schedule();

    }

}
