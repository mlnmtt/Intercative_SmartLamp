package it.polito.did.arduino_lamp;

/**
 * Created by mlnmtt on 23/02/18.
 */

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class WiFi {

    String url;

    // Tag for logging
    private final String TAG = getClass().getSimpleName();

    // AsyncTask object that manages the connection in a separate thread
    private WiFiSocketTask wifiTask;

    // Costruttore
    public WiFi(WiFiSocketTask wifiTask, String url) {
        this.wifiTask = wifiTask;
        this.url = url;
    }

    /**
     * Helper function, print a status to both the UI and program log.
     */
    void setStatus(String s) {
        Log.v(TAG, s);
    }

    /**
     * Try to start a connection with the specified remote host.
     */
    public void connect(String host, int port) {

        if(wifiTask != null) {
            setStatus("Already connected!");

            try {
                // Start the asyncronous task thread
                setStatus("Attempting to connect...");
                wifiTask = new WiFiSocketTask(host, port);
                wifiTask.execute();

            } catch (Exception e) {
                e.printStackTrace();
                setStatus("Invalid address/port!");
            }
        }
    }

    /**
     * Disconnect from the connection.
     */
    public void disconnect() {
        if(wifiTask == null) {
            setStatus("Already disconnected!");
            return;
        }

        wifiTask.disconnect();
        setStatus("Disconnecting...");
    }

    /**
     * Send the message using the AsyncTask.
     */
    public void send(String msg) {
        if(wifiTask == null) return;
        if(msg.length() == 0) return;
        wifiTask.sendMessage(msg);
        Log.v(TAG, "[TX] " + msg);
    }

    /**
     * Invoked by the AsyncTask when the connection ends..
     */
    private void disconnected() {
        setStatus("Disconnected.");
        wifiTask = null;
    }

}