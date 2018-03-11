package it.polito.did.arduino_lamp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by mlnmtt on 30/11/17.
 */

public class Lamp {
    // variabili
    private String URL;
    private String name;
    private int rgb;
    private boolean io;
    private int intensity;
    private Bitmap picture;

    private Paint paint1;
    private float angle;
    private Path path1;

    // costruttore
    public Lamp(Context ctx, String URL) {
        this.URL=URL;
        // x simulare pacchetti di arduino che portano le info sulla lampada
        switch (URL) {
            case "url1":
                picture = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.lamp1);
                name = "Cucina";
                break;
            case "url2":
                picture = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.lamp2);
                name = "Studio";
                break;
            case "url3":
                picture = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.lamp3);
                name = "Bagno";
                break;
        }
    }

    public Lamp getLamp() { return Lamp.this; }

    // metodi
    // ogni set e get invia e riceve pacchetti TCP a e da Arduino
    public String getUrl(){
        return URL;
    }
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
    public Bitmap getPicture(){
        return picture;
    }
}
