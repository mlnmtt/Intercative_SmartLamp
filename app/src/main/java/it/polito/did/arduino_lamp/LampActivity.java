package it.polito.did.arduino_lamp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;
import java.util.Set;

public class LampActivity extends AppCompatActivity {

    private int LOCAL_PORT = 2048;
    private int remote_port;

    private LampView lv;

    private SeekBar sb;
    private SeekBar sbLight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lamp);

        // COME COLLEGO LA LAMPADA GIUSTA A QUESTA ACTIVITY PER CONNETTERMI E COMUNICARE?
        Intent i = getIntent();
        String url = i.getExtras().getString("URL");
        Lamp lamp = LampManager.getInstance().getLamp(url);

        // WI-FI
        WiFiSocketTask wiFiSocketTask = new WiFiSocketTask(url, LOCAL_PORT);
        final WiFi wifi = new WiFi(wiFiSocketTask, url);
        wifi.connect(url , LOCAL_PORT);

        wifi.send("Hello!");

        // NOME LAMPADA
        TextView tv = (TextView) findViewById(R.id.lamp_name);
        tv.setText(url);

        // SEEKBAR ANGOLO ANIMAZIONE
        lv=findViewById(R.id.lv);
        sb=findViewById(R.id.seekBar);
        sb.setProgress((int)lv.getAngle());

        // SEEKBAR INTESITA LUMINOSA
        sbLight=findViewById(R.id.seekBar2);
        //sbLight.setProgress(lamp.getIntensity());


        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lv.setAngle(progress);
                Log.d("seekBar", ""+progress);
                String buff = "a" + progress;
                wifi.send(buff);
                // AGGIUNGI COMUNICAZIONE CON ARDUINO PER MUOVERE ALI
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }
}
