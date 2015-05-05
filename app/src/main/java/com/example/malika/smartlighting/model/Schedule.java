package com.example.malika.smartlighting.model;

import android.widget.Switch;

/**
 * Created by Malika (mxp134930) on 4/1/2015.
 */
public class Schedule {
    private long id;
    private boolean active;
    private int hours;
    private int minutes;
    private int luminosity;

    public Schedule() {
    }

    public Schedule(boolean active, int hours, int minutes, int luminosity) {
        this.active = active;
        this.hours = hours;
        this.minutes = minutes;
        this.luminosity = luminosity;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getLuminosity() {
        return luminosity;
    }

    public void setLuminosity(int luminosity) {
        this.luminosity = luminosity;
    }
}
