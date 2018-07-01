package it.polito.did.arduino_lamp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class LampActivity extends AppCompatActivity {

    // VARIABILI XML
    private LampView lv;
    private TextView tv;
    private SeekBar sbL;
    private FlippedSeekBar sbR;
    private SeekBar sbLight;
    private Switch io;
    private RadioButton cold;
    private RadioButton soft;
    private RadioButton warm;
    private RadioGroup color;
    private ImageView lampImg;

    private ActionBar actionBar;

    // VARIABILI CONNESIONE TCP
    private WiFiSocketTask wiFiSocketTask;
    private WiFi wifi;
    private String buff;
    private WiFi.OnMessageReceived listener;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private Lamp lamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lamp);

        // COLLEGO LA LAMPADA GIUSTA A QUESTA ACTIVITY PER CONNETTERMI E COMUNICARE
        Intent i = getIntent();
        String url = i.getExtras().getString("URL");
        lamp = LampManager.getInstance().getLamp(url);

        // SHARED PREFERENCES
        sharedPreferences = getSharedPreferences("savedState", 0);
        if (sharedPreferences!=null) {
            lamp.setIntensity(sharedPreferences.getInt("i",0));
            lamp.setAngleR(sharedPreferences.getInt("r",0));
            lamp.setAngleL(sharedPreferences.getInt("l",0));
            lamp.setColor(sharedPreferences.getInt("c",0));
            Boolean state = sharedPreferences.getBoolean("s",false);
            if (state) { lamp.turnOn(); }
            else { lamp.turnOff(); }
        }

        // COLLEGO LE VARIABILI DELL'OGGETTO A QUELLE DELLA GUI
        // NOME LAMPADA

        setActionBar(actionBar); // metedo esterno per settare l'appBar
        tv=findViewById(R.id.lamp_name);
        tv.setText(lamp.getName());

        // TASTO ON-OFF
        io=findViewById(R.id.switch1);
        io.setChecked(lamp.isOn());

        // TASTI COLORE
        cold=findViewById(R.id.radioButton2);
        soft=findViewById(R.id.radioButton4);
        warm=findViewById(R.id.radioButton5);
        color=findViewById(R.id.radioGroup);
        switch (lamp.getColor()) {
            case 190: color.check(R.id.radioButton2); break;
            case 45: color.check(R.id.radioButton4); break;
            case 20: color.check(R.id.radioButton5); break;
        }

        // IMMAGINE
        if (getResources().getConfiguration().orientation == 2) { // solo in landscape mode
            lampImg=findViewById(R.id.lamp_img);
            // Loading Image from URL
            Picasso.with(this)
                    .load(lamp.getPicture())
                    .resize(100,100)
                    .into(lampImg);
        }


        // SEEKBAR INTESITA LUMINOSA
        sbLight=findViewById(R.id.seekBar2);
        sbLight.setProgress(lamp.getIntensity());
        sbLight.setMax(255);

        // SEEKBAR ANGOLO ANIMAZIONE
        lv=findViewById(R.id.lv);
        lv.setAngleL(lamp.getAngleL());
        lv.setAngleR(lamp.getAngleR());
        sbL=findViewById(R.id.seekBar);
        sbL.setProgress(lamp.getAngleL());
        sbL.setMax(130);
        sbR=findViewById(R.id.seekBar3);
        sbR.setProgress(lamp.getAngleR());
        sbR.setMax(130);

        // WI-FI
        listener = new WiFi.OnMessageReceived() {  // listener per aggiornare la GUI da Arduino
            @Override
            public void messageReceived(final String message) {
                Handler handler = new Handler(getApplicationContext().getMainLooper()); // fa riferimento alla coda di messaggi del thread principale
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        String[] tmp = message.split(";");
                        switch (tmp[0]) {
                            case "y": io.setChecked(true); break;
                            case "n": io.setChecked(false); break;
                            case "i": sbLight.setProgress(Integer.parseInt(tmp[1])); break;
                        }
                        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
                    }
                };
                handler.post(runnable);
            }
        };
        wifi = new WiFi(url, listener, this);
        wiFiSocketTask = new WiFiSocketTask();
        wiFiSocketTask.execute(wifi);

        // GRAY OUT
        if (lamp.isOn() == false) {
            lv.setAlpha(.5f);
            sbL.setAlpha(.5f);
            sbL.setEnabled(false);
            sbR.setAlpha(.5f);
            sbR.setEnabled(false);
            sbLight.setAlpha(.5f);
            sbLight.setEnabled(false);
            cold.setAlpha(.5f);
            cold.setClickable(false);
            soft.setAlpha(.5f);
            soft.setClickable(false);
            warm.setAlpha(.5f);
            warm.setClickable(false);
            color.setAlpha(.5f);
        } else {
            lv.setAlpha(1);
            sbL.setAlpha(1);
            sbL.setEnabled(true);
            sbR.setAlpha(1);
            sbR.setEnabled(true);
            sbLight.setAlpha(1);
            sbLight.setEnabled(true);
            cold.setAlpha(1);
            cold.setClickable(true);
            soft.setAlpha(1);
            soft.setClickable(true);
            warm.setAlpha(1);
            warm.setClickable(true);
            color.setAlpha(1);
        }

        // ACCENDI E SPEGNI
        io.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    lamp.turnOn();
                    buff = "y";
                    wifi.setData(buff);
                    buff = "";
                }
                else {
                    lamp.turnOff();
                    buff = "n";
                    wifi.setData(buff);
                    buff = "";
                }
            }
        });

        // CAMBIA COLORE
        color.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int color = 0;
                switch (i) {
                    case R.id.radioButton2: color = 190; break;
                    case R.id.radioButton4: color = 45; break;
                    case R.id.radioButton5: color = 20; break;
                }
                lamp.setColor(color);
                buff = "c" + lamp.getColor();
                wifi.setData(buff);
                buff="";

                io.setChecked(true);
            }
        });

        // CAMBIA INTENSITA
        sbLight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                lamp.setIntensity(progress);
                Log.d("intensita", ""+progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                buff = "i" + lamp.getIntensity();
                wifi.setData(buff);
                buff="";

                io.setChecked(true);
            }
        });

        // AGGIORNA ANGOLO MOTORE SX
        sbL.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vb.vibrate(100);
                lv.setAngleL(progress);
                lamp.setAngleL(progress);
                Log.d("seekBar", ""+progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                buff = "s" + lamp.getAngleL();
                wifi.setData(buff);
                buff="";

                io.setChecked(true);
            }
        });

        // AGGIORNA ANGOLO MOTORE DX
        sbR.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vb.vibrate(100);
                lv.setAngleR(progress);
                lamp.setAngleR(progress);
                Log.d("seekBar", ""+progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                buff = "d" + lamp.getAngleR();
                wifi.setData(buff);
                buff="";

                io.setChecked(true);
            }
        });

    }

    @Override
    protected void onDestroy() {
        wifi.setConnection(false);
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        wifi.setConnection(false);
        editor = sharedPreferences.edit();
        if (lamp != null) {
            editor.putInt("i",lamp.getIntensity());
            editor.putInt("l",lamp.getAngleL());
            editor.putInt("r",lamp.getAngleR());
            editor.putInt("c",lamp.getColor());
            editor.putBoolean("s",lamp.isOn());
            editor.commit();
        }
        super.onStop();
    }

    // imposta appBar
    public void setActionBar(ActionBar actionBar) {
        this.actionBar = actionBar;
        actionBar.setTitle((CharSequence) lamp.getName());
        actionBar.setHomeButtonEnabled(true);
        actionBar.show();
    }
}
