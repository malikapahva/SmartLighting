package com.example.malika.smartlighting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.malika.smartlighting.R;


public class SmartAndroid extends ActionBarActivity implements ClientThread.ClientInterface {

    //Class Variables
    static final String hostnameDefault = "10.176.67.118"; //"10.176.67.118";//"192.168.2.9";//"localhost";
    static final int portDefault = 1024;
    static final boolean autoConnect = false; //Connect on startup

    //Objects
    SmartClient client;

    //GUI
    TextView status;
    TextView hostname;
    TextView port;
    Button command;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect_fragment);

        //Create the client which holds the connection with the server
        client = new SmartClient(SmartClient.APP, hostnameDefault, portDefault);

        //Find the UI objects
        status = (TextView) findViewById(R.id.status);
        hostname = (TextView)findViewById(R.id.hostname);
        port = (TextView) findViewById(R.id.port);
        command = (Button) findViewById(R.id.command);

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
            ClientThread thread = new ClientThread(this, client);
            thread.execute(ClientThread.CONNECT);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_app, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onCommand(View view) {

        //if no connection...
        if(client.isConnected() == false)
        {
            Log.v("DEBUG", "Retrying connection...");

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
            ClientThread thread = new ClientThread(this, client);
            thread.execute(ClientThread.CONNECT);

            //Disable buttons
            command.setEnabled(false);
            hostname.setEnabled(false);
            port.setEnabled(false);
        }
        //Otherwise, perform action
        else {

            nextActivity();

        }
    }

    public void update(String input)
    {

        if(command.isEnabled() == false)
        {
            command.setText("Continue");
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

        //Show the ack from the server (accessed by client thread)
        status.setText(input);
        Toast.makeText(this, input, Toast.LENGTH_SHORT).show();

        //Save client to singleton
        Singleton.getInstance().client = client;

        //Move to Schedule
        nextActivity();
    }

    @Override
    public void noConnection() {

    }

    public void nextActivity(){

        Intent intent = new Intent();
        intent.setClassName("com.example.malika.smartlighting", "com.example.malika.smartlighting.activity.MainActivity");
        startActivity(intent);

    }


}
