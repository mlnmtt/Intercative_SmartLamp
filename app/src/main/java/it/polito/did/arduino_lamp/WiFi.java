package it.polito.did.arduino_lamp;

/**
 * Created by mlnmtt on 23/02/18.
 */

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import static java.lang.Thread.sleep;

public class WiFi {

    // Tag for logging
    private final String TAG = getClass().getSimpleName();

    // Location of the remote host
    String url;
    int port=2048;

    // variabili TCP
    private Socket socket;
    private PrintWriter bufferOut;
    private BufferedReader bufferIn;
    private String data = "?";
    private String packet;
    private String command;
    private int value;
    private char[] tmp;
    private OnMessageReceived messageListener = null;

    private Lamp lamp;

    // stato della connessione
    private boolean connection;

    // Socket timeout - close if no messages received (ms)
    private int timeout = 10000;

    private LampActivity lampActivity;

    // Costruttore
    public WiFi(String url, OnMessageReceived listener, LampActivity lampActivity) {
        this.url = url;
        lamp = LampManager.getInstance().getLamp(url);
        this.lampActivity = lampActivity;
        connection = true;
        messageListener = listener; // per agire dopo aver interpretato i messaggi
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setConnection(boolean connection) {
        this.connection=connection;
    }

    // Helper function, print a status to both the UI and program log
    void setStatus(String s) {
        Log.v(TAG, s);
    }

    // Invoked by the AsyncTask when the connection is successfully established
    private void connected() {
        setStatus("Connected");
    }

    // Invoked by the AsyncTask when the connection ends
    private void disconnected() {
        setStatus("Disconnected");
    }

    // Invoked by the AsyncTask when a newline-delimited message is received
    private String gotMessage(String msg) {
        Log.v(TAG, "[RX] " + msg);
        //tmp = msg.toCharArray();
        command = msg.substring(0,1);
        if (msg.length() > 1) { value = Integer.parseInt(msg.substring(1)); }
        // INTERPRETA MESSAGGIO E AGGIORNA LE VARIABILI E L'INTERFACCIA
        switch (command) {
            case "y": lamp.turnOn(); break;
            case "n": lamp.turnOff(); break;
            case "c": lamp.setColor(value); break;
            case "i": lamp.setIntensity(value); break;
            case "s": lamp.setAngleL(value); break;
            case "d": lamp.setAngleR(value); break;
        }

        return command + ";" + value;
    }

    public String getPacket() { return packet; }

    // Disconnect from the connection
    public void disconnect() {
        try {
            if (socket != null) socket.close();
            if (bufferIn != null) bufferIn.close();
            if (bufferOut != null) bufferOut.close();
            disconnected();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Apre la connessione, interroga Arduino o gli comunica aggiornamenti di stato, aspetta una risposta, chiude la connessione
    public void run() {
        try {
            // Send messages in a loop every second
            while(connection) {
                // Open the socket and connect to it
                socket = new Socket(url, port);
                socket.setSoTimeout(timeout);

                // Get the input and output streams
                bufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                bufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                // contatta arduino
                bufferOut.println(data); // come stai? oppure aggiornati
                // ritorna al caso default
                if (!data.equals("?"))
                    setData("?");

                // Read a response message from arduino
                String msg = bufferIn.readLine();
                if (msg!=null)
                    messageListener.messageReceived(gotMessage(msg)); // chiamo gotMessage e poi invoco il listener per aggiornare l'interfaccia dell'activity

                // Once talked, try to close the streams
                disconnect();
                sleep(1000);
            }
        } catch (Exception e) {
            lampActivity.finish();
            e.printStackTrace();
            Log.e(TAG, "Error in socket thread!");
        }

    }

    public interface OnMessageReceived {
        void messageReceived(String message);
    }

}