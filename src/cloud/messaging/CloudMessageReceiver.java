package cloud.messaging;


import app.sample.GeoLocationApp.LocatingActivity;
import app.sample.GeoLocationApp.R;
import geo.location.PolygonAsyncTask;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;


public class CloudMessageReceiver extends BroadcastReceiver  {

	private final int MY_NOTIFICATION_ID = 11151990;
	
    private static final String ACTION_CHECK_POS = "checkposition";
    private static final String ACTION_SEND_COORD = "sendcoordinates";
    private static final String ACTION_SEND_CONTACTS = "sendcontacts";
    
    private static final String ACTION_GCM_REGISTRATION =
            "com.google.android.c2dm.intent.REGISTRATION";

    private static final String ACTION_PACKAGE_REPLACED =
            "android.intent.action.PACKAGE_REPLACED";

     Context context;
	
	@Override
	public void onReceive(Context cntx, Intent i) {
		
		Log.i("Mappoint", "cloud message receiver");

		context = cntx;
		
		Bundle extras = i.getExtras();
		String action = i.getAction();
		
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
	     
	    String messageType = gcm.getMessageType(i);
		
		if (!extras.isEmpty()) {
	    	  
	    	  Log.i("Mappoint", "extras not empty");
	    	  
	    	  if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
	    		  Log.i("Mappoint", "message received "+extras.keySet().toString());
	    		  
	    		  String userAction = extras.getString("action");
	    		  
	    		  
	    		  if(ACTION_CHECK_POS.equals(userAction)){
	    			 

	    			  final String lat = extras.getString("lat");
	    			  final String lon = extras.getString("lon");
	    			  
	    			  Log.i("Mappoint", "kid sent check coords "+lat+" "+lon);
						
	    			  
	    			  Intent checkIntent = new Intent(LocatingActivity.DATA_REFRESHED_ACTION);
	    			  checkIntent.putExtra("lat", lat);
	    			  checkIntent.putExtra("lon",lon);
	    			  
	    			  context.sendOrderedBroadcast(
	    					    checkIntent,
	    					    null,
	    						new BroadcastReceiver() {

	    							@Override
	    							public void onReceive(Context cont, Intent intent) {
	    							
	    								int result = getResultCode();

	    								Log.i("Mappoint", "result code from LocatingActivity "+result);
	    								
	    								if (result != Activity.RESULT_OK) {
	    									  sendResultNotifiation(lat,lon,"Aplikacja dziecka przes³a³a wspó³rzêdne");
	    								}
	    							}
	    						}, 
	    						null, 
	    						0, 
	    						null,
	    						null);  
 
	    		  }
	    		  else if(ACTION_SEND_COORD.equals(userAction)){

	    			  String lat = extras.getString("lat");
	    			  String lon = extras.getString("lon");
	    			  
	    			  Log.i("Mappoint", "kid sent send position "+lat+" "+lon);
	    			  

	    			  Intent coordIntent = new Intent(context,CloudMessageService.class);
	    			  coordIntent.putExtra("lat", lat);
	    			  coordIntent.putExtra("lon",lon);
	    			  
	    			  context.startService(coordIntent);
	    			  
	    		  }
	    		  else if(ACTION_SEND_CONTACTS.equals(userAction)){
	    			  Log.i("Mappoint", "kid sent contacts");
	    		  }
	    		  
	    		  
	    	  }else if (action.equals(ACTION_GCM_REGISTRATION)) {
	    		  Log.i("Mappoint", extras.getString("registration_id"));
	          } else if (action.equals(ACTION_PACKAGE_REPLACED)) {
	        	  Log.i("Mappoint", "package replaced");
	          }
	      }
	}

	
	public void sendResultNotifiation(String lat, String lon, String message){
		
		  Intent locatingActivityIntent = new Intent(context,LocatingActivity.class);
		  locatingActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		  
		  locatingActivityIntent.putExtra("lat", lat);
		  locatingActivityIntent.putExtra("lon", lon);
		
		
		   final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, locatingActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			
		   RemoteViews mContentView = new RemoteViews(context.getPackageName(), R.layout.notification_view);

			mContentView.setTextViewText(R.id.notif_text, message);
		  
			
			Notification.Builder notificationBuilder = new Notification.Builder(context)
			.setTicker("Wiadomoœæ od dziecka")
			.setSmallIcon(android.R.drawable.stat_sys_warning)
			.setAutoCancel(true)
			.setContentIntent(pendingIntent)
			.setContent(mContentView);

		

			NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(MY_NOTIFICATION_ID,notificationBuilder.build());
			
			Log.i("Mappoint","Notification sent");
			
	}
	
}
