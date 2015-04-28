package com.example.malika.smartlighting.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.example.malika.smartlighting.R;
import com.example.malika.smartlighting.dao.DatabaseHelper;
import com.example.malika.smartlighting.dao.ScheduleDao;
import com.example.malika.smartlighting.model.Schedule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Malika (mxp134930) on 4/1/2015.
 */
public class ScheduleAdapter extends BaseAdapter implements ListAdapter {
    private List<Schedule> schedules = new ArrayList<Schedule>();
    private Context context;
    private ScheduleDao scheduleDao;


    public ScheduleAdapter(List<Schedule> scheduleList, Context context, ScheduleDao scheduleDao) {
        this.schedules = scheduleList;
        this.context = context;
        this.scheduleDao = scheduleDao;
    }

    @Override
    public int getCount() {
        return schedules.size();
    }

    @Override
    public Schedule getItem(int position) {
        return schedules.get(position);
    }

    @Override
    public long getItemId(int position) {
        return schedules.get(position).getId();
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.schedule, null);
        }

        final Schedule schedule = getItem(position);

        Switch mySwitch = (Switch) view.findViewById(R.id.onOff);
        mySwitch.setOnCheckedChangeListener(null);

        mySwitch.setChecked(schedule.isActive());

        TextView listItemText = (TextView) view.findViewById(R.id.scheduleText);

        listItemText.setText(schedule.getHours() + " : " + schedule.getMinutes() + " - " + schedule.getLuminosity());
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                schedule.setActive(isChecked);
                scheduleDao.updateSchedule(schedule.getId(), schedule);
            }
        });

        return view;
    }

}
