package uk.ac;

public class LongLat {
    public double longitude;
    public double latitude;

    public LongLat(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public boolean isConfined() {
        return false;
    }

    public double distanceTo(LongLat a) {
        return 0.0;
    }

    public boolean closeTo(LongLat a) {
        return false;
    }

    public LongLat nextPosition(int a) {
        LongLat b = new LongLat(0.0, 0.0);
        return b;
    }
}
