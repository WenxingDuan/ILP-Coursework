/**
 * Class for organizing the path.
 *
 * @author Duan Wenxing
 */

package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.List;
import java.awt.geom.Line2D;

public class PathOrganizer {

    public static double upperDegree(double degree) {
        double multiple = Math.ceil(degree / 10.0);
        if (Double.doubleToLongBits(multiple * 10) == Double.doubleToLongBits(360.0)) {
            return 0;
        } else {
            return multiple * 10;
        }
    }

    public static double lowerDegree(double degree) {
        double multiple = Math.floor(degree / 10.0);
        return multiple * 10;
    }

    public static boolean canFly(LongLat start, LongLat end, List<List<LongLat>> noFlyLongLat) {
        for (List<LongLat> currZonePoints : noFlyLongLat) {
            for (int i = 0; i < currZonePoints.size() - 1; i++) {
                LongLat currLinePoint1 = currZonePoints.get(i);
                LongLat currLinePoint2 = currZonePoints.get(i + 1);
                if (Line2D.linesIntersect(start.longitude, start.latitude, end.longitude, end.latitude,
                        currLinePoint1.longitude, currLinePoint1.latitude, currLinePoint2.longitude,
                        currLinePoint2.latitude)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static List<LongLat> organizePath(LongLat start, LongLat end, List<List<LongLat>> noFlyLongLat) {
        List<LongLat> pathLongLats = new ArrayList<LongLat>();
        pathLongLats.add(start);
        double upper_longitude, upper_latitude, lower_longitude, lower_latitude;
        double alpha = 170.0;

        double delta_X = start.longitude - end.longitude;
        double delta_Y = start.latitude - end.latitude;
        double degree = Math.toDegrees(Math.atan(delta_Y / delta_X));

        if (delta_X > 0)
            degree = degree + 180.0;

        double distance = start.distanceTo(end);

        double upperBound = upperDegree(degree);
        double lowerBound = lowerDegree(degree);

        double upperSideLen = distance / Math.sin(Math.toRadians(alpha))
                * Math.sin(Math.toRadians(degree - lowerBound));
        double lowerSideLen = distance / Math.sin(Math.toRadians(alpha))
                * Math.sin(Math.toRadians(upperBound - degree));

        upper_longitude = start.longitude + upperSideLen * Math.cos(Math.toRadians(upperBound));
        upper_latitude = start.latitude + upperSideLen * Math.sin(Math.toRadians(upperBound));
        lower_longitude = start.longitude + lowerSideLen * Math.cos(Math.toRadians(lowerBound));
        lower_latitude = start.latitude + lowerSideLen * Math.sin(Math.toRadians(lowerBound));

        LongLat upperPoint = new LongLat(upper_longitude, upper_latitude);
        LongLat lowerPoint = new LongLat(lower_longitude, lower_latitude);

        boolean upperCanFly = PathOrganizer.canFly(start, upperPoint, noFlyLongLat)
                && PathOrganizer.canFly(upperPoint, end, noFlyLongLat) && upperPoint.isConfined();
        boolean lowerCanFly = PathOrganizer.canFly(start, lowerPoint, noFlyLongLat)
                && PathOrganizer.canFly(lowerPoint, end, noFlyLongLat) && lowerPoint.isConfined();

        if (upperCanFly) {
            pathLongLats.add(upperPoint);
            pathLongLats.add(end);
            return pathLongLats;
        }
        if (lowerCanFly) {
            pathLongLats.add(lowerPoint);
            pathLongLats.add(end);
            return pathLongLats;
        }
        return null;
    }

}
