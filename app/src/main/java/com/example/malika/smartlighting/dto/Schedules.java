package com.example.malika.smartlighting.dto;

import com.example.malika.smartlighting.model.Schedule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Malika (mxp134930) on 4/26/2015.
 */
public class Schedules {

    private List<Schedule> schedules;

    public Schedules() {
        schedules = new ArrayList<>();
    }

    public Schedules(List<Schedule> schedules) {
        this.schedules = schedules;
    }

    public List<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
    }

}
