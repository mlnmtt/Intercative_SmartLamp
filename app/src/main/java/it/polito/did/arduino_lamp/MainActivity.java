package it.polito.did.arduino_lamp;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ProgressBar loader;
    private LinearLayout ll;
    private SwipeRefreshLayout swipeLayout;
    private Runnable r = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toast.makeText(MainActivity.this, "Searching for lamps", Toast.LENGTH_SHORT).show();

        final LampManager lm= LampManager.getInstance();

        ll = (LinearLayout) findViewById(R.id.ll);
        loader=(ProgressBar)findViewById(R.id.loading);

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override public void run() {
                        swipeLayout.setRefreshing(false);
                        /*
                        if (r != null) {
                            lm.discover(ll, r);
                        }
                        */
                        recreate();
                    }
                }, 1000);
            }
        });
        swipeLayout.setColorScheme(android.R.color.holo_green_dark);

        r = new Runnable() {
            @Override
            public void run() {
                loader.setVisibility(View.GONE);
                // Toast.makeText(MainActivity.this, "Searching for lamps", Toast.LENGTH_SHORT).show();
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
                    // Loading Image from URL
                    Picasso.with(v.getContext())
                            .load(lamps.get(i).getPicture())
                            .resize(200,200)
                            .into(iv);

                    // Change Activity
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
        };

        lm.discover(ll, r);
    }
}
