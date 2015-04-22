package com.example.malika.smartlighting.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.malika.smartlighting.R;
import com.example.malika.smartlighting.dao.DatabaseHelper;
import com.example.malika.smartlighting.dao.ScheduleDao;
import com.example.malika.smartlighting.model.Schedule;

import java.util.List;

/**
 * Created by Malika (mxp134930) on 4/5/2015.
 */
public class DisplaySchedule extends ActionBarActivity {

    private long scheduleId;
    private Context context;
    private ScheduleDao scheduleDao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.displayschedule);
        scheduleId = getIntent().getExtras().getLong("scheduleId");
        scheduleDao = new ScheduleDao(new DatabaseHelper(this));
        Schedule schedule = scheduleDao.findScheduleById((int) scheduleId);
        if (schedule != null) {
            updateContactDetail(schedule);
        }
    }

    private void updateContactDetail(Schedule schedule) {
        TextView time = (TextView) findViewById(R.id.timeValue);
        time.setText(schedule.getHours() + " : " + schedule.getMinutes());
        TextView luminosity = (TextView) findViewById(R.id.luminosityValue);
        luminosity.setText((String.valueOf(schedule.getLuminosity())));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.editdelete, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final long id = item.getItemId();

        if (id == R.id.edit) {
            Intent i = new Intent();
            i.setClassName("com.example.malika.smartlighting", "com.example.malika.smartlighting.activity.EditSchedule");

            i.putExtra("scheduleId", scheduleId);
            startActivity(i);
            finish();
        } else if (id == R.id.delete) {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(DisplaySchedule.this);
            alertBuilder.setMessage("Are you sure you want to delete the selected schedule?");
            alertBuilder.setNegativeButton("Cancel", null);
            alertBuilder.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int position) {
                    scheduleDao.deleteSchedule(scheduleId);
                    Toast.makeText(DisplaySchedule.this, "Schedule deleted successfully", Toast.LENGTH_LONG).show();

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


