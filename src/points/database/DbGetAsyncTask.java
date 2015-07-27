package points.database;

import java.util.LinkedHashMap;

import app.sample.GeoLocationApp.IUpdateMapView;
import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

public class DbGetAsyncTask extends AsyncTask<Void, Void, LinkedHashMap<String, LatLng>>{

	Activity context;
	PointDataSource dbDataSource;
	LinkedHashMap<String, LatLng> points;
	IUpdateMapView view;
	
	
	public DbGetAsyncTask(Activity context) {
		super();
		this.context = context;
		view = (IUpdateMapView)context;
	}

	@Override
	protected void onPreExecute() {

		super.onPreExecute();
	}
	
	@Override
	protected LinkedHashMap<String, LatLng> doInBackground(Void... a) {
		
		try{
			dbDataSource = new PointDataSource(context);
			dbDataSource.open();
		    points = dbDataSource.getAllPointsHashMap();
		    dbDataSource.close();
		    Log.i("Mappoint","rozmiar hasha "+points.size());
		}catch(Exception e){
		     points = new LinkedHashMap<String, LatLng>();
		}
		
		
		return points;
	}

	@Override
	protected void onPostExecute(LinkedHashMap<String, LatLng> result) {
		view.updateMap(result);
		super.onPostExecute(result);
	}

	

}
