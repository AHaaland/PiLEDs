package com.andrewhaaland.piapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import org.json.*;

import com.andrewhaaland.piapp.Utils.NetworkHelper;
import com.andrewhaaland.piapp.services.MyService;


public class MainActivity extends AppCompatActivity {
    TextView label;
    private static final String LED_BLUE = "http://104.162.45.205:5001/17";
    private boolean networkOK;


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent){
            String message = intent.getStringExtra(MyService.MY_SERVICE_PAYLOAD);
            //label.append(message + "\n");
            JSONObject res;
                try {
                    res = new JSONObject(message);
                    int stat = res.getInt("pinAction");
                    String pin = res.getString("pinNum");
                    String visStat = stat == 1 ? "off" : "on";
                    Snackbar sb = Snackbar.make(findViewById(R.id.textView), pin + " has been turned " + visStat + "!" , Snackbar.LENGTH_LONG);
                    View sbV = sb.getView();
                    TextView tvSnack = (TextView) sbV.findViewById(android.support.design.R.id.snackbar_text);
                    tvSnack.setGravity(Gravity.CENTER_HORIZONTAL);
                    sb.show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button rPi3on = (Button)findViewById(R.id.button);
        Button rpi3off = (Button)findViewById(R.id.button2);
        label = (TextView)findViewById(R.id.textView);
        networkOK = NetworkHelper.hasNetworkAccess(this);
        label.append("\nNetwork Ok!: " + networkOK);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mBroadcastReceiver, new IntentFilter(MyService.MY_SERVICE_MESSAGE));

        rPi3on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runClickHandler(v,true);
            }
        });
        rpi3off.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                runClickHandler(v,false);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mBroadcastReceiver);
    }

    public void runClickHandler(View view, Boolean LEDon)
    {
        if(networkOK) {
            Intent on = new Intent(this, MyService.class);
            if(LEDon) {
                on.setData(Uri.parse(LED_BLUE + "&2"));
            }
            else {
                on.setData(Uri.parse(LED_BLUE + "&1"));
            }
            startService(on);
        }
        else{
            final Snackbar sb = Snackbar.make(findViewById(R.id.textView), "No Network!", Snackbar.LENGTH_LONG);
            View sbV = sb.getView();
            TextView tvSnack = (TextView) sbV.findViewById(android.support.design.R.id.snackbar_text);
            tvSnack.setGravity(Gravity.CENTER_HORIZONTAL);
            sb.setAction("Dismiss", new View.OnClickListener(){
               public void onClick(View v){
                   sb.dismiss();
               }
            });

            sb.show();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }





































}
