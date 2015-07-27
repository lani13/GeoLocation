package points.database;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

import com.google.android.gms.maps.model.LatLng;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


public class PointDataSource{

    private Context context;
    private SQLiteDatabase db;
    private String[] columns = {COLUMN_ID,POINT_ID,POINT_LAT, POINT_LON};


    public static final String COLUMN_ID = "_id";
    public static final String POINT_ID = "point_id";
    public static final String POINT_LAT = "point_lat";
    public static final String POINT_LON = "point_lon";
    public static final String DATABASE_TABLE = "points_tab";

    private static final String DATABASE_NAME = "points.db";

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
            + DATABASE_TABLE + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + POINT_ID
            + " text not null,"+  POINT_LAT  + " real not null," + POINT_LON + " real not null);";


    public PointDataSource(Context context) {
       this.context = context;
    }

    public void open(){
        //db = helper.getWritableDatabase();
        db = SQLiteDatabase.openOrCreateDatabase(context.getExternalFilesDir(null)+ "/" + DATABASE_NAME, null);
        db.execSQL(CREATE_TABLE);
        Log.i("Mappoint","open " + context.getExternalFilesDir(null));
    }

    public void close() {
        db.close();
        //helper.close();
    }

    private void clearPointsTable() {
        if(db != null){
            db.delete(DATABASE_TABLE, null, null);
        }
    }

    private ArrayList<DbPoint> getAllPoints() {

        ArrayList<DbPoint> list = new ArrayList<DbPoint>();

        if(db != null){
            Cursor cursor = db.query(DATABASE_TABLE, columns, null, null, null, null, COLUMN_ID);

            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                DbPoint point = new DbPoint();
                point.setId(cursor.getLong(0));
                point.setPointId(cursor.getString(1));
                point.setLat(cursor.getDouble(2));
                point.setLon(cursor.getDouble(3));
                list.add(point);
                cursor.moveToNext();
            }

            cursor.close();
        }
        return list;
    }

    public LinkedHashMap<String, LatLng> getAllPointsHashMap(){

        LinkedHashMap<String, LatLng> hashPoints = new LinkedHashMap<String, LatLng>();
        ArrayList<DbPoint> points = getAllPoints();

        for(DbPoint p : points){
            LatLng l = new LatLng(p.getLat(),p.getLon());
            hashPoints.put(p.getPointId(),l);

        }
        return hashPoints;
    }


    public void insertPointsTable(LinkedHashMap<String, LatLng> hashPoints){
        Log.i("Mappoint","insert");
        clearPointsTable();

        Set<LinkedHashMap.Entry<String, LatLng>> entries = hashPoints.entrySet();

        if(db != null){


            for(LinkedHashMap.Entry e : entries){
                String key = (String) e.getKey();
                LatLng l = (LatLng)e.getValue();
                Log.i("Mappoint","insert "+key);
                ContentValues values = new ContentValues();
                values.put(POINT_ID, key);
                values.put(POINT_LAT, l.latitude);
                values.put(POINT_LON, l.longitude);

                db.insert(DATABASE_TABLE, null, values);
            }


        }
    }
}
