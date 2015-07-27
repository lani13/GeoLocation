package points.database;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import app.sample.GeoLocationApp.IUpdateMapView;
import com.google.android.gms.maps.model.LatLng;

import java.util.LinkedHashMap;


public class DbSaveAsyncTask extends AsyncTask<LinkedHashMap<String, LatLng>, Void, String>{

	Activity context;
	PointDataSource dbDataSource;
	LinkedHashMap<String, LatLng> points;
	IUpdateMapView view;
	
	
	public DbSaveAsyncTask(Activity context) {
		super();
		this.context = context;
		view = (IUpdateMapView)context;
	}

	@Override
	protected void onPreExecute() {

		super.onPreExecute();
	}
	
	@Override
	protected String doInBackground(LinkedHashMap<String, LatLng>... p) {
		
		LinkedHashMap<String, LatLng> points = p[0];
		
		if(points.size()>2){
			   
			LinkedHashMap<String, LatLng> newPoints = new LinkedHashMap<String, LatLng>();
			int m = 0;
			
			for(LatLng l : points.values()){
				newPoints.put("m"+m, l);
				m++;
			}
			
			
		   try{
			dbDataSource = new PointDataSource(context);
		    dbDataSource.open();
		    dbDataSource.insertPointsTable(newPoints);
		    dbDataSource.close();
		   }
		   catch(Exception e){
			Log.i("Mappoint","B³¹d zapisu do bazy");
		    return "B³¹d zapisu do bazy";
		   }
		}
		else{
			return "Za ma³o punktów";
		}
		
		return "Zapisano wspó³rzêdne do bazy";
	}

	@Override
	protected void onPostExecute(String s) {
		view.showToast(s);
		super.onPostExecute(null);
	}

	

}
