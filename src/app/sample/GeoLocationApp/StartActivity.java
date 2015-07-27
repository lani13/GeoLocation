package app.sample.GeoLocationApp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import cloud.messaging.RegIdDataSource;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

public class StartActivity extends Activity {

    Button buttonRegister;
    ImageButton mapButton, locatingButton;
    Context context;
    GoogleCloudMessaging gcm;
    String regid;
    String SENDER_ID = "";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        context = getApplicationContext();

        mapButton = (ImageButton)findViewById(R.id.button_map);
        locatingButton = (ImageButton)findViewById(R.id.button_location_activity);
        buttonRegister = (Button)findViewById(R.id.button_register);

        mapButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(context, MapActivity.class);
                startActivity(i);
            }});

        locatingButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(context, LocatingActivity.class);
                startActivity(i);
            }});

        buttonRegister.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View arg0) {
                registerInBackground();
            }});


    }//onCreate

    private void registerInBackground() {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        if(regid != null){
                            RegIdDataSource source = new RegIdDataSource(context);
                            source.open();
                            source.insertIdTable(regid);
                            regid = source.getRegistrationId();
                            source.close();
                            Log.i("Mappoint", "reg id saved to db");
                        }
                        //Toast.makeText(context, getString(R.string.txt_gcm_registered), Toast.LENGTH_LONG).show();
                        break;
                    case 1:

                        //Toast.makeText(context, getString(R.string.error_register_gcm), Toast.LENGTH_LONG).show();
                        break;
                }
                super.handleMessage(msg);
            }

        };
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);

                    if (regid != null) {
                        Log.i("Mappoint", "reg id = " +regid);
                        handler.sendEmptyMessage(0);
                    } else {
                        Log.i("Mappoint", "brak reg id");
                        handler.sendEmptyMessage(1);
                    }

                } catch (IOException e) {
                    handler.sendEmptyMessage(1);
                    Log.i("Mappoint", "message service error while registering to google "+ e.toString());
                }
            }
        };
        new Thread(runnable).start();
    }


}