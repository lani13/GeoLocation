package cloud.messaging;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class RegIdDataSource {

	private Context context;
	private SQLiteDatabase db;
	private String[] columns = {COLUMN_ID,REG_ID};
	
	
	public static final String COLUMN_ID = "_id";
	public static final String REG_ID = "reg_id";
	public static final String DATABASE_TABLE = "reg_tab";
  
    private static final String DATABASE_NAME = "reg.db";
   	
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
    	      + DATABASE_TABLE + "(" + COLUMN_ID
    	      + " integer primary key autoincrement, " + REG_ID + " text not null);";
    

	public RegIdDataSource(Context context) {

		this.context = context;
	}
	
	public void open(){
	   
		db = SQLiteDatabase.openOrCreateDatabase(context.getExternalFilesDir(null)+ "/" + DATABASE_NAME, null);
		db.execSQL(CREATE_TABLE);   
		Log.i("Mappoint","open " + context.getExternalFilesDir(null));
	}

	public void close() {

		db.close();
    }

	private void clearTable() {
	    if(db != null){
	    	db.delete(DATABASE_TABLE, null, null);
	    }
	  }
	
	public String getRegistrationId() {
		
		String id = "";
		
		if(db != null){
		Cursor cursor = db.query(DATABASE_TABLE, columns, null, null, null, null, COLUMN_ID);
		
		cursor.moveToFirst();
		
	    while (!cursor.isAfterLast()) {
	   
	    	id = cursor.getString(1);
   	        cursor.moveToNext();
	    }
	    
	    cursor.close();
		}
		
		return id;
	}
	
	
	
	public void insertIdTable(String id){
		Log.i("Mappoint","insert");

		if(db != null){
			
			clearTable();
			
			Log.i("Service1","insert "+ id);
    		
			ContentValues values = new ContentValues();
	    	
			values.put(REG_ID, id);
    		    	
	    	db.insert(DATABASE_TABLE, null, values);
		}
	}
}
