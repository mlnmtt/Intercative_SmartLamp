package it.polito.did.arduino_lamp;

import android.content.Context;

public class Lamp {
    // variabili
    private String URL;
    private String name;
    private int rgb;
    private boolean io;
    private int intensity;
    private String picture;
    private int angleL;
    private int angleR;

    // costruttore
    public Lamp(Context ctx, String URL) {
        this.URL=URL;
    }

    public Lamp getLamp() { return Lamp.this; }

    // metodi
    // ogni set e get invia e riceve pacchetti TCP a e da Arduino
    public String getUrl(){ return URL; }
    public void setColor(int rgb){
        this.rgb = rgb;
    }
    public int getColor(){
        return rgb;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }
    public void turnOn(){
        this.io = true;
    }
    public void turnOff(){
        this.io = false;
    }
    public boolean isOn(){
        return io;
    }
    public void setIntensity(int intensity){
        this.intensity = intensity;
    }
    public int getIntensity(){
        return intensity;
    }
    public void setPicture(String picture) { this.picture = picture; }
    public String getPicture(){
        return picture;
    }
    public int getAngleL() { return angleL; }
    public void setAngleL(int angleL) { this.angleL = angleL; }
    public int getAngleR() { return angleR; }
    public void setAngleR(int angleR) { this.angleR = angleR; }
}
