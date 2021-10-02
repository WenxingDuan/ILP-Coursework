package uk.ac.ed.inf;

// import java.lang.*;

public class LongLat {
    public double longitude;
    public double latitude;

    public LongLat(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public boolean isConfined() {

        double minLongitude = -3.192473;
        double maxLongitude = -3.184319;
        double minLatitude = 55.942617;
        double maxLatitude = 55.946233;

        return (this.longitude >= minLongitude) && (this.longitude <= maxLongitude) && (this.latitude >= minLatitude)
                && (this.latitude <= maxLatitude);
    }

    public double distanceTo(LongLat location) {
        return (Math.sqrt(
                Math.pow(this.longitude - location.longitude, 2) + Math.pow(this.latitude - location.latitude, 2)));
    }

    public boolean closeTo(LongLat location) {
        return (0.00015 > this.distanceTo(location));
    }

    public LongLat nextPosition(int degree) {
        if (degree == -999) {
            return this;
        } else {
            double newLongitude = this.longitude + (0.00015 * Math.cos(Math.toRadians(degree)));
            double newLatitude = this.latitude + (0.00015 * Math.sin(Math.toRadians(degree)));
            LongLat newLocation = new LongLat(newLongitude, newLatitude);
            return newLocation;
        }

    }
}
