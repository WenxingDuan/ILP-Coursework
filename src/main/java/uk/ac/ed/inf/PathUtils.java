/**
 * Class for the tools for organizing the path.
 *
 * @author Duan Wenxing
 */

package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.List;
import java.awt.geom.Line2D;

public class PathUtils {

    /**
     * Method used to calculate the smallest multiple of 10 angle greater than the
     * input angle. (e.g. input 12.4, return 20.0)
     * 
     * @param degree the input degree
     * @return the smallest multiple of 10 angle greater than the input angle
     */
    public static double upperDegree(double degree) {
        double multiple = Math.ceil(degree / 10.0);
        if (Double.doubleToLongBits(multiple * 10) == Double.doubleToLongBits(360.0)) {
            return 0;
        } else {
            return multiple * 10;
        }
    }

    /**
     * Method used to calculate the greatest multiple of 10 angle less than the
     * input angle. (e.g. input 12.4, return 10.0)
     * 
     * @param degree the input degree
     * @return the greatest multiple of 10 angle less than the input angle
     */
    public static double lowerDegree(double degree) {
        double multiple = Math.floor(degree / 10.0);
        return multiple * 10;
    }

    /**
     * Method to check if the line connected the starting point and ending point
     * touches or too close to the no-fly zones.
     * 
     * @param start        the starting position
     * @param end          the ending position
     * @param noFlyLongLat the list of the no-fly zones edgepoints
     * @return true if the connected line between the starting point and ending
     *         point is safe to fly, false otherwise
     */
    public static boolean canFly(LongLat start, LongLat end, List<List<LongLat>> noFlyLongLat) {
        // go over every small zones in the no-fly zones
        for (List<LongLat> currZonePoints : noFlyLongLat) {
            for (int i = 0; i < currZonePoints.size() - 1; i++) {
                // get two adjacent points in the no-fly zone
                LongLat currLinePoint1 = currZonePoints.get(i);
                LongLat currLinePoint2 = currZonePoints.get(i + 1);
                // calculate the distance from 2 points to the fly path
                double point1ToLineDistance = Line2D.ptSegDist(start.longitude, start.latitude, end.longitude,
                        end.latitude, currLinePoint1.longitude, currLinePoint1.latitude);
                double point2ToLineDistance = Line2D.ptSegDist(start.longitude, start.latitude, end.longitude,
                        end.latitude, currLinePoint2.longitude, currLinePoint2.latitude);
                // check if the edge point is too close to the fly path
                boolean pointTooClose = !((point1ToLineDistance > 0.00015) && (point2ToLineDistance > 0.00015));
                // check if the fly path intersect with the no-fly zone edge line
                boolean intersect = Line2D.linesIntersect(start.longitude, start.latitude, end.longitude, end.latitude,
                        currLinePoint1.longitude, currLinePoint1.latitude, currLinePoint2.longitude,
                        currLinePoint2.latitude);
                // return false if fly path is too close to the edge point of it intersect with
                // the no-fly zone edge line
                if (intersect || pointTooClose) {
                    return false;
                }
            }
        }
        // return true if fly path is not too close to the edge point, and it is not
        // intersect with the no-fly zone edge line
        return true;
    }

    /**
     * Method to recursively use a naive way to calculate the path from starting
     * point to ending point by offset the slope of both start and end points by a
     * same increasing degree to form a triangle which meet the condition of the
     * degree with multiple of 10 and not pass the no-fly zones. Math details can be
     * found if Design Document 100.100.100
     * 
     * @param start        the starting position
     * @param end          the ending position
     * @param slope        the degree to be offset on both start and end points on
     *                     both clockwise and counterclockwise direction
     * @param noFlyLongLat the list of the no-fly zones edgepoints
     * 
     * @return key points list on the available path, null if it is not reachable.
     */
    public static List<LongLat> naiveOrganizePath(LongLat start, LongLat end, double slope,
            List<List<LongLat>> noFlyLongLat) {
        // calculate the angle on the middle point between start and end
        double alpha = 180.0 + 10 - slope * 2;
        // if offset degree cannot form a triangle, return null
        if (alpha <= 0)
            return null;
        // use the same offset degree on the start and end points to check if reachable
        List<LongLat> path = advanceOrganizePath(start, end, slope, slope, noFlyLongLat);
        // if not reachable, recursively increase the offset degree until it cannot form
        // a triangle
        if (path == null)
            return advanceOrganizePath(start, end, slope + 10.0, slope + 10.0, noFlyLongLat);
        else
            return path;
    }

    /**
     * Method to use to calculate the path from starting point to ending point by
     * offset the slope of both start and end points by different degree to form a
     * triangle which meet the condition of the degree with multiple of 10. Math
     * details can be found if Design Document 100.100.100
     * 
     * @param start        the starting position
     * @param end          the ending position
     * @param upperSlope   the degree to be offset in the counterclockwise direction
     *                     on starting point / the degree to be offset in the
     *                     clockwise direction on ending point
     * @param lowerSlope   the degree to be offset in the clockwise direction on
     *                     starting point / the degree to be offset in the
     *                     counterclockwise direction on ending point
     * @param noFlyLongLat the list of the no-fly zones edgepoints
     * 
     * @return key points list on the available path, null if it is not reachable.
     */
    public static List<LongLat> advanceOrganizePath(LongLat start, LongLat end, double upperSlope, double lowerSlope,
            List<List<LongLat>> noFlyLongLat) {
        List<LongLat> pathLongLats = new ArrayList<LongLat>();
        pathLongLats.add(start);
        // calculate the angle on the middle point between start and end
        double alpha = 180.0 + 10 - upperSlope - lowerSlope;
        // if offset degree cannot form a triangle, return null
        if (alpha <= 0)
            return null;

        double upper_longitude, upper_latitude, lower_longitude, lower_latitude;
        // difference of longitude
        double delta_X = end.longitude - start.longitude;
        // difference of latitude
        double delta_Y = end.latitude - start.latitude;
        // angle between start and end
        double degree = Math.toDegrees(Math.atan(delta_Y / delta_X));
        // when end is located on the right of the start point, add degree with 180 to
        // eliminate errors caused by arctan
        if (delta_X < 0)
            degree = degree + 180.0;
        // when degree less then 0, add 360 to get the real legal angle
        if (degree < 0)
            degree = degree + 360.0;
        double distance = start.distanceTo(end);
        // calculate the angle of the starting point after offset in the
        // counterclockwise direction
        //
        // when offsetting counterclockwise, upperBound is also the degree on the end
        // point in the triangle formed by start, middle and end
        double upperBound = upperDegree(degree) - 10 + upperSlope;
        // calculate the angle of the starting point after offset in the
        // clockwise direction
        //
        // when offsetting clockwise, lowerBound is also the degree on the end
        // point in the triangle formed by start, middle and end
        double lowerBound = lowerDegree(degree) + 10 - lowerSlope;

        // calculate the length of the triangle formed by start, middle and end
        double upperSideLen = distance / Math.sin(Math.toRadians(alpha))
                * Math.sin(Math.toRadians(degree - lowerBound));
        double lowerSideLen = distance / Math.sin(Math.toRadians(alpha))
                * Math.sin(Math.toRadians(upperBound - degree));

        // calculate the coordinates of the midpoint coordinates of offsetting
        // counterclockwise
        upper_longitude = start.longitude + upperSideLen * Math.cos(Math.toRadians(upperBound));
        upper_latitude = start.latitude + upperSideLen * Math.sin(Math.toRadians(upperBound));
        LongLat upperPoint = new LongLat(upper_longitude, upper_latitude);
        // midpoint coordinates of offsetting clockwise
        lower_longitude = start.longitude + lowerSideLen * Math.cos(Math.toRadians(lowerBound));
        lower_latitude = start.latitude + lowerSideLen * Math.sin(Math.toRadians(lowerBound));
        LongLat lowerPoint = new LongLat(lower_longitude, lower_latitude);
        // check if the midpoints is safe to fly
        boolean upperCanFly = PathUtils.canFly(start, upperPoint, noFlyLongLat)
                && PathUtils.canFly(upperPoint, end, noFlyLongLat) && upperPoint.isConfined();
        boolean lowerCanFly = PathUtils.canFly(start, lowerPoint, noFlyLongLat)
                && PathUtils.canFly(lowerPoint, end, noFlyLongLat) && lowerPoint.isConfined();

        // return the flyable path
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

        // if none of the path is safe, return null
        return null;
    }

    /**
     * Method to find the shortest path among all possible paths using or not using
     * landmarks between the input start and end points
     * 
     * @param start        the starting position
     * @param end          the ending position
     * @param landmarks    the locations of the landmarks
     * @param noFlyLongLat the list of the no-fly zones edgepoints
     * 
     * @return the shortest path key points list
     */
    public static List<LongLat> organizeShortestPath(LongLat start, LongLat end, List<LongLat> landmarks,
            List<List<LongLat>> noFlyLongLat) {
        List<List<LongLat>> allPaths = new ArrayList<List<LongLat>>();
        // find all paths that not using landmarks
        List<List<LongLat>> directPath = findAllPaths(start, end, noFlyLongLat);
        if (directPath.size() != 0)
            allPaths.addAll(directPath);

        // find all paths that using landmarks
        for (LongLat landmark : landmarks) {
            List<LongLat> currPath = PathUtils.organizePathThoughLandmark(start, landmark, end, noFlyLongLat);
            if (currPath != null)
                allPaths.add(currPath);
        }
        // find the shortest among all possible paths
        return chooseShortestPath(allPaths);
    }

    /**
     * Method to find all direct reachable safe paths from start to end
     * 
     * @param start        the starting position
     * @param end          the ending position
     * @param noFlyLongLat the list of the no-fly zones edgepoints
     * 
     * @return the list of all possible paths key points list
     */
    public static List<List<LongLat>> findAllPaths(LongLat start, LongLat end, List<List<LongLat>> noFlyLongLat) {
        List<List<LongLat>> allPaths = new ArrayList<List<LongLat>>();
        // try all possible offset angles of the start and end points
        for (double upperSlope = 0; upperSlope < 180; upperSlope = upperSlope + 10) {
            for (double lowerSlope = 0; lowerSlope < 180; lowerSlope = lowerSlope + 10) {
                List<LongLat> currPaths = PathUtils.advanceOrganizePath(start, end, upperSlope, lowerSlope,
                        noFlyLongLat);
                // only use the safe path
                if (currPaths != null)
                    allPaths.add(currPaths);
            }
        }
        return allPaths;

    }

    /**
     * Method to use the naive way to find all possible paths through landmarks
     * 
     * @param start        the starting position
     * @param landmark     the locations of the landmarks
     * @param end          the ending position
     * @param noFlyLongLat the list of the no-fly zones edgepoints
     * 
     * @return one safe path from start to end through the landmark
     */
    public static List<LongLat> organizePathThoughLandmark(LongLat start, LongLat landmark, LongLat end,
            List<List<LongLat>> noFlyLongLat) {
        List<LongLat> firstPath = naiveOrganizePath(start, landmark, 10.0, noFlyLongLat);
        List<LongLat> secondPath = naiveOrganizePath(landmark, end, 10.0, noFlyLongLat);
        // return the path only when the path is safe to fly
        if (firstPath == null || secondPath == null)
            return null;
        else {
            firstPath.remove(firstPath.size() - 1);
            firstPath.addAll(secondPath);
            return firstPath;
        }

    }

    /**
     * Method to calculate the total distance of an input path key points list
     * 
     * @param pathPoint path key points list
     * 
     * @return the total distance to fly this path
     */
    public static double distanceCalculator(List<LongLat> pathPoint) {
        double distance = 0;
        for (int i = 0; i < pathPoint.size() - 1; i++) {
            distance = distance + pathPoint.get(i).distanceTo(pathPoint.get(i + 1));
        }
        return distance;
    }

    /**
     * Method to calculate the degree of the direct connected line between start and
     * end respect to X axis
     * 
     * @param start the starting position
     * @param end   the ending position
     * 
     * @return the degree of the line between start and end respect to X axis
     */
    public static int degreeTwoPoints(LongLat start, LongLat end) {
        double delta_X = end.longitude - start.longitude;
        double delta_Y = end.latitude - start.latitude;
        double degree = Math.toDegrees(Math.atan(delta_Y / delta_X));
        // when end is located on the right of the start point, add degree with 180 to
        // eliminate errors caused by arctan
        if (delta_X < 0)
            degree = degree + 180.0;
        // when degree less then 0, add 360 to get the real legal angle
        if (degree < 0)
            degree = degree + 360.0;
        degree = Math.round(degree);
        return (int) degree;
    }

    /**
     * Method to find the shortest (the lowest battery cost) path among all input
     * paths
     * 
     * @param pathOptions the list of all paths key points list
     * 
     * @return the paths key points list with the shortest distance (the lowest
     *         battery cost)
     */
    public static List<LongLat> chooseShortestPath(List<List<LongLat>> pathOptions) {
        int shortestOrderIndex = 0;
        int shortestCost = batteryCalculator(pathOptions.get(0));
        // go over all paths find the path index with the lowest battery cost
        for (int i = 0; i < pathOptions.size(); i++) {
            if (batteryCalculator(pathOptions.get(i)) < shortestCost) {
                shortestOrderIndex = i;
                shortestCost = batteryCalculator(pathOptions.get(i));
            }
        }
        return pathOptions.get(shortestOrderIndex);
    }

    /**
     * Method to calculate the battery cost of the input path
     * 
     * @param path the input path
     * 
     * @return the battery cost of the input path
     */
    public static int batteryCalculator(List<LongLat> path) {
        int cost = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            cost = cost + batteryCalculator(path.get(i), path.get(i + 1));
        }
        return cost;
    }

    /**
     * Method to calculate the battery cost to move from the input start to end
     * 
     * @param start the starting position
     * @param end   the ending position
     * 
     * @return the battery cost from the start to end
     */
    public static int batteryCalculator(LongLat start, LongLat end) {
        int cost = (int) Math.floor(start.distanceTo(end) / 0.00015);
        return cost;
    }

    /**
     * Method to remove the same nearby key points in the input path and return
     * 
     * @param longLatList the input path
     * 
     * @return the input path without nearby same point
     */
    public static List<LongLat> removeSameLongLat(List<LongLat> longLatList) {

        for (int i = 0; i < longLatList.size() - 1; i++) {
            if (longLatList.get(i).closeTo(longLatList.get(i + 1)))
                longLatList.remove(i + 1);
        }
        return longLatList;
    }

}
