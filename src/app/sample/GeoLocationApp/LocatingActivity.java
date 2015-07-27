package app.sample.GeoLocationApp;


import java.lang.reflect.Array;
import java.util.LinkedHashMap;

import points.database.DbGetAsyncTask;
import geo.location.CustomPolygon;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class LocatingActivity extends Activity implements IUpdateMapView {

    GoogleMap map;
    Button locBtn;
    LinkedHashMap<String, LatLng> points;
    Polygon polygon;
    PolygonOptions opts;
    Marker locatingPoint;
    private BroadcastReceiver mRefreshReceiver;

    private final int r = Color.red(Color.CYAN);
    private final int g = Color.green(Color.CYAN);
    private final int b = Color.blue(Color.CYAN);
    private final int color = Color.argb(100,r,g,b);

    public static final String DATA_REFRESHED_ACTION = "app.sample.GeoLocation.DATA_REFRESHED";



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locating_activity);

        map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map_fragment_locating)).getMap();
        locBtn = (Button)findViewById(R.id.button_locating);


        if (savedInstanceState == null) {

            new DbGetAsyncTask(this).execute();

        }
        else{
            if(savedInstanceState.containsKey("points")){
                points = (LinkedHashMap<String, LatLng>) savedInstanceState.getSerializable("points");
                updateMap(points);
            }

            if(savedInstanceState.containsKey("lat")){
                double lat = savedInstanceState.getDouble("lat");
                double lon = savedInstanceState.getDouble("lon");

                locatingPoint = map.addMarker(new MarkerOptions()
                        .title("Punkt do zlokalizowania")
                        .snippet(lat + " " + lon)
                        .position(new LatLng(lat,lon))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pins32_1))
                        .draggable(true)
                        .anchor(0.5f,1f));
            }
        }






        Intent i = getIntent();

        if(i != null){
            Bundle extras = i.getExtras();
            if(extras != null){
                if(extras.containsKey("lat")){

                    String lat = extras.getString("lat");
                    String lon = extras.getString("lon");
                    Log.i("Mappoint","Nowe Activity - Dziecko przesla³o wspl. "+lat+" "+lon);

                    setCurrentKidPosMarker(lat, lon);
                }
            }
            else{
                LatLng home = new LatLng(53.113, 18.047);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(home, 11));
            }
        }

        map.setOnMapClickListener(new OnMapClickListener(){

            @Override
            public void onMapClick(LatLng point) {

                if(locatingPoint != null)
                    locatingPoint.remove();


                Log.i("Mappoint","Dodany punkt "+point.latitude + " " + point.longitude);

                locatingPoint = map.addMarker(new MarkerOptions()
                        .title("Punkt do zlokalizowania")
                        .snippet(point.latitude + " " + point.longitude)
                        .position(point)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pins32_1))
                        .draggable(true)
                        .anchor(0.5f,1f));


            }});


        map.setOnMarkerClickListener(new OnMarkerClickListener(){

            @Override
            public boolean onMarkerClick(Marker mark) {
                return true;
            }});

        locBtn.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View view) {

                int n = points.size();
                int index = 0, result=0;

                if(n>0 && locatingPoint != null){
                    double[] points_lat = new double[n];
                    double[] points_lon = new double[n];


                    for(LatLng v : points.values()){
                        Array.set(points_lat, index, v.latitude);
                        Array.set(points_lon, index, v.longitude);
                        index++;
                    }

                    CustomPolygon myPolygon = new CustomPolygon(points_lat,points_lon,n,getApplicationContext());
                    boolean test = myPolygon.setPolygon();

                    if(test){
                        LatLng l = locatingPoint.getPosition();
                        result = myPolygon.LctPtRelBndry(l.latitude,l.longitude); //lat,lon

                        String resultText;

                        switch(result){
                            case CustomPolygon.INSIDE:	resultText = "Wewn¹trz obszaru";
                                break;
                            case CustomPolygon.ONSIDE: 	resultText = "Na krawêdzi obszaru";
                                break;
                            case CustomPolygon.OUTSIDE:	resultText = "Na zewn¹trz obszaru";
                                break;
                            default:  					resultText = "B³¹d";
                                break;
                        }

                        Toast.makeText(getApplicationContext(), resultText, Toast.LENGTH_LONG).show();
                    }//if test
                }//if points.size>0
                else
                    Toast.makeText(getApplicationContext(), "Ustaw wspó³rzêdne obszaru", Toast.LENGTH_LONG).show();


            }});

        registerCloudMessageReceiver();

    }//onCreate

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putSerializable("points", points);

        if(locatingPoint != null){
            LatLng loc = locatingPoint.getPosition();
            outState.putDouble("lat", loc.latitude);
            outState.putDouble("lon", loc.longitude);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void updateMap(LinkedHashMap<String, LatLng> dbPoints) {

        points = dbPoints;

        //jesli dane s¹ w bazie SQLITE to rysuje polygon
        if(points.size()>0){

            opts = new PolygonOptions()
                    .strokeColor(Color.BLUE)
                    .fillColor(color);



            for (LatLng value : points.values()) {


                opts.add(value);


                map.addMarker(new MarkerOptions()
                        .title("Start")
                        .snippet(value.latitude + " " + value.longitude)
                        .position(value)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.flag32))
                        .draggable(false)
                        .anchor(0.5f,0.5f));
            }

            polygon = map.addPolygon(opts);
            polygon.setStrokeWidth(1.0f);
        }
        else{
            showToast("Nie ma punktów w bazie");
        }

    }

    @Override
    public void showToast(String info) {

        Toast.makeText(getApplicationContext(), info, Toast.LENGTH_LONG).show();
    }


    private void setCurrentKidPosMarker(String lat, String lon){

        if(lat != null){
            LatLng p = new LatLng(Double.parseDouble(lat),Double.parseDouble(lon));

            if(locatingPoint != null)
                locatingPoint.remove();

            locatingPoint = map.addMarker(new MarkerOptions()
                    .title("Aktualne wspó³rzêdne dziecka")
                    .snippet(lat + " " + lon)
                    .position(p)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pins32_1))
                    .draggable(true)
                    .anchor(0.5f,1f));

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(p, 10));
        }
    }



    private void registerCloudMessageReceiver(){

        mRefreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Log.i("Mappoint","BroadcastIntent from cloud received in LocatigActivity");

                //result code RESULT_OK wys³any do nadawcy - nie wysy³aj notyfikacji;

                if (mRefreshReceiver.isOrderedBroadcast()) {

                    setResultCode(RESULT_OK);

                    if(intent != null){
                        Bundle extras = intent.getExtras();
                        if(extras != null){
                            if(extras.containsKey("lat")){

                                String lat = extras.getString("lat");
                                String lon = extras.getString("lon");
                                Log.i("Mappoint","Stare Activity - Dziecko przesla³o wspl. "+lat+" "+lon);

                                setCurrentKidPosMarker(lat, lon);
                            }
                        }
                    }

                }


            }
        };

    }



    @Override
    protected void onResume() {

        try
        {
            IntentFilter intentFilter = new IntentFilter();

            intentFilter.addAction(DATA_REFRESHED_ACTION);

            registerReceiver(mRefreshReceiver,intentFilter);
        }catch(IllegalArgumentException e) {
            e.printStackTrace();
        }

        super.onPause();
    }

    @Override
    protected void onPause() {

        try{
            unregisterReceiver(mRefreshReceiver);
        }catch(IllegalArgumentException e) {
            e.printStackTrace();
        }

        super.onResume();
    }




}
