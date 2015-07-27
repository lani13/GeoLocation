package cloud.messaging;


import java.lang.reflect.Array;
import java.util.LinkedHashMap;

import app.sample.GeoLocationApp.LocatingActivity;
import app.sample.GeoLocationApp.R;
import points.database.PointDataSource;
import geo.location.CustomPolygon;

import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;


public class CloudMessageService extends Service  {

	public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
   
	    
	private final int MY_NOTIFICATION_ID = 11151992;
    
    Context context;
    String regid;
    boolean received;
    
    double lat, lon;
	PointDataSource dbDataSource;
	LinkedHashMap<String, LatLng> points;
	LatLng l;
	int result;
    

	@Override
	public IBinder onBind(Intent arg0) {
		
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(final Intent intent, int flags, int startid) {

		 Log.i("Usluga", "message calculate coords service start");
		
		 context = getApplicationContext();
		 
		 Bundle extras = intent.getExtras();
		 String latitude = extras.getString("lat");
		 String longitude = extras.getString("lon");
		  
		 lat = Double.parseDouble(latitude);
		 lon = Double.parseDouble(longitude);
		
		 l = new LatLng(lat,lon);
		 result = 0;
		 
		 
		 
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
			boolean isPolygonSet = myPolygon.setPolygon();
			
			if(isPolygonSet){
				
				result = myPolygon.Lct(l.latitude, l.longitude);
			
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
				
			}//if isPolygonSet
			}//if points.size>0
			else
				Log.i("Mappoint", "Error setting polygon");
		 
		 
		 
			if(result == CustomPolygon.OUTSIDE){
				
				Intent checkIntent = new Intent(LocatingActivity.DATA_REFRESHED_ACTION);
  			    checkIntent.putExtra("lat", lat+"");
  			    checkIntent.putExtra("lon",lon+"");
  			  
  			    context.sendOrderedBroadcast(
  					    checkIntent,
  					    null,
  						new BroadcastReceiver() {

  							@Override
  							public void onReceive(Context cont, Intent intent) {
  							
  								int result = getResultCode();

  								Log.i("Mappoint", "result code from LocatingActivity "+result);
  								
  								if (result != Activity.RESULT_OK) {
  									  sendNotification(lat+"",lon+"","Aplikacja dziecka przes³a³a wspó³rzêdne");
  								}
  							}
  						}, 
  						null, 
  						0, 
  						null,
  						null);  
				
				
			}
		 
		 return START_NOT_STICKY;
	}
	
	
	@Override
	public void onDestroy() {
        super.onDestroy();
		Log.i("Usluga", "message service stop");
	}


	public void sendNotification(String lat, String lon, String message){
		
		Intent locatingActivityIntent = new Intent(context,LocatingActivity.class);
		  locatingActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		  
		  locatingActivityIntent.putExtra("lat", lat+"");
		  locatingActivityIntent.putExtra("lon", lon+"");
		
		
		   final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, locatingActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			
		   RemoteViews mContentView = new RemoteViews(context.getPackageName(), R.layout.notification_view);

		    mContentView.setTextViewText(R.id.notif_text, "Dziecko jest poza wyznaczonym obszarem");
		  
			
			Notification.Builder notificationBuilder = new Notification.Builder(context)
			.setTicker("Wiadomoœæ od dziecka")
			.setSmallIcon(android.R.drawable.stat_sys_warning)
			.setAutoCancel(true)
			.setContentIntent(pendingIntent)
			.setContent(mContentView);

		

			NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(MY_NOTIFICATION_ID,notificationBuilder.build());
		
		   Log.i("Mappoint","Send coords notification sent");
		
		
		
	}
	
	
}
