package points.database;


public class DbPoint{
    long id;
    String point_id;
    Double lat, lon;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPointId() {
        return this.point_id;
    }

    public void setPointId(String id) {
        this.point_id = id;
    }

    public double getLat() {
        return this.lat;
    }

    public void setLat(double la) {
        this.lat = la;
    }

    public double getLon() {
        return this.lon;
    }

    public void setLon(double lo) {
        this.lon = lo;
    }


}

