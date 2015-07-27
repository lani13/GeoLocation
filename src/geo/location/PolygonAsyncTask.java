package geo.location;

import java.lang.reflect.Array;
import java.util.LinkedHashMap;
import cloud.messaging.CloudMessageReceiver;
import com.google.android.gms.maps.model.LatLng;
import points.database.PointDataSource;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


public class PolygonAsyncTask extends AsyncTask<Void, Void, Void>{

	Context context;
	CloudMessageReceiver broadcastReceiver;
	double lat, lon;
	PointDataSource dbDataSource;
	LinkedHashMap<String, LatLng> points;
	LatLng l;
	int result;
	
	public PolygonAsyncTask(String lati, String longi, CloudMessageReceiver brRec, Context cont){
		this.lat = Double.parseDouble(lati);
		this.lon = Double.parseDouble(longi);
		this.broadcastReceiver = brRec;
		this.context = cont;
		l = new LatLng(lat,lon);
		result = 0;
	}
	
	@Override
	protected Void doInBackground(Void ...voids) {
		
		try{
			dbDataSource = new PointDataSource(context);
			dbDataSource.open();
		    points = dbDataSource.getAllPointsHashMap();
		    dbDataSource.close();
		    Log.i("Mappoint","rozmiar hasha "+points.size());
		}catch(Exception e){
		     points = new LinkedHashMap<String, LatLng>();
		}
		
		
		int n = points.size();
		int index = 0;
		
		if(n>0 && l != null){
		double[] points_lat = new double[n];
		double[] points_lon = new double[n];
		
						
		for(LatLng v : points.values()){
			Array.set(points_lat, index, v.latitude);
			Array.set(points_lon, index, v.longitude);
			index++;
		}
		
		CustomPolygon myPolygon = new CustomPolygon(points_lat,points_lon,n,context);
		boolean test = myPolygon.setPolygon();
		
		if(test){
			
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
		
			Log.i("Mappoint", "Locating after notification from google "+ resultText);
			
		}//if test
		}//if points.size>0
		else
			Log.i("Mappoint", "Error setting polygon");
		
		return null;
	}

	@Override
	protected void onPostExecute(Void v) {
		
		if(result == CustomPolygon.OUTSIDE){
			
    		  broadcastReceiver.sendResultNotifiation(lat+"",lon+"","Dziecko jest poza wyznaczonym obszarem");

			Log.i("Mappoint","Send coords notification sent");
			
		}
		
	}

	
	
}
