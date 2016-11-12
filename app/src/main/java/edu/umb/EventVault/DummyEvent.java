package edu.umb.EventVault;

/**
 * Created by alpi on 11/10/16.
 */

class DummyEvent {
    private double lat;
    private double lon;
    private String name, activity;

    DummyEvent(String activity, double lat, double lon, String name) {
        this.activity = activity;
        this.lat = lat;
        this.lon = lon;
        this.name = name;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    double getLat() {
        return lat;
    }

    public String toString() {
        return "Event name : " + this.name + " "+this.activity;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
