package com.example.malika.smartlighting.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.malika.smartlighting.R;
import com.example.malika.smartlighting.dao.DatabaseHelper;
import com.example.malika.smartlighting.dao.ScheduleDao;
import com.example.malika.smartlighting.model.Schedule;

/**
 * Created by Malika (mxp134930) on 4/5/2015.
 */
public class EditSchedule extends ActionBarActivity {

    private long scheduleId;
    private ScheduleDao scheduleDao;
    private ScheduleGenerator scheduleGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scheduleDao = new ScheduleDao(new DatabaseHelper(this));
        scheduleGenerator = new ScheduleGenerator(this);
        setContentView(R.layout.addschedule);
        scheduleId = getIntent().getExtras().getLong("scheduleId");
        Schedule oldSchedule = scheduleDao.findScheduleById(scheduleId);
        if (oldSchedule != null) {
            updateSchedule(oldSchedule);
        }
    }
    public void updateSchedule(Schedule schedule){
        TimePicker timePicker = (TimePicker)findViewById(R.id.timePicker);
        timePicker.setCurrentHour(schedule.getHours());
        timePicker.setCurrentMinute(schedule.getMinutes());
        SeekBar seekBar = (SeekBar)findViewById(R.id.setLuminosity);
        seekBar.setProgress(schedule.getLuminosity());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.savecancel, menu);
        inflater.inflate(R.menu.delete, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == R.id.save) {
                Schedule scheduleFromView = scheduleGenerator.generate();
                scheduleDao.updateSchedule(scheduleId, scheduleFromView);
                Toast.makeText(this, "Schedule updated successfully", Toast.LENGTH_LONG).show();
                Intent i = new Intent();
                i.setClassName("com.example.malika.smartlighting", "com.example.malika.smartlighting.activity.ScheduleInfo");
                startActivity(i);
                finish();
            }

            if (item.getItemId() == R.id.cancel){
                Intent i = new Intent();
                i.setClassName("com.example.malika.smartlighting", "com.example.malika.smartlighting.activity.ScheduleInfo");
                startActivity(i);
                finish();
            }

            if (item.getItemId() == R.id.delete) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(EditSchedule.this);
                alertBuilder.setMessage("Are you sure you want to delete the selected schedule?");
                alertBuilder.setNegativeButton("Cancel", null);
                alertBuilder.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        scheduleDao.deleteSchedule(scheduleId);
                        Toast.makeText(EditSchedule.this, "Schedule deleted successfully", Toast.LENGTH_LONG).show();

                        Intent i = new Intent();
                        i.setClassName("com.example.malika.smartlighting", "com.example.malika.smartlighting.activity.ScheduleInfo");
                        startActivity(i);
                        finish();
                    }
                });
                alertBuilder.show();
            }
            return super.onOptionsItemSelected(item);

    }
}


