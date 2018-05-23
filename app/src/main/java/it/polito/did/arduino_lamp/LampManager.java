package it.polito.did.arduino_lamp;

import android.util.Log;
import android.view.View;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
    private int LOCAL_PORT = 4096;
    private int remote_port;
    private byte[] buf = new byte[64000];
    private InetAddress remote_address;
    private String remote_name;
    private String remote_img;
    private String message;

    // costruttore
    private LampManager() { lampList= Collections.synchronizedList(new ArrayList<Lamp>()); } // lista sincronizzata affinchè sia accesibile da entrambi i thread senza che litighino

    // static block initialization for exception handling
    // SINGLETON
    static {
        try {
            instance = new LampManager();
        } catch(Exception e) {
            throw new RuntimeException("Exception occured in creating singleton instance");
        }
    }

    // restituisce il manager delle lampade
    public static LampManager getInstance(){
        return instance;
    }

    // restituisce un elenco di lampade identificate
    public List<Lamp> getLamps(){
        return lampList;
    }

    // restituisce la lampada connessa
    public Lamp getLamp(String url) {
        for (Lamp lamp : lampList) {
            if (lamp.getUrl().equals(url)) { return lamp; }
        }
        return null;
    }

    // inizia una ricerca di altre lampade, poi invoca il metodo run() dell’oggetto done per indicare che la ricerca è terminata
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

                        // ascolta e ricevi pacchetti per non più di 10 secondi
                        long startTime=System.currentTimeMillis();
                        int delta=10000;
                        while(delta>0) {
                            s.setSoTimeout(delta);
                            s.receive(p);

                            // assegna variabili udp lampada
                            remote_port = p.getPort();
                            remote_address = p.getAddress();
                            message = new String (p.getData());
                            String[] tmp = message.split(";");
                            remote_name = tmp[0];
                            remote_img = tmp[1];
                            Log.d("UDP", remote_address.toString() + ":" + remote_port + "; name: " + remote_name + "; img: " + remote_img);
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
                    l.setName(remote_name);
                    l.setPicture(remote_img);
                    lampList.add(l);
                }
                //esegue il runnable done nel thread principale
                v.post(done);
            }
        });
        t.start();
    }
}

