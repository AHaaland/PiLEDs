package com.andrewhaaland.piapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.*;

import com.andrewhaaland.piapp.Utils.NetworkHelper;
import com.andrewhaaland.piapp.services.MyService;


public class MainActivity extends AppCompatActivity {
    TextView label;
    private static final String LED_BLUE = "http://104.162.45.205:5001/17";
    private static final String LED_RGB = "http://104.162.45.205:5001/rgb/";
    private static final String TEMP_READ = "http://104.162.45.205:5000/temp";
    private boolean networkOK;
    private String selectedColor = null;
    TextView tempLabel;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent){
            String message = intent.getStringExtra(MyService.MY_SERVICE_PAYLOAD);
            //label.append(message + "\n");
            JSONObject res;
                try {
                    res = new JSONObject(message);
                    Log.i("TempCrash",res.has("pinAction")+" ");
                    if(res.has("pinAction")){
                        int stat = res.getInt("pinAction");
                        String pin = res.getString("pinNum");
                        String visStat = stat == 1 ? "off" : "on";
                        Snackbar sb = Snackbar.make(findViewById(R.id.textView), pin + " has been turned " + visStat + "!" , Snackbar.LENGTH_LONG);
                        View sbV = sb.getView();
                        TextView tvSnack = (TextView) sbV.findViewById(android.support.design.R.id.snackbar_text);
                        tvSnack.setGravity(Gravity.CENTER_HORIZONTAL);
                        sb.show();
                    }
                    else{
                        Double temp = res.getDouble("tempF");
                        tempLabel.append(temp.toString());
                    }
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
        Button getColorRGB = (Button)findViewById(R.id.button3);
        Button rgbOn = (Button)findViewById(R.id.button4);
        Button rgbOff = (Button)findViewById(R.id.button5);
        label = (TextView)findViewById(R.id.textView);
        final TextView colorText = (TextView) findViewById(R.id.textView2);
        tempLabel = (TextView) findViewById(R.id.textView3);
        networkOK = NetworkHelper.hasNetworkAccess(this);
        label.append("\nNetwork Ok!: " + networkOK);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mBroadcastReceiver, new IntentFilter(MyService.MY_SERVICE_MESSAGE));
        runTemp();


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

        rgbOn.setOnClickListener(new View.OnClickListener() {
            public static final String TAG = "";

            @Override
            public void onClick(View v) {
                Log.i(TAG, selectedColor);
                runClickHandlerRGB(v,selectedColor,true);
            }
        });
        rgbOff.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                runClickHandlerRGB(v,selectedColor,false);
            }
        });



        getColorRGB.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.color_picker_dialog, null);
                mBuilder.setTitle("Pick a Color!");
                final Spinner color_pick = (Spinner)mView.findViewById(R.id.spinner);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.colors));
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                color_pick.setAdapter(adapter);

                mBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!color_pick.getSelectedItem().toString().equalsIgnoreCase("Choose a Color…")){
                            selectedColor = color_pick.getSelectedItem().toString();
                            //TODO: This won't work, keeps appending colors!
                            colorText.setText("Current Color: "+selectedColor);
                        }
                        dialog.dismiss();
                    }
                });

                mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                mBuilder.setView(mView);
                AlertDialog aDialog = mBuilder.create();
                aDialog.show();
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

    public void runClickHandlerRGB(View view, String color, Boolean LEDon) {
        if(networkOK) {
            Intent on = new Intent(this, MyService.class);
            if (LEDon) {
                on.setData(Uri.parse(LED_RGB + color.toLowerCase() + "&2"));
            } else {
                on.setData(Uri.parse(LED_RGB + color.toLowerCase() + "&1"));
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

    public void runTemp(){
        if(networkOK) {
            Intent intent = new Intent(this, MyService.class);
            intent.setData(Uri.parse(TEMP_READ));
            startService(intent);
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
