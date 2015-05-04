package com.example.malika.smartlighting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.malika.smartlighting.R;
import com.example.malika.smartlighting.dao.DatabaseHelper;
import com.example.malika.smartlighting.dao.ScheduleDao;
import com.example.malika.smartlighting.dto.Schedules;
import com.example.malika.smartlighting.model.Schedule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;


public class ScheduleInfo extends ActionBarActivity implements ClientThread.ClientInterface {
    SmartClient client;
    ObjectMapper objectMapper = new ObjectMapper();
    ScheduleDao scheduleDao = new ScheduleDao(new DatabaseHelper(this));


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedulelist);
        client = Singleton.getInstance().client;
        List<Schedule> schedules = scheduleDao.getAllSchedules();
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent displayScheduleIntent = new Intent();
                displayScheduleIntent.setClassName("com.example.malika.smartlighting", "com.example.malika.smartlighting.activity.EditSchedule");
                displayScheduleIntent.putExtra("scheduleId", id);
                startActivity(displayScheduleIntent);
                finish();
            }
        });
        addSchedulesToList(schedules);
    }

    private void sendToPie(String jsonSchedules) {
        if(client.isConnected()) {
            ClientThread thread = new ClientThread(this, client);
            thread.execute(ClientThread.SEND, SmartClient.SCHEDULE + " " + jsonSchedules);
        }
        else {
            noConnection();
        }
        System.out.println(jsonSchedules);
    }

    @Override
    public void update(String result) {

    }

    @Override
    public void noConnection() {
        Toast.makeText(this, "You are not connected to the server. Try restarting the app.", Toast.LENGTH_LONG).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_send, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void addSchedulesToList(List<Schedule> schedules){
        ListView listView = (ListView) findViewById(R.id.listView);
        ScheduleAdapter scheduleAdapter = new ScheduleAdapter(schedules, this, scheduleDao);
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

        if(item.getItemId() == R.id.send){
            List<Schedule> allActiveSchedules = scheduleDao.getAllActiveSchedules();
            Schedules input = new Schedules(allActiveSchedules);
            try {
                String jsonSchedules = objectMapper.writeValueAsString(input);
                sendToPie(jsonSchedules);
                String message = allActiveSchedules.isEmpty() ? "Nothing to send to Pie" : "Schedules successfully sent to Pie, no. of schedules - " + allActiveSchedules.size();
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
