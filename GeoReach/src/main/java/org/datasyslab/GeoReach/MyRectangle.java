package org.datasyslab.GeoReach;

public class MyRectangle {
    public double min_x;
    public double min_y;
    public double max_x;
    public double max_y;

    public MyRectangle(double p_min_x, double p_min_y, double p_max_x, double p_max_y) {
        this.min_x = p_min_x;
        this.min_y = p_min_y;
        this.max_x = p_max_x;
        this.max_y = p_max_y;
    }

    public MyRectangle() {
        this.min_x = 0.0;
        this.min_y = 0.0;
        this.max_x = 0.0;
        this.max_y = 0.0;
    }
}