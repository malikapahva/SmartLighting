package com.example.malika.smartlighting.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.malika.smartlighting.model.Schedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Malika (mxp134930) on 4/1/2015.
 */
public class ScheduleDao {
    private DatabaseHelper databaseHelper;
    public static final String TABLE_NAME = "schedule";
    private static final String ID = "id";
    private static final String ACTIVE = "active";
    private static final String HOURS = "hours";
    private static final String MINUTES = "minutes";
    private static final String LUMINOSITY = "luminosity";

    public static final String TABLE_CREATE =
            "Create table " + TABLE_NAME + "(" + ID + " INTEGER PRIMARY KEY autoincrement," + ACTIVE + " BOOLEAN ," + HOURS + " TEXT ," +
                    MINUTES + " TEXT , " + LUMINOSITY + " TEXT );";

    public ScheduleDao(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void addSchedule(Schedule schedule) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        updateContentValues(schedule, values);
        database.insert(TABLE_NAME, null, values);
        System.out.println("Schedule added Successfully......");
        database.close();
    }

    private void updateContentValues(Schedule schedule, ContentValues values) {
        values.put(ACTIVE, schedule.isActive());
        values.put(HOURS, schedule.getHours());
        values.put(MINUTES, schedule.getMinutes());
        values.put(LUMINOSITY, schedule.getLuminosity());
    }

    public Schedule findScheduleById(long id) {
            /*  SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[] { HOURS, MINUTES, LUMINOSITY },
                ID + "=", new String[] { String.valueOf(id) },
                null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        assert cursor != null;
        Schedule schedule = new Schedule(Integer.parseInt(cursor.getString(1)),
                Integer.parseInt(cursor.getString(2)), Integer.parseInt(cursor.getString(3)));

        cursor.close();
        return schedule;*/


        Cursor cursor = databaseHelper.getReadableDatabase().rawQuery("select * from " + TABLE_NAME + " where " + ID + "=" + id, null);
        try {
            if (cursor.moveToFirst()) {
                return fetchScheduleFromCursor(cursor);
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    private Schedule fetchScheduleFromCursor(Cursor cursor){
        int active = cursor.getInt(cursor.getColumnIndex(ACTIVE));
        Schedule schedule = new Schedule(
                active == 1,
                cursor.getInt(cursor.getColumnIndex(HOURS)),
                cursor.getInt(cursor.getColumnIndex(MINUTES)),
                cursor.getInt(cursor.getColumnIndex(LUMINOSITY))
        );
        schedule.setId(cursor.getInt(cursor.getColumnIndex(ID)));
        return schedule;
    }

    public List<Schedule> getAllSchedules() {
        List<Schedule> scheduleList = new ArrayList<Schedule>();
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Schedule schedule = fetchScheduleFromCursor(cursor);
                    scheduleList.add(schedule);
                } while (cursor.moveToNext());
            }
            sortSchedules(scheduleList);
            return scheduleList;
        } finally {
            cursor.close();
        }

    }

    public List<Schedule> getAllActiveSchedules() {
        List<Schedule> scheduleList = new ArrayList<Schedule>();
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Schedule schedule = fetchScheduleFromCursor(cursor);
                    if (schedule.isActive()) {
                        scheduleList.add(schedule);
                    }
                } while (cursor.moveToNext());
            }
            return scheduleList;
        } finally {
            cursor.close();
        }

    }

    public void deleteSchedule(long id){
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        database.delete(TABLE_NAME, ID + "=" + id, null);
        database.close();
    }

    public void updateSchedule(long id, Schedule newScheduleDetail) {
        ContentValues values = new ContentValues();
        updateContentValues(newScheduleDetail, values);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        database.update(TABLE_NAME, values, ID + " = " + id, null);
        database.close();
    }

    public void sortSchedules(List<Schedule> schedules){
        Collections.sort(schedules, new Comparator<Schedule>() {
            @Override
            public int compare(Schedule lhs, Schedule rhs) {
                String time1 = lhs.getHours() + " : "+  lhs.getMinutes();
                String time2 = rhs.getHours() + " : "+  rhs.getMinutes();
                return time1.compareTo(time2);
            }
        });

    }
}
