package com.example.malika.smartlighting.activity;

import com.example.malika.smartlighting.dto.Schedules;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


//Class for Pis, used from a terminal
class SmartPi extends SmartClient{

    //Options
    boolean testMode = false;

    //Class options
    String sensorScript = "RCTime.py";
    String motorScript = "motor.py";
    String lightScript = "light.py";


    //Class variables
    int luminosity; //0-100 = percentage, -1 = off
    Schedules schedules; //Holds the schedule of luminosities
    ObjectMapper objectMapper; //Reads JSON


    //Control variables
    boolean change;
    boolean blindsOpen;
    boolean lightOn;

    //Constructors
    SmartPi () {
        this(hostname, port);
    }

    SmartPi (String hostname, int port) {
        this.type = PI;
        this.hostname = hostname;
        this.port = port;

        luminosity = -1;
        schedules = null;
        objectMapper = new ObjectMapper();

        change = false;
        blindsOpen = false;
        lightOn = false;
    }

    //Run
    public void run()
    {
        //Connect
        while(isConnected() == false)
            connect();

        //Loop indefinitely
        while(isConnected()){


            //Wait for a command from the server
            String command = receive();

            //Server sends null = disconnect
            if(command == null)
            {
                System.out.println("Server disconnected!");
                break;
            }

            //Check for luminosity
            else if(command.indexOf(LUM) > -1)
            {
                int space = command.trim().indexOf(" ");
                if(space > -1)
                {
                    try{

                        luminosity = Integer.parseInt(command.substring(space + 1));

                        System.out.println("LUM: " + luminosity);

                        change = true;

                    }catch(NumberFormatException ex)
                    {
                        System.out.println("BAD LUM COMMAND (not an integer): " + command.substring(space + 1));
                    }
                }
                else
                    System.out.println("BAD LUM COMMAND: " + command);
            }

            //Check for Schedule
            else if(command.indexOf(SCHEDULE) > -1)
            {
                int space = command.trim().indexOf(" ");
                if(space > -1)
                {

                    try {

                        schedules = objectMapper.readValue(command.substring(space + 1), Schedules.class);

                        System.out.println("SCHED: " + schedules.getSchedules().size());

                    } catch (JsonParseException e) {
                        System.out.println("BAD SCHED COMMAND (json exception): " + command.substring(space + 1));
                        e.printStackTrace();
                    }catch (IOException e) {
                        System.out.println("BAD SCHED COMMAND (io exception): " + command.substring(space + 1));
                        e.printStackTrace();
                    }

                }
                else
                    System.out.println("BAD SCHED COMMAND: " + command);
            }

            //Print command (usually heartbeat)
            else{

                System.out.println(command);

            }

            //Return an Acknowledgement
            send("Pi: " + command);


            //Control variables
            //desire brighter? -1 = dimmer, 0 = unsure, 1 = brighter
            int brighter = 0;

            //Check cases
            while(change){

                //Query the light values
                int inside = sensor(true);
                int outside = sensor(false);


                //desired is less than current inside...
                if(luminosity < inside && brighter < 1)
                {
                    brighter = -1;

                    //...turn off the light first.
                    if(changeLight(false) == 0)
                    {
                        //...if it is off, close the blinds.
                        if(changeBlinds(false) == 0)
                        {
                            //...if it is closed, nothing more we can do.
                            break;
                        }
                        else
                        {
                            //...it should now be closed, nothing more we can do.
                            break;
                        }
                    }
                    else
                    {
                        //...it should now be off, check again.
                        continue;
                    }
                }

                //desired is greater than current...
                if(luminosity > inside && brighter > -1)
                {
                    brighter = 1;

                    if(changeBlinds(true) == 0)
                    {
                        //...if open, try the light.
                        if(changeLight(true) == 0)
                        {
                            //...if it is on, nothing more we can do.
                            break;
                        }
                        else
                        {
                            //...it should now be on, nothing more we can do.
                            break;
                        }
                    }
                    else
                    {
                        //...it should now be open, check again.
                        continue;
                    }
                }

                //Reached the end.
                break;
            }

            //TEST
            if(testMode && change)
                System.out.println("inside: " + sensor(true));

            //Wait for another update.
            change = false;

        }
    }

    //Call the sensor script
    //Pass whether the sensor is inside or outside
    //Wait for a return value of a percentage 0->100
    //if -1, sensor could not be read
    int sensor(boolean inside)
    {
        if(testMode == false)
        {
            String response = null;

            //Try to run the python function
            try{

                //Call python
                ProcessBuilder pb = new ProcessBuilder("python", sensorScript, inside + "");
                Process p = pb.start();

                //Get output from the process
                BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

                response = in.readLine().toString();

                System.out.println("Sensor: " + response);

                //parse to output
                return Integer.parseInt(response);

            }catch(IOException e)
            {
                System.out.println("Python script could not run: " + e);
            }catch(NullPointerException e)
            {
                System.err.println("Python script encountered an error.");
            }catch(NumberFormatException e)
            {
                System.err.println("Not an integer: " + response);
            }

            return -1;
        }
        else{
            int output = 25;//(int)(Math.random() * 50);

            if(blindsOpen)
                output += 25;

            if(lightOn)
                output = 90;

            return output;
        }
    }

    //Call the motor script
    //Pass the open or close command
    //Wait for a return of change or static
    //Return 1 on True (Change), 0 on False (Static), -1 on Error
    int changeBlinds(boolean open)
    {
        //No change
        if(open && blindsOpen)
            return 0;

        if(!open && !blindsOpen)
            return 0;

        if(testMode == false)
        {
            String response = null;

            //Try to run the python function
            try{

                //Call python
                ProcessBuilder pb = new ProcessBuilder("python", motorScript, open + "");
                Process p = pb.start();

                //Get output from the process
                BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

                response = in.readLine().toString();

                System.out.println("Motor: " + response);

                //parse to output
                if(Boolean.parseBoolean(response))
                {
                    //Save the change in blinds
                    blindsOpen = open;
                    return 1;
                }
                else
                    return 0; //no change

            }catch(IOException e)
            {
                System.out.println("Python script could not run: " + e);
            }catch(NullPointerException e)
            {
                System.err.println("Python script encountered an error.");
            }

            //Error
            return -1;
        }
        else{

            blindsOpen = open;

            return 1;

        }
    }

    //Call the light script
    //Pass the on or off command
    //Wait for a return of change or static
    //Return 1 on True (Change), 0 on False (Static), -1 on Error
    int changeLight(boolean on)
    {
        //No change
        if(on && lightOn)
            return 0;

        if(!on && !lightOn)
            return 0;

        if(testMode == false)
        {

            String response = null;

            //Try to run the python function
            try{

                //Call python
                ProcessBuilder pb = new ProcessBuilder("python", lightScript, on + "");
                Process p = pb.start();

                //Get output from the process
                BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

                response = in.readLine().toString();

                System.out.println("Light: " + response);

                //parse to output
                if(Boolean.parseBoolean(response))
                {
                    //Save the change to the lights
                    lightOn = on;
                    return 1;
                }
                else
                    return 0; //no change

            }catch(IOException e)
            {
                System.out.println("Python script could not run: " + e);
            }catch(NullPointerException e)
            {
                System.err.println("Python script encountered an error.");
            }

            //Error
            return -1;
        }
        else
        {

            lightOn = on;

            return 1;

        }
    }


    //Show the user how to write the arguments
    public static String help()
    {
        String output = "To run SmartPi type the following:\n"+
                "   java SmartPi [hostname [port]]\n"+
                "\n"+
                "   where hostname by default is:\n"+
                "      localhost\n"+
                "\n"+
                "   and port by default is:\n"+
                "      1024\n";

        return output;
    }



    //Run this program
    public static void main(String[] args){

        //Run with java SmartApp [hostname [port]]
        if(args.length > 0)
        {
            //Hostname
            hostname = args[0];

            //Show help
            if(hostname.indexOf("HELP") > -1 || hostname.indexOf("?") > -1)
            {
                System.out.println(help());
                System.exit(1);
            }

            if(args.length > 1)
            {
                //port number was specified
                try{
                    port = Integer.parseInt(args[1]);
                } catch(NumberFormatException e)
                {
                    System.out.println("Invalid port number: " + args[1] + help());
                    e.printStackTrace();
                }
            }
        }

        SmartPi program = new SmartPi(hostname, port);
        program.run();
    }
}