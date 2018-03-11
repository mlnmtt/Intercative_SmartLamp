package it.polito.did.arduino_lamp;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ProgressBar loader;
    private LinearLayout ll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ll = (LinearLayout) findViewById(R.id.ll);
        loader=(ProgressBar)findViewById(R.id.loading);

        final LampManager lm= LampManager.getInstance();

        lm.discover(ll, new Runnable() {
            public void run() {
                loader.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                for (int i=ll.getChildCount()-1; i>=0; i--) {
                    View v=ll.getChildAt(i);
                    if (v==loader) continue;
                    ll.removeViewAt(i);
                }
                List<Lamp> lamps=lm.getLamps();
                for (int i=0; i<lamps.size(); i++) {
                    final String url = lamps.get(i).getUrl();
                    View v = getLayoutInflater().inflate(R.layout.adapter_lamp, ll,false); // aggiunge view alle liste
                    TextView tv = (TextView) v.findViewById(R.id.lamp_name);
                    tv.setText(lamps.get(i).getName());
                    ImageView iv = (ImageView) v.findViewById(R.id.lamp_img);
                    iv.setImageBitmap(lamps.get(i).getPicture());
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(getApplicationContext(), LampActivity.class);
                            i.putExtra("URL", url);
                            startActivity(i);
                        }
                    });
                    ll.addView(v);
                }
            }
        });
    }
}
