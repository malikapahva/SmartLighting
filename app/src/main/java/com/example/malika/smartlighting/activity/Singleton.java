package com.example.malika.smartlighting.activity;

/**
 * Created by Zach on 4/11/2015.
 */
public class Singleton {

    //This singleton
    private static Singleton instance;

    //The client holding the connection
    public static SmartClient client;

    //Return this singleton (or create a new one)
    public static Singleton getInstance()
    {
        if (instance == null)
        {
            // Create the instance
            instance = new Singleton();
        }

        // Return the instance
        return instance;
    }

}
