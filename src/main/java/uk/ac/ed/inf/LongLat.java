/**
 * Class for store a location by longitude and latitude
 *
 * @author Duan Wenxing
 */

package uk.ac.ed.inf;

public class LongLat {
    public double longitude;
    public double latitude;

    /**
     * Constructor of LongLat class.
     * 
     * @param longitude the longitude
     * @param latitude  the latitude
     */
    public LongLat(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    /**
     * Method to check if the location is in the valid range
     * 
     * @return True if the location is in the valid range False otherwise
     */
    public boolean isConfined() {
        double minLongitude = -3.192473;
        double maxLongitude = -3.184319;
        double minLatitude = 55.942617;
        double maxLatitude = 55.946233;
        return (this.longitude >= minLongitude) && (this.longitude <= maxLongitude) && (this.latitude >= minLatitude)
                && (this.latitude <= maxLatitude);
    }

    /**
     * Method to calculate the distance to the input location
     * 
     * @param location the location to be calculated
     * @return the distance between two location
     */
    public double distanceTo(LongLat location) {
        return (Math.sqrt(
                Math.pow(this.longitude - location.longitude, 2) + Math.pow(this.latitude - location.latitude, 2)));
    }

    /**
     * Method to check if the distance of input location is within 0.00015 degree
     * 
     * @param location the location to be checked
     * @return True if the distance between two location is within 0.00015 degree
     *         False otherwise
     */
    public boolean closeTo(LongLat location) {
        return (0.00015 > this.distanceTo(location));
    }

    /**
     * Method to calculate the location after moving 0.00015 degree in the direction
     * of the input degree
     * 
     * @param degree the direction in degree of the next step
     * @return the location after moving
     */
    public LongLat nextPosition(int degree) {
        int hoverDegree = -999;
        if (degree == hoverDegree) {
            return this;
        } else {
            double newLongitude = this.longitude + (0.00015 * Math.cos(Math.toRadians(degree)));
            double newLatitude = this.latitude + (0.00015 * Math.sin(Math.toRadians(degree)));
            LongLat newLocation = new LongLat(newLongitude, newLatitude);
            return newLocation;
        }
    }

    /**
     * Method to check if the input location is exactly same with the current
     * location
     * 
     * @param other the location to be checked
     * @return true if the input location is exactly same with the current location,
     *         false otherwise
     */
    public boolean samePoint(LongLat other) {
        return (Double.doubleToLongBits(this.longitude) == Double.doubleToLongBits(other.longitude))
                && (Double.doubleToLongBits(this.latitude) == Double.doubleToLongBits(other.latitude));

    }
}
