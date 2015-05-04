package com.example.malika.smartlighting.activity;

import com.example.malika.smartlighting.dto.Schedules;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;


//Class for Pis, used from a terminal
class SmartPi extends SmartClient{

    //Class variables
    int luminosity; //0-100 = percentage, -1 = off

    Schedules schedules; //Holds the schedule of luminosities

    ObjectMapper objectMapper; //Reads JSON

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

            //Print command
            else{

                System.out.println(command);

            }

            //Return an Acknowledgement
            send("Pi: " + command);

            //Loop to query light and open/close blinds and turn on/off light



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