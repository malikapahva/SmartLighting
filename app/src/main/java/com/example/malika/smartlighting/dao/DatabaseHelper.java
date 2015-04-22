package com.example.malika.smartlighting.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Malika (mxp134930) on 4/1/2015.
 */
public class DatabaseHelper  extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "smartlighting.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        System.out.println("Running Query : " + ScheduleDao.TABLE_CREATE);
        sqLiteDatabase.execSQL(ScheduleDao.TABLE_CREATE);
        System.out.println("Table has created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        System.out.println("Running upgrade query");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ScheduleDao.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
