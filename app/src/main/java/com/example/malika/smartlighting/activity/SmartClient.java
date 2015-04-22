/*
 	Project: Smart Lighting
 			Build a client to connect to a server.
 			The client sends a message to the server,
 			then waits for an acknowledgement.

 	Author: Zachary Terlizzese

 	Date: Feb 21, 2015

 	Source: http://www.oracle.com/technetwork/java/socket-140484.html


 	How to run:
		java SmartClient type [hostname [port]]

		where type can be:
			app
			pi

		and hostname by default is:
			localhost

		and port by default is:
			1024

*/

package com.example.malika.smartlighting.activity;

import java.io.*;
import java.net.*;
import java.util.*;

class SmartClient {

    //////////////////////////
    //Customizable Variables//
    //////////////////////////

    //Default host
    static String hostname = "localhost";

    //Default port
    static int port = 1024;

    //Max ports to try
    static int portAttempts = 1;


    ///////////////////
    //Class Variables//
    ///////////////////

    //Client type
    static String type = "NA";

    //Constants of types
    static final String APP = "APP";
    static final String PI = "PI";
    static final String UNDETERMINED = "UNKNOWN";

    //Safeword
    static final String SAFEWORD = "EXIT";

    //Heartbeat
    static final String HEARTBEAT = "<3";

    //Socket to reach server
    Socket socket;

    //Write out to server
    PrintWriter out;

    //Take input from server
    BufferedReader in;

    /////////////////////
    //Control Variables//
    /////////////////////

    //Determine if connected
    //set using connect() and disconnect()
    //get using isConnected()
    private boolean connected;


    ////////////////
    //Constructors//
    ////////////////

    //Constructor (all global inputs)
    SmartClient()
    {

        //Socket to reach server
        socket = null;

        //Write out to server
        out = null;

        //Take input from server
        in = null;

    }


    //Constructor (all inputs, no globals)
    SmartClient(String type, String hostname, int port)
    {
        //Save inputs
        this.type = type;
        this.hostname = hostname;
        this.port = port;

        //Socket to reach server
        socket = null;

        //Write out to server
        out = null;

        //Take input from server
        in = null;

    }


    ////////////////////
    //Methods: Connect//
    ////////////////////

    //Return if connected
    public boolean isConnected() {
        return connected;
    }

    //Form a connection with global inputs
    //Output: boolean connection = true if connection established, false otherwise
    //Global Outputs: Socket socket, PrintWriter out, BufferedReader in
    public boolean connect()
    {
        return connect(hostname, port);
    }

    //Form a connection
    //Inputs: string hostname, int port
    //Output: boolean connection = true if connection established, false otherwise
    //Global Outputs: Socket socket, PrintWriter out, BufferedReader in
    public boolean connect(String hostname, int port)
    {

        //return value (global control variable)
        connected = false;

        //Connection socket must be reset
        socket = null;

        //Default hostname
        if(hostname == null || "".equals(hostname))
        {
            hostname = this.hostname;
        }

        //Default port
        if(port <= 1023)
        {
            port = this.port;
        }

        //Base case if no connection can be found
        int maxPort = port + portAttempts;

        //Try ports until a server is found.
        while(socket == null && port < maxPort)
        {

            //Create socket connection
            try{
                System.out.println("Connecting to " + hostname + " on port " + port + "...");

                socket = new Socket(hostname, port);

                System.out.println("Connected to " + socket.getRemoteSocketAddress());

                //Create the output stream for the client to send information to the server
                out = new PrintWriter(socket.getOutputStream(), true);

                //Create the input stream for the server to send information to the client
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                connected = true;

            } catch (UnknownHostException e) {
                System.err.println("Could not find: " + hostname + ".");
                System.exit(-1);
            } catch(ConnectException e){
                System.err.println("Connection found host "+hostname+" but the port " + port + " was not available.");
                port++;
            }catch (SocketException e) {
                e.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }

        }

        if(connected) {

            //Tell server the type (App or Pi) and send the IP address
            try{

                //Send the client type to the server
                out.println(type + " " + socket.getLocalAddress() + ":" + socket.getLocalPort());

                //Wait for a response from the server
                String introAck = in.readLine();

                //Print that response
                System.out.println(introAck);

            } catch (UnknownHostException e) {

                System.err.println("Trying to connect to unknown host!");
                e.printStackTrace();

            } catch (SocketException e){

                System.err.println("Server has disconnected!");
                System.exit(0);

            } catch (NullPointerException e){

                System.err.println("No response from server!");
                e.printStackTrace();

            } catch (IOException e) {

                e.printStackTrace();
                System.exit(-1);

            }

        }

        return connected;
    }


    //Disconnect from the server
    public void disconnect()
    {

        //Works for apps
        send(SAFEWORD);

        //Receive the ack or hold a command
        receive();

        connected = false;

        System.out.println("Disconnected from server.");
    }


    /////////////////////////
    //Methods: Send/Receive//
    /////////////////////////

    //Take a command from the user
    //Input: String text = a command recognized by the server
    public void send(String text)
    {
        if(connected) {

            //If empty string, discard
            if(text == null || "".equals(text))
            {
                return ;
            }

            //Send input to server
            out.println(text);

        }

    }

    //Receive a message from the server
    //Output: String ack = response from the server, null if error out
    public String receive()
    {
        if(connected) {

            //Wait for a response from the server
            try{

                String message = in.readLine();

                return message;

            } catch (UnknownHostException e) {

                System.err.println("Trying to connect to unknown host!");
                e.printStackTrace();

            } catch (SocketException e){

                System.err.println("Server has disconnected!");
                System.exit(0);

            } catch (IOException e) {

                e.printStackTrace();
                System.exit(-1);

            } catch (NullPointerException e){

                System.err.println("No response from server!");
                e.printStackTrace();

            }

        }

        return null;
    }


    /////////////////////////
    //Methods: Overloadable//
    /////////////////////////

    //Run a generic client code setup for APP and PI
    //This method is intended to be overloaded by children
    public void run()
    {

        while(connected == false)
            connect();

        if(connected) {

            //Create a scanner to read the keyboard
            Scanner keyboard = new Scanner(System.in);

            //Send data over socket to server
            while(true){

                //If app...
                if(type == APP) {

                    //Send user input
                    String input = keyboard.nextLine();

                    send(input);

                    //Wait for a response from the server
                    String ack = receive();

                    System.out.println(ack);

                } else if(type == PI) {

                    //Wait for a message from the server
                    String ack = receive();

                    //Print that message
                    System.out.println(ack);

                    //Respond to server with an ack
                    send("Ack: " + ack);

                }
            }
        }
    }

    //Show the user how to write the arguments
    //This method is intended to be overloaded by children
    public static String help()
    {
        String output = "To run SmartClient type the following:\n"+
                "   java SmartClient type [hostname [port]]\n"+
                "\n"+
                "   where type can be:\n"+
                "      app\n"+
                "      pi\n"+
                "\n"+
                "   and hostname by default is:\n"+
                "      localhost\n"+
                "\n"+
                "   and port by default is:\n"+
                "      1024\n";

        return output;
    }


    ////////
    //Main//
    ////////

    //Run this program
    public static void main(String[] args){

        //Run with java SmartClient [hostname [port]]
        if(args.length > 0)
        {
            //Client type
            String typeInput = args[0].toUpperCase();

            //Show help
            if(typeInput.indexOf("HELP") > -1 || typeInput.indexOf("?") > -1)
            {
                System.out.println(help());
                System.exit(1);
            }
            //Otherwise determine the type of client
            else if(typeInput.indexOf(APP) > -1)
            {
                type = APP;
            }
            else if(typeInput.indexOf(PI) > -1)
            {
                type = PI;
            }
            else {
                type = UNDETERMINED;
            }



            if(args.length > 1)
            {
                //Host name was given
                hostname = args[1];



                if(args.length > 2)
                {
                    //port number was specified
                    try{
                        port = Integer.parseInt(args[2]);
                    } catch(NumberFormatException e)
                    {
                        System.out.println("Invalid port number: " + args[2] + help());
                        e.printStackTrace();
                    }
                }
            }
        }


        SmartClient program = new SmartClient();
        program.run();
    }
}
