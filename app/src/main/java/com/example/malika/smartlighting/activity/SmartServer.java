/*
 	Project: Smart Lighting
 			Build a server to connect to multiple clients.
 			The server should accept input from a client,
 			forward it to another client,
 			and send an acknowledgement to the first client.

 	Author: Zachary Terlizzese

 	Date: Feb 21, 2015

 	Source: http://www.oracle.com/technetwork/java/socket-140484.html


 	How to run:
		java SmartServer [port]

		where port by default is:
			1024

*/

package com.example.malika.smartlighting.activity;

import java.io.*;
import java.net.*;
import java.util.*;

class SmartServer {

	//Variables

	//Default port
	static int port = 1024;
	

	//This server socket
	ServerSocket server;

	//The client sockets
	ArrayList<Socket> clients;

	//App clients (sorted from clients list)
	ArrayList<ServerThread> app;

	//Pi clients (sorted from clients list)
	ArrayList<ServerThread> pi;

	//Command to send from Apps to Pis
	String command;


	//Constructor
	SmartServer(){

		//Setup empty socket
		server = null;

		//Setup empty clients
		clients = new ArrayList<Socket>();

		app = new ArrayList<ServerThread>();

		pi = new ArrayList<ServerThread>();

		command = "";

		//Look for an open port
		while(server == null){
			try{

				server = new ServerSocket(port);

			} catch (IOException e) {
				System.out.println("Could not open a socket on port "+port);
				port++;
			}
		}

    	//Loop continuously
		while(true){
			try{
				System.out.println("Server waiting for client on port " + port + "...");

				//Hang here until a client is found...
				clients.add(server.accept());

				//Create access to the new client
				//SmartClient c = new SmartClient(clients.get(clients.size() - 1));
				ServerThread s = new ServerThread(this, clients.get(clients.size() - 1));

				System.out.println("New client connected.");

				Thread t = new Thread(s);

				t.start();
			} catch (IOException e) {

				System.out.println("Accept failed: "+port);
				System.exit(-1);

			}
		}
	}


	//Methods

	//Add client to app list
	public void reportApp(ServerThread thread)
	{
		app.add(thread);
	}

	//Add client to pi list
	public void reportPi(ServerThread thread)
	{
		pi.add(thread);
	}

	//Update from apps to be sent to the pis
	public void appUpdate(String input)
	{

		command = input;
	}

	//Add client to pi list
	public String piUpdate()
	{
		return command;
	}

	//Show the user how to write the arguments
	public static String help()
	{
		String output = "To run SmartServer type the following:\n"+
						"   java SmartServer [port]\n"+
						"\n"+
						"   where port by default is: 1024\n";

		return output;
	}



	//Main method to start the server
  	public static void main(String[] args){

		//Run with java SmartServer [port]
  		if(args.length > 0)
  		{
				//port number was specified
				try{

					port = Integer.parseInt(args[0]);

				} catch(NumberFormatException e)
				{
					System.out.println("Invalid port number: " + args[0] + "\n\n" + help());
					System.exit(-1);
				}
  		}

        SmartServer program = new SmartServer();

  	}

}




//This keeps a connection with each client
class ServerThread implements Runnable {

	//Variables

	//Smart Server
	SmartServer server;

	//This client
	Socket socket;

	//Type of client
	String type;

	//Constants of types
	static final String APP = SmartClient.APP;
	static final String PI = SmartClient.PI;
	static final String UNDETERMINED = SmartClient.UNDETERMINED;
	
	//Safeword
	static final String SAFEWORD = SmartClient.SAFEWORD;
	

	//Constructor
	ServerThread(SmartServer parent, Socket client)
	{
		server = parent;
		socket = client;
		type = UNDETERMINED;
	}

	//Run a thread to receive input from client and send an acknowledgement back
	public void run(){

		//This will read input from the client
		BufferedReader in = null;

		//This will send an acknowledgement back to the client
		PrintWriter out = null;

		//Show connection was established at the server end
		try{

			//Prepare stream to read from client
		  	in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		  	//Prepare stream to write to client
		  	out = new PrintWriter(socket.getOutputStream(), true);

		} catch (IOException e) {

		  	e.printStackTrace();
		  	System.exit(-1);

		}


		//Communicate with client
		try{


			//Assume the first message sent will be to determine the client's type
			String typeInput = in.readLine();

			//Determine the type of client
			if(typeInput.indexOf(APP) > -1)
			{
				type = APP;
				server.reportApp(this);
			}
			else if(typeInput.indexOf(PI) > -1)
			{
				type = PI;
				server.reportPi(this);
			}
			else {
				type = UNDETERMINED;
			}

			//Show the text on the server
			System.out.println("Client " + type + " is online.");

			//Send acknowledgement back to this client
			out.println("Ack: " + type);

			//Remember the last command given (only used in PI)
			String lastCommand = "";

			//Continuously read input from this client while connected
			while(true){

				//Interpret command for apps
				if(type == APP)	{

					//Wait for input from app
					String line = in.readLine();

					//Show the text on the server screen
					System.out.println(line);
					
					//Process safeword
					if(line.equals(SAFEWORD)) {
						
						//Send safeword back to this client
						try{
						
							out.println(line);
							
							System.out.println("Client " + type + " disconnected.");
							
							//Exit the thread
							return;
	
						}catch(Exception e) {
							System.out.println("Err: " + e);							
						}
						
					}

					//update the server with the command
					server.appUpdate(line);

					//Send acknowledgement back to this client
					out.println("Ack: " + line);

				}
				//Interpret command for pis
				else if(type == PI) {					

					//Check for new command from server
					String message = server.piUpdate();				

					//If there is a new command
					if(!message.equals("") && !lastCommand.equals(message)){

						//Show the client
						out.println(message);

						//Wait for an ack
						String ack = in.readLine();

						//Show the text on the server
						System.out.println(ack);

						//Store last command
						lastCommand = message;

					}

				}

			}



		} catch (IOException e) {

			System.out.println("Client " + type + " disconnected.");

		}

	}

}
