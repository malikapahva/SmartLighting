/*
package com.example.malika.smartlighting.activity;

import android.os.AsyncTask;
import android.util.Log;


class ClientThread extends AsyncTask<String, Integer, String> {

    //Parent file used to call this class
    SmartAndroid app;

    //Client to connect to server
    SmartClient client;

    //Table of commands
    static final String CONNECT = "CONNECT";
    static final String SEND = "SEND";
    static final String RECEIVE = "RECEIVE";
    static final String DISCONNECT = "DISCONNECT";

    //Constructor (called on every search)
    public ClientThread(SmartAndroid app, SmartClient client)
    {
        this.app = app;

        this.client = client;

    }

    //Shows the progress of the download
    protected void onProgressUpdate(Integer progress) {
        Log.v("Progress", "Progress: " + progress);

    }

    //Search the webpage, download the file, and populate the list
    //urlComponents should be [ address, file, extension ]
    @Override
    protected String doInBackground(String... input) {

        String output = null;

        if(input.length > 0)
        {

           switch(input[0])
           {

                case CONNECT:

                    client.connect();

                    if(client.isConnected())
                        output = "Connection established!";
                    else output = "Could not connect to server!";

                    break;

               case SEND:
                   if(input.length > 1)
                   {
                       client.send(input[1]);

                       output = client.receive();
                   }
                   break;

               default:

           }

        }

       return output;
    }

    //Return the list to the main activity
    protected void onPostExecute(String result) {

        app.update(result);

    }
}
*/

package com.example.malika.smartlighting.activity;

import android.os.AsyncTask;
import android.util.Log;


class ClientThread extends AsyncTask<String, Integer, String> {

    //Wait for response
    boolean waitForResponse = false;

    //Parent file used to call this class
    ClientInterface app;

    //Client to connect to server
    SmartClient client;

    //Table of commands
    static final String CONNECT = "CONNECT";
    static final String SEND = "SEND";
    static final String RECEIVE = "RECEIVE";
    static final String DISCONNECT = "DISCONNECT";

    //Constructor (called on every search)
    public ClientThread(ClientInterface app, SmartClient client)
    {
        this.app = app;

        this.client = client;

    }

    //Shows the progress of the download
    protected void onProgressUpdate(Integer progress) {
        Log.v("Progress", "Progress: " + progress);

    }

    //Search the webpage, download the file, and populate the list
    //urlComponents should be [ address, file, extension ]
    @Override
    protected String doInBackground(String... input) {

        String output = null;

        if(input.length > 0)
        {

            switch(input[0])
            {

                case CONNECT:

                    client.connect();

                    if(client.isConnected())
                        output = "Connection established!";
                    else output = "Could not connect to server!";

                    break;

                case SEND:
                    if(input.length > 1)
                    {
                        client.send(input[1]);

                        if(waitForResponse)
                            output = client.receive();
                        else
                            output = input[1];
                    }
                    break;

                default:

            }

        }

        return output;
    }

    //Return the list to the main activity
    protected void onPostExecute(String result) {

        app.update(result);

    }

    interface ClientInterface {

        public void update(String result);

        public void noConnection();

    }
}
