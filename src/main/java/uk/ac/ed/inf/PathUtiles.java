/**
 * Class for organizing the path.
 *
 * @author Duan Wenxing
 */

package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.List;
import java.awt.geom.Line2D;

public class PathUtiles {

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

                double point1ToLineDistance = Line2D.ptSegDist(start.longitude, start.latitude, end.longitude,
                        end.latitude, currLinePoint1.longitude, currLinePoint1.latitude);
                double point2ToLineDistance = Line2D.ptSegDist(start.longitude, start.latitude, end.longitude,
                        end.latitude, currLinePoint2.longitude, currLinePoint2.latitude);
                boolean pointTooClose = !((point1ToLineDistance > 0.00015) && (point2ToLineDistance > 0.00015));

                boolean intersect = Line2D.linesIntersect(start.longitude, start.latitude, end.longitude, end.latitude,
                        currLinePoint1.longitude, currLinePoint1.latitude, currLinePoint2.longitude,
                        currLinePoint2.latitude);
                if (intersect || pointTooClose) {
                    return false;
                }
            }
        }
        return true;
    }

    public static List<LongLat> naiveOrganizePath(LongLat start, LongLat end, double slope,
            List<List<LongLat>> noFlyLongLat) {

        double alpha = 180.0 + 10 - slope * 2;
        if (alpha <= 0)
            return null;

        List<LongLat> path = advanceOrganizePath(start, end, slope, slope, noFlyLongLat);
        if (path == null)
            return advanceOrganizePath(start, end, slope + 10.0, slope + 10.0, noFlyLongLat);
        else
            return path;
    }

    public static List<LongLat> advanceOrganizePath(LongLat start, LongLat end, double upperSlope, double lowerSlope,
            List<List<LongLat>> noFlyLongLat) {
        List<LongLat> pathLongLats = new ArrayList<LongLat>();
        pathLongLats.add(start);
        double alpha = 180.0 + 10 - upperSlope - lowerSlope;

        if (alpha <= 0)
            return null;

        double upper_longitude, upper_latitude, lower_longitude, lower_latitude;
        double delta_X = end.longitude - start.longitude;
        double delta_Y = end.latitude - start.latitude;
        double degree = Math.toDegrees(Math.atan(delta_Y / delta_X));

        if (delta_X < 0)
            degree = degree + 180.0;
        if (degree < 0)
            degree = degree + 360.0;
            
        double distance = start.distanceTo(end);
        double upperBound = upperDegree(degree) - 10 + upperSlope;
        double lowerBound = lowerDegree(degree) + 10 - lowerSlope;

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

        boolean upperCanFly = PathUtiles.canFly(start, upperPoint, noFlyLongLat)
                && PathUtiles.canFly(upperPoint, end, noFlyLongLat) && upperPoint.isConfined();
        boolean lowerCanFly = PathUtiles.canFly(start, lowerPoint, noFlyLongLat)
                && PathUtiles.canFly(lowerPoint, end, noFlyLongLat) && lowerPoint.isConfined();

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

    public static List<LongLat> organizeShortestPath(LongLat start, LongLat end, List<LongLat> landmarks,
            List<List<LongLat>> noFlyLongLat) {
        List<List<LongLat>> allPaths = new ArrayList<List<LongLat>>();

        List<List<LongLat>> directPath = findAllPaths(start, end, noFlyLongLat);
        if (directPath.size() != 0)
            allPaths.addAll(directPath);

        for (LongLat landmark : landmarks) {
            List<LongLat> currPath = PathUtiles.organizePathThoughLandmark(start, landmark, end, noFlyLongLat);
            if (currPath != null)
                allPaths.add(currPath);
        }
        return chooseShortestPath(allPaths);
    }

    public static List<List<LongLat>> findAllPaths(LongLat start, LongLat end, List<List<LongLat>> noFlyLongLat) {
        List<List<LongLat>> allPaths = new ArrayList<List<LongLat>>();
        for (double upperSlope = 0; upperSlope < 180; upperSlope = upperSlope + 10) {
            for (double lowerSlope = 0; lowerSlope < 180; lowerSlope = lowerSlope + 10) {
                List<LongLat> currPaths = PathUtiles.advanceOrganizePath(start, end, upperSlope, lowerSlope,
                        noFlyLongLat);
                if (currPaths != null)
                    allPaths.add(currPaths);
            }
        }
        return allPaths;

    }

    public static List<LongLat> organizePathThoughLandmark(LongLat start, LongLat landmark, LongLat end,
            List<List<LongLat>> noFlyLongLat) {
        List<LongLat> firstPath = naiveOrganizePath(start, landmark, 10.0, noFlyLongLat);
        List<LongLat> secondPath = naiveOrganizePath(landmark, end, 10.0, noFlyLongLat);
        if (firstPath == null || secondPath == null)
            return null;
        else {
            firstPath.remove(firstPath.size() - 1);
            firstPath.addAll(secondPath);
            return firstPath;
        }

    }

    public static double distanceCalculator(List<LongLat> pathPoint) {
        double distance = 0;
        for (int i = 0; i < pathPoint.size() - 1; i++) {
            distance = distance + pathPoint.get(i).distanceTo(pathPoint.get(i + 1));
        }
        return distance;
    }

    public static int degreeTwoPoints(LongLat start, LongLat end) {
        double delta_X = end.longitude - start.longitude;
        double delta_Y = end.latitude - start.latitude;
        double degree = Math.toDegrees(Math.atan(delta_Y / delta_X));

        if (delta_X < 0)
            degree = degree + 180.0;
        if (degree < 0)
            degree = degree + 360.0;
        degree = Math.round(degree);
        return (int) degree;
    }

    public static List<LongLat> chooseShortestPath(List<List<LongLat>> pathOptions) {
        int shortestOrderIndex = 0;
        int shortestCost = batteryCalculater(pathOptions.get(0));
        for (int i = 0; i < pathOptions.size(); i++) {
            if (batteryCalculater(pathOptions.get(i)) < shortestCost) {
                shortestOrderIndex = i;
                shortestCost = batteryCalculater(pathOptions.get(i));

            }
        }
        return pathOptions.get(shortestOrderIndex);
    }

    public static int batteryCalculater(List<LongLat> path) {
        int cost = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            cost = cost + batteryCalculater(path.get(i), path.get(i + 1));
        }
        return cost;
    }

    public static int batteryCalculater(LongLat start, LongLat end) {
        int cost = (int) Math.floor(start.distanceTo(end) / 0.00015);
        return cost;

    }

    public static List<LongLat> removeSameLongLat(List<LongLat> longLatList) {

        for (int i = 0; i < longLatList.size() - 1; i++) {
            for (int j = i + 1; j < longLatList.size(); j++) {
                if (longLatList.get(i).closeTo(longLatList.get(j)))
                    longLatList.remove(j);
            }
        }
        return longLatList;
    }

}
