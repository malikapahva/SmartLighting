package com.example.malika.smartlighting.activity;

import android.app.Activity;
import android.widget.SeekBar;
import android.widget.TimePicker;

import com.example.malika.smartlighting.R;
import com.example.malika.smartlighting.model.Schedule;

/**
 * Created by Malika (mxp134930) on 4/1/2015.
 */
public class ScheduleGenerator {
    private Activity activity;

    public ScheduleGenerator(Activity activity) {
        this.activity = activity;
    }


    public Schedule generate() {
        TimePicker timePicker = (TimePicker) activity.findViewById(R.id.timePicker);
        int hours = timePicker.getCurrentHour();
        int minutes = timePicker.getCurrentMinute();
        SeekBar seekBar = (SeekBar) activity.findViewById(R.id.setLuminosity);
        int luminosity = seekBar.getProgress();
        return new Schedule(hours, minutes, luminosity);
    }


}
