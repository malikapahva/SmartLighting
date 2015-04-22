package com.example.malika.smartlighting.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.malika.smartlighting.R;
import com.example.malika.smartlighting.dao.DatabaseHelper;
import com.example.malika.smartlighting.dao.ScheduleDao;
import com.example.malika.smartlighting.model.Schedule;

import java.util.ArrayList;
import java.util.List;


public class ScheduleInfo extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedulelist);
        ScheduleDao scheduleDao = new ScheduleDao(new DatabaseHelper(this));
        List<Schedule> schedules = scheduleDao.getAllSchedules();
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent displayScheduleIntent = new Intent();
                displayScheduleIntent.setClassName("com.example.malika.smartlighting", "com.example.malika.smartlighting.activity.DisplaySchedule");
                displayScheduleIntent.putExtra("scheduleId", id);
                startActivity(displayScheduleIntent);
                finish();
            }
        });
        addSchedulesToList(schedules);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void addSchedulesToList(List<Schedule> schedules){
        ListView listView = (ListView) findViewById(R.id.listView);
        ScheduleAdapter scheduleAdapter = new ScheduleAdapter(schedules, this);
        listView.setAdapter(scheduleAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add) {
            Intent i = new Intent();
            i.setClassName("com.example.malika.smartlighting", "com.example.malika.smartlighting.activity.AddSchedule");
            startActivity(i);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
