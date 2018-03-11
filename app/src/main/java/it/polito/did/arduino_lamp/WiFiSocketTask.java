package it.polito.did.arduino_lamp;

/**
 * Created by mlnmtt on 08/03/18.
 */

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * AsyncTask that connects to a remote host over WiFi and reads/writes the connection
 * using a socket. The read loop of the AsyncTask happens in a separate thread, so the
 * main UI thread is not blocked. However, the AsyncTask has a way of sending data back
 * to the UI thread. Under the hood, it is using Threads and Handlers.
 */
public class WiFiSocketTask extends AsyncTask<Void, String, Void> {

    // Tag for logging
    private final String TAG = getClass().getSimpleName();

    // ciao

    // Location of the remote host
    String address;
    int port;

    // Special messages denoting connection status
    private static final String PING_MSG = "SOCKET_PING";
    private static final String CONNECTED_MSG = "SOCKET_CONNECTED";
    private static final String DISCONNECTED_MSG = "SOCKET_DISCONNECTED";

    Socket socket;
    private BufferedReader inStream;
    OutputStream outStream = new OutputStream() {
        @Override
        public void write(int i) throws IOException {

        }
    };

    // Signal to disconnect from the socket
    private boolean disconnectSignal = false;

    // Socket timeout - close if no messages received (ms)
    private int timeout = 5000;

    // Constructor
    public WiFiSocketTask(String address, int port) {
        this.address = address;
        this.port = port;
    }

    /**
     * Helper function, print a status to both the UI and program log.
     */
    void setStatus(String s) {
        Log.v(TAG, s);
    }

    /**
     * Invoked by the AsyncTask when the connection is successfully established.
     */
    private void connected() {
        setStatus("Connected.");
    }

    /**
     * Invoked by the AsyncTask when the connection ends..
     */
    private void disconnected() {
        setStatus("Disconnected.");
    }

    /**
     * Invoked by the AsyncTask when a newline-delimited message is received.
     */
    private void gotMessage(String msg) {
        Log.v(TAG, "[RX] " + msg);
    }


    /**
     * Main method of AsyncTask, opens a socket and continuously reads from it
     */
    @Override
    protected Void doInBackground(Void... arg) {

        try {

            // Open the socket and connect to it
            socket = new Socket();
            socket.connect(new InetSocketAddress(address, port), timeout);

            // Get the input and output streams
            inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outStream = socket.getOutputStream();

            // Confirm that the socket opened
            if(socket.isConnected()) {

                // Make sure the input stream becomes ready, or timeout
                long start = System.currentTimeMillis();
                while(!inStream.ready()) {
                    long now = System.currentTimeMillis();
                    if(now - start > timeout) {
                        Log.e(TAG, "Input stream timeout, disconnecting!");
                        disconnectSignal = true;
                        break;
                    }
                }
            } else {
                Log.e(TAG, "Socket did not connect!");
                disconnectSignal = true;
            }

            // Read messages in a loop until disconnected
            while(!disconnectSignal) {

                // Parse a message with a newline character
                String msg = inStream.readLine();

                // Send it to the UI thread
                publishProgress(msg);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error in socket thread!");
        }

        // Send a disconnect message
        publishProgress(DISCONNECTED_MSG);

        // Once disconnected, try to close the streams
        try {
            if (socket != null) socket.close();
            if (inStream != null) inStream.close();
            if (outStream != null) outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * This function runs in the UI thread but receives data from the
     * doInBackground() function running in a separate thread when
     * publishProgress() is called.
     */
    @Override
    protected void onProgressUpdate(String... values) {

        String msg = values[0];
        if(msg == null) return;

        // Handle meta-messages
        if(msg.equals(CONNECTED_MSG)) {
            connected();
        } else if(msg.equals(DISCONNECTED_MSG))
            disconnected();
        else if(msg.equals(PING_MSG))
        {}

        // Invoke the gotMessage callback for all other messages
        else
            gotMessage(msg);

        super.onProgressUpdate(values);
    }

    /**
     * Write a message to the connection. Runs in UI thread.
     */
    public void sendMessage(String data) {
        try {
            outStream.write(data.getBytes());
            outStream.write('\n');
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set a flag to disconnect from the socket.
     */
    public void disconnect() {
        disconnectSignal = true;
    }
}
