package it.polito.did.arduino_lamp;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by mlnmtt on 30/11/17.
 */

public class LampManager {
    // variabili
    private static LampManager instance;
    private List<Lamp> lampList;
    // String [] urls = new String[]{"url1", "url2", "url3"};
    private int LOCAL_PORT = 4096;
    private int remote_port;
    private byte[] buf = new byte[64000];
    private InetAddress remote_address;
    private String remote_name;

    // costruttore
    private LampManager() { lampList= Collections.synchronizedList(new ArrayList<Lamp>()); } // lista sincronizzata affinchè sia accesibile da entrambi i thread senza che litighino

    //static block initialization for exception handling
    static {
        try {
            instance = new LampManager();
        } catch(Exception e) {
            throw new RuntimeException("Exception occured in creating singleton instance");
        }
    }

    public static LampManager getInstance(){
        return instance;
    }

    // restituisce un elenco di lampade identificate
    public List<Lamp> getLamps(){
        return lampList;
    }

    public Lamp getLamp(String url) {
        for (Lamp lamp : lampList) {
            if (lamp.getUrl() == url) {
                return lamp;
            }
        }
        return null;
    }

    // inizia una ricerca di altre lampade, poi invoca il metodo run() dell’oggetto done per indicare che la ricerca è terminata.
    public void discover(final View v, final Runnable done)  {
        lampList.clear();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                DatagramSocket s = null;
                DatagramPacket p;
                Set<String> discovered = new HashSet<>(); // set di lampade
                try {
                    try {
                        s = new DatagramSocket(LOCAL_PORT, InetAddress.getByAddress(new byte[]{(byte)-1,(byte)-1,(byte)-1,(byte)-1})); // casting per byte di indirizzo IP
                        p = new DatagramPacket(buf, buf.length);
                        Log.d("UDP", "start listening"); // debugging

                        // ascolta e ricevi pacchetti per non più di 5 secondi
                        long startTime=System.currentTimeMillis();
                        int delta=5000;
                        while(delta>0) {
                            s.setSoTimeout(delta);
                            s.receive(p);

                            remote_port = p.getPort();
                            remote_address = p.getAddress();
                            remote_name = new String (p.getData());
                            Log.d("UDP", remote_address.toString() + ":" + remote_port + "; name: " + remote_name);
                            delta=5000-(int)(System.currentTimeMillis()-startTime);
                            // aggiunge al set le lampade trovate
                            discovered.add(remote_address.toString().substring(1)); // per togliere lo slash iniziale dell'inetAddress
                        }
                    } finally {
                        if (s != null) s.close(); // chiude socket
                    }
                } catch (Exception e){
                    Log.d("UDP", e.toString());
                }

                // aggiunge le lampade dal set alla lista
                for (String url : discovered) {
                    Lamp l = new Lamp(v.getContext(), url);
                    lampList.add(l);
                }
                //esegue il runnable done nel thread principale
                v.post(done);
            }
        });
        t.start();
    }
}

