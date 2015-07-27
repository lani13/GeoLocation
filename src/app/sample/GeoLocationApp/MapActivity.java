package app.sample.GeoLocationApp;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import points.database.DbGetAsyncTask;
import points.database.DbSaveAsyncTask;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;


public class MapActivity extends Activity implements IUpdateMapView {


    GoogleMap map;
    LinkedHashMap<String, LatLng> points;
    Polygon polygon;
    PolygonOptions opts;
    Button saveBtn, clearBtn;


    private final int r = Color.red(Color.CYAN);
    private final int g = Color.green(Color.CYAN);
    private final int b = Color.blue(Color.CYAN);
    private final int color = Color.argb(100,r,g,b);



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);


        map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map_fragment)).getMap();

        if (savedInstanceState == null) {

            new DbGetAsyncTask(this).execute();

        }else{
            if(savedInstanceState.containsKey("points")){
                points = (LinkedHashMap<String, LatLng>) savedInstanceState.getSerializable("points");
                updateMap(points);
            }
        }

        saveBtn = (Button)findViewById(R.id.button_save);
        clearBtn = (Button)findViewById(R.id.button_clear);

        saveBtn.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                saveBtn.setEnabled(false);
                clearBtn.setEnabled(false);
                new DbSaveAsyncTask(MapActivity.this).execute(points);
            }});

        clearBtn.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {

                if(points != null){
                    points.clear();
                }
                map.clear();

            }});


        LatLng home = new LatLng(53.113, 18.047);

        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(home, 11));

        map.setOnMapClickListener(new OnMapClickListener(){

            @Override
            public void onMapClick(LatLng point) {


                Log.i("Mappoint","Dodany punkt "+point.latitude + " " + point.longitude);

                Marker m = map.addMarker(new MarkerOptions()
                        .title("Start")
                        .snippet(point.latitude + " " + point.longitude)
                        .position(point)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.flag64))
                        .draggable(true)
                        .anchor(0.5f,0.5f));

                String s = m.getId();

                points.put(s,point);



                opts = new PolygonOptions()
                        .strokeColor(Color.BLUE)
                        .fillColor(color);

                for (LatLng value : points.values()) {
                    opts.add(value);
                }



                if(polygon != null){
                    polygon.remove();
                    polygon = null;
                }

                polygon = map.addPolygon(opts);
                polygon.setStrokeWidth(1.0f);


            }});


        map.setOnMarkerDragListener(new OnMarkerDragListener(){

            LatLng tempPoint;

            @Override
            public void onMarkerDrag(Marker midMark) {

                LatLng midPos = midMark.getPosition();
                String midMarkId = midMark.getId();

                if(points.containsKey(midMarkId))
                    points.put(midMarkId, midPos);


                if(polygon != null){
                    ArrayList<LatLng> list = new ArrayList<LatLng>();

                    for (LatLng value : points.values()) {
                        list.add(value);
                    }
                    polygon.setPoints(list);
                }

            }

            @Override
            public void onMarkerDragEnd(Marker markEnd) {

                LatLng endPos = markEnd.getPosition();
                String markId = markEnd.getId();

                if(points.containsKey(markId))
                    points.put(markId, endPos);




                if(polygon != null){
                    ArrayList<LatLng> list = new ArrayList<LatLng>();

                    for (LatLng value : points.values()) {
                        list.add(value);
                    }
                    polygon.setPoints(list);
                }




                Log.i("Mappoint","Punkt koncowy "+endPos.toString());

                Log.i("Mappoint","Hash end "+points.toString());
            }

            @Override
            public void onMarkerDragStart(Marker markStart) {

                tempPoint = markStart.getPosition();
                Log.i("Mappoint","Punkt poczatkowy "+tempPoint.toString());

                Log.i("Mappoint","Hash start "+points.toString());
            }


        });


        map.setOnMarkerClickListener(new OnMarkerClickListener(){

            @Override
            public boolean onMarkerClick(Marker mark) {
		       return true;
            }});



    }// onCreate


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("points", points);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void updateMap(LinkedHashMap<String, LatLng> dbPoints) {
        //jesli dane s¹ w bazie SQLITE to rysuje polygon
        points = dbPoints;

        if(points.size()>0){

            map.clear();

            opts = new PolygonOptions()
                    .strokeColor(Color.BLUE)
                    .fillColor(color);


            for (LatLng value : points.values()) {


                opts.add(value);


                map.addMarker(new MarkerOptions()
                        .title("Start")
                        .snippet(value.latitude + " " + value.longitude)
                        .position(value)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.flag64))
                        .draggable(true)
                        .anchor(0.5f,0.5f));
            }

            polygon = map.addPolygon(opts);
            polygon.setStrokeWidth(1.0f);
        }

        saveBtn.setEnabled(true);
        clearBtn.setEnabled(true);

    }//UpdateMap


    @Override
    public void showToast(String info) {

        Toast.makeText(getApplicationContext(), info, Toast.LENGTH_LONG).show();

    }



}
