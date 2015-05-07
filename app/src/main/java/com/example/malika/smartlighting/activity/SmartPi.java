package com.example.malika.smartlighting.activity;

import com.example.malika.smartlighting.dto.Schedules;
import com.example.malika.smartlighting.model.Schedule;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.List;


//Class for Pis, used from a terminal
class SmartPi extends SmartClient{

    //Options
    boolean testMode = false;
    boolean verbose = true;
    int lumThresholdLow = 1500;
    int lumThresholdMid = 1000;
    int lumThresholdHigh = 500;

    //Class options
    String sensorScript = "Rctime.py";
    String motorScript = "/home/pi/Desktop/Adafruit-Motor-HAT-Python-Library/examples/StepperTest.py";
    String lightScript = "blink.py";


    //Class variables
    int luminosity; //0-100 = percentage, -1 = off
    Schedules schedules; //Holds the schedule of luminosities
    ObjectMapper objectMapper; //Reads JSON


    //Control variables
    boolean change;
    boolean blindsOpen;
    boolean lightOn;
    int schedulePointer;

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
        blindsOpen = true;
        lightOn = false;
        schedulePointer = -1;
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

                        //Check schedule
                        schedulePointer = getNextScheduled(schedules);


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

            //Check for
            else if(command.indexOf(BLINDS) > -1)
            {
                changeBlinds(!blindsOpen);
            }

            //Check for
            else if(command.indexOf(LIGHT) > -1)
            {
                changeLight(!lightOn);
            }

            //Print command (usually heartbeat)
            else{

                System.out.println(command);

            }

            //Return an Acknowledgement
            send("Pi: " + command);


            //Check next scheduled
            if(schedulePointer == -1 && schedules != null && schedules.getSchedules().size() > 0)
            {
                schedulePointer = getNextScheduled(schedules);
            }
            else if(schedules != null
                    && Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == schedules.getSchedules().get(schedulePointer).getHours()
                    && Calendar.getInstance().get(Calendar.MINUTE) == schedules.getSchedules().get(schedulePointer).getMinutes())
            {
                luminosity = schedules.getSchedules().get(schedulePointer).getLuminosity();
                change = true;
                schedulePointer++;
                if(schedulePointer >= schedules.getSchedules().size())
                    schedulePointer = 0;

                if(verbose)
                    System.out.println("Changing to " + luminosity + "%\nNext scheduled is " + schedules.getSchedules().get(schedulePointer).getHours() + ":" + schedules.getSchedules().get(schedulePointer).getMinutes());
            }

            //Control variable
            //desire brighter? -1 = dimmer, 0 = unsure, 1 = brighter
            int brighter = 0;

            //Check cases
            while(change){

                //Query the light values
                int[] sensors = sensor();
                int inside = sensors[0];
                int outside = sensors[1];

                if(verbose)
                {
                    System.out.println("Inside: " + inside + "%");
                    System.out.println("Outside: " + outside + "%");
                }

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
                            if(verbose)
                                System.out.println("Blinds are closed.");
                            //...if it is closed, nothing more we can do.
                            break;
                        }
                        else
                        {
                            if(verbose)
                                System.out.println("Blinds are closing...");
                            //...it should now be closed, nothing more we can do.
                            break;
                        }
                    }
                    else
                    {
                        if(verbose)
                            System.out.println("Light is turning off...");
                        //...it should now be off, check again.
                        continue;
                    }
                }

                //desired is greater than current...
                if(luminosity > inside && brighter > -1)
                {
                    brighter = 1;

                    //ADD check outside sensor
                    if(changeBlinds(true) == 0)
                    {
                        //...if open, try the light.
                        if(changeLight(true) == 0)
                        {
                            if(verbose)
                                System.out.println("Light is on.");
                            //...if it is on, nothing more we can do.
                            break;
                        }
                        else
                        {
                            if(verbose)
                                System.out.println("Light is turning on...");
                            //...it should now be on, nothing more we can do.
                            break;
                        }
                    }
                    else
                    {
                        if(verbose)
                            System.out.println("Blinds are opening...");
                        //...it should now be open, check again.
                        continue;
                    }
                }

                //Reached the end.
                break;
            }

            //TEST
            if(verbose && change)
                System.out.println("Final inside: " + sensor()[0] + "%");

            //Wait for another update.
            change = false;

        }
    }

    //Get the next scheduled item
    //Input is a Schedules object
    //Output is the pointer in that object that is next
    int getNextScheduled(Schedules sched)
    {
        int output = -1;

        int leastDiff = 24 * 60;
        int leastPointer = -1;

        int closestDiff = 24 * 60;
        int closestPointer = -1;

        Calendar time = Calendar.getInstance();
        int currHour = time.get(Calendar.HOUR_OF_DAY);
        int currMin = time.get(Calendar.MINUTE);

        List<Schedule> list = sched.getSchedules();

        for(int i = 0; i < list.size(); i++)
        {
            //Remove unactive schedules
            if(list.get(i).isActive() == false)
            {
                list.remove(i);
                i--;
                continue;
            }

            int diff = list.get(i).getHours() * 60 + list.get(i).getMinutes() - currHour * 60 - currMin;

            //Next this day
            if(diff >= 0 && diff < closestDiff)
            {
                closestDiff = diff;
                closestPointer = i;
            }

            //First tomorrow
            if(diff < leastDiff)
            {
                leastDiff = diff;
                leastPointer = i;
            }
        }

        if(closestPointer > -1)
            output = closestPointer;
        else
            output = leastPointer;

        if(verbose)
            System.out.println("Closest Scheduled to " + currHour + ":" + currMin + " is " + list.get(output).getHours() + ":" + list.get(output).getMinutes());

        return output;
    }

    //Call the sensor script
    //Wait for a return array with two values as time delay
    //if -1, sensor could not be read
    int[] sensor()
    {

        if(testMode == false)
        {

            String response = null;

            //Try to run the python function
            try{

                //Call python
                ProcessBuilder pb = new ProcessBuilder("sudo", "python", sensorScript);
                Process p = pb.start();

                //Get output from the process
                BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));


                //Check inside sensor
                response = in.readLine().toString();

                if(verbose)
                    System.out.println("Sensor inside: " + response);

                int inside = Integer.parseInt(response);


                //Check outside sensor
                response = in.readLine().toString();

                if(verbose)
                    System.out.println("Sensor outside: " + response);

                int outside = Integer.parseInt(response);


                //Map values 0-10,000ish to 0-100%
                if(inside > lumThresholdLow)
                    inside = 0;
                else if(inside > lumThresholdMid)
                    inside = 33;
                else if(inside > lumThresholdHigh)
                    inside = 67;
                else
                    inside = 100;

                //Map values 0-10,000ish to 0-100%
                if(outside > lumThresholdLow)
                    outside = 0;
                else if(outside > lumThresholdMid)
                    outside = 33;
                else if(outside > lumThresholdHigh)
                    outside = 67;
                else
                    outside = 100;

                //return results
                return new int[] { inside, outside };

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

            return new int[] {-1, -1};
        }
        else{
            int output = 25;//(int)(Math.random() * 50);

            if(blindsOpen)
                output += 25;

            if(lightOn)
                output = 90;

            return new int[] {output, output};
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
                System.out.println("Args: " + open);
                //Call python
                ProcessBuilder pb = new ProcessBuilder("sudo", "python", motorScript, open + "");
                Process p = pb.start();

                //Get output from the process
                BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

                response = in.readLine().toString();

                System.out.println("Blinds: " + response);

                //Save the change in blinds
                blindsOpen = open;
                return 1;
				/*
				//parse to output
				if(Boolean.parseBoolean(response))
				{
					//Save the change in blinds
					blindsOpen = open;
					return 1;
				}
				else
					return 0; //no change
					*/

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

            System.out.println("Blinds: " + blindsOpen);

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
                ProcessBuilder pb = new ProcessBuilder("sudo", "python", lightScript, on + "");
                Process p = pb.start();

                //Get output from the process
                BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

                response = in.readLine().toString();

                System.out.println("Light: " + response);

                //Save the change to the lights
                lightOn = on;
                return 1;

				/*
				//parse to output
				if(Boolean.parseBoolean(response))
				{
					//Save the change to the lights
					lightOn = on;
					return 1;
				}
				else
					return 0; //no change
					*/

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

            System.out.println("Light: " + lightOn);

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