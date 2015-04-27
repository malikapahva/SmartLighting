package com.example.malika.smartlighting.activity;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.malika.smartlighting.R;


public class ConnectFragment extends Fragment {

    //Class Variables
    static final String hostnameDefault = "10.176.67.118";//"192.168.2.9";//"localhost";
    static final int portDefault = 1024;
    static final boolean autoConnect = false; //Connect on startup

    //Objects
    ConnectInterface listener; //fragment interface to send data to main
    SmartClient client;
    TextView status;
    TextView hostname;
    TextView port;
    Button command;
    SmartAndroid smartAndroid;

    public static ConnectFragment newInstance() {
        ConnectFragment fragment = new ConnectFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    public ConnectFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Create the client which holds the connection with the server
        client = new SmartClient(SmartClient.APP, hostnameDefault, portDefault);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.connect_fragment, container, false);

        //Find the UI objects
        status = (TextView) view.findViewById(R.id.status);
        hostname = (TextView) view.findViewById(R.id.hostname);
        port = (TextView) view.findViewById(R.id.port);
        command = (Button) view.findViewById(R.id.command);

        command.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Switch to schedule info
                onCommand(command);
            }
        });

        hostname.setText(hostnameDefault);
        port.setText(portDefault + "");

        if(autoConnect) {
            //Hide hostname and port input
            hostname.setEnabled(false);
            port.setEnabled(false);

            //Disable button while connecting
            command.setEnabled(false);

            //Show that a connection is occurring
            status.setText("Connecting...");

            //Start a thread to connect the client
            ClientThread thread = new ClientThread(smartAndroid, client);
            thread.execute(ClientThread.CONNECT);
        }


        return view;
    }


    public void onCommand(View view) {

        //if no connection...
        if(client.isConnected() == false)
        {

            //Create the client which holds the connection with the server
            try {
                client = new SmartClient(SmartClient.APP, String.valueOf(hostname.getText()).trim(), Integer.parseInt(String.valueOf(port.getText()).trim()));
            } catch(NumberFormatException e)
            {
                status.setText("Enter a valid port number: 1024 - 65534");
                return;
            }

            //Show that a connection is occurring
            status.setText("Connecting...");

            //Create a new thread to connect to the server
            ClientThread thread = new ClientThread(smartAndroid, client);
            thread.execute(ClientThread.CONNECT);

            //Disable buttons
            command.setEnabled(false);
            hostname.setEnabled(false);
            port.setEnabled(false);
        }

    }

    //Response from ClientThread Async Task
    public void update(String input)
    {

        if(command.isEnabled() == false)
        {
            command.setText("Command");
            command.setEnabled(true);
        }

        //No response = assume disconnect
        if(input == null)
        {
            status.setText("Server disconnected!");
            client.disconnect();

            command.setText("Connect");
            hostname.setEnabled(true);
            port.setEnabled(true);
            return;
        }

        //Could not connect
        if(client.isConnected() == false)
        {
            command.setText("Retry Connection");
            hostname.setEnabled(true);
            port.setEnabled(true);
        }
        //Else if connection was successful...
        else {
            //this will signal main activity and pass the client object to it
            listener.connected(client);

        }

        //Show the ack from the server (accessed by client thread)
        status.setText(input);


    }


    /////////////
    //Interface//
    /////////////

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (ConnectInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ConnectInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    //implement this interface and it functions in main if you want the fragment to send data to the
    //main activity
    public interface ConnectInterface {

        public void connected(SmartClient client);

    }



}
