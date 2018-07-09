package it.polito.did.arduino_lamp;

import android.os.AsyncTask;

/**
 * AsyncTask that connects to a remote host over WiFi and reads/writes the connection
 * using a socket. The read loop of the AsyncTask happens in a separate thread, so the
 * main UI thread is not blocked. However, the AsyncTask has a way of sending data back
 * to the UI thread. Under the hood, it is using Threads and Handlers.
 */
public class WiFiSocketTask extends AsyncTask<WiFi, Void, Void> {

    // Constructor
    WiFiSocketTask() {}

    /**
     * Main method of AsyncTask, opens a socket and continuously reads from it
     */
    @Override
    protected Void doInBackground(WiFi... wiFis) {
        wiFis[0].run();
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
