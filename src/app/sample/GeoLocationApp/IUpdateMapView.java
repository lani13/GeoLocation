package app.sample.GeoLocationApp;

import java.util.LinkedHashMap;
import com.google.android.gms.maps.model.LatLng;

public interface IUpdateMapView {


    public void updateMap(LinkedHashMap<String, LatLng> result);
    public void showToast(String info);
}
