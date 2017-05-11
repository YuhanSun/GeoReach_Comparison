package org.datasyslab.GeoReach;

public class Entity {
    public boolean IsSpatial;
    public double lon;
    public double lat;

    Entity() {
        this.IsSpatial = false;
        this.lon = 0.0;
        this.lat = 0.0;
    }

    Entity(double lon, double lat) {
        this.IsSpatial = true;
        this.lon = lon;
        this.lat = lat;
    }
}