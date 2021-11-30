package uk.ac.ed.inf;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.awt.geom.Line2D;

import static org.junit.Assert.*;
import org.junit.Test;

public class Apptest {
    private Connection conn;
    private Statement statement;
    String DBport = "1527";
    String Webport = "9898";
    // ================================================================
    PathBuilder p = new PathBuilder(Webport, DBport);
    List<List<LongLat>> noFlyLongLat = PathBuilder.noFlyLongLat;
    // ================================================================
    LongLat appletonTower = new LongLat(-3.186874, 55.944494);

    @Test
    public void testEverydayEverything() {
        for (int year = 2022; year <= 2023; year++) {
            for (int month = 1; month <= 12; month++) {
                for (int date = 1; date <= 28; date++) {
                    String[] a = { String.valueOf(date), String.valueOf(month), String.valueOf(year), Webport, DBport };
                    System.err.println("" + year + " " + month + " " + date + " ");
                    App.main(a);

                    // =============================================
                    // ================Test Angle===================
                    // =============================================
                    System.err.println("Testing Angle Correctness");
                    try {
                        testDBAngle();
                        System.err.println("  --success");
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("!!!!!!!!!!!!!!!!!");
                        System.err.println("  --Unsuccess--  ");
                        System.err.println("!!!!!!!!!!!!!!!!!");
                    }

                    // =============================================
                    // ===============connectness===================
                    // =============================================
                    System.err.println("Testing Path Points Connectness");
                    try {
                        testPathConnectness();
                        System.err.println("  --success");
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("!!!!!!!!!!!!!!!!!");
                        System.err.println("  --Unsuccess--  ");
                        System.err.println("!!!!!!!!!!!!!!!!!");
                    }
                    // =============================================
                    // ===============Start end APT=================
                    // =============================================
                    System.err.println("Testing Start End at APT");
                    try {
                        testStartingEndingAPT();
                        System.err.println("  --success");
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("!!!!!!!!!!!!!!!!!");
                        System.err.println("  --Unsuccess--  ");
                        System.err.println("!!!!!!!!!!!!!!!!!");
                    }
                    // =============================================
                    // =================== Battery =================
                    // =============================================
                    System.err.println("Testing Battery Enough");
                    try {
                        testBattery();
                        System.err.println("  --success");
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("!!!!!!!!!!!!!!!!!");
                        System.err.println("  --Unsuccess--  ");
                        System.err.println("!!!!!!!!!!!!!!!!!");
                    }
                    // =============================================
                    // =================== No-Fly =================
                    // =============================================
                    System.err.println("Testing No-Fly Zone");
                    try {
                        testNoFlyZone();

                        System.err.println("  --success");
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("!!!!!!!!!!!!!!!!!");
                        System.err.println("  --Unsuccess--  ");
                        System.err.println("!!!!!!!!!!!!!!!!!");
                    }

                    System.err.println("===============================");
                    System.err.println("");
                }
            }
        }
        System.err.println("Everything Success");

    }

    // =================================================================

    private void testDBAngle() {
        final String pathQuery = "select * from FLIGHTPATH";
        String jdbcString = "jdbc:derby://localhost:" + DBport + "/derbyDB";
        try {
            conn = DriverManager.getConnection(jdbcString);
            statement = conn.createStatement();
            PreparedStatement psOrderQuery = this.conn.prepareStatement(pathQuery);
            ResultSet rs = psOrderQuery.executeQuery();
            while (rs.next()) {
                Double start_x = rs.getDouble("FROMLONGITUDE");
                Double start_y = rs.getDouble("FROMLATITUDE");
                int angle = rs.getInt("ANGLE");
                Double end_x = rs.getDouble("TOLONGITUDE");
                Double end_y = rs.getDouble("TOLATITUDE");
                if (angle == -999) {
                    assertEquals(start_x, end_x);
                    assertEquals(start_y, end_y);
                } else {
                    assertEquals(degreeTwoPoints(start_x, start_y, end_x, end_y), angle);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void testPathConnectness() {

        final String pathQuery = "select * from FLIGHTPATH";
        String jdbcString = "jdbc:derby://localhost:" + DBport + "/derbyDB";

        try {
            conn = DriverManager.getConnection(jdbcString);
            statement = conn.createStatement();
            PreparedStatement psOrderQuery = this.conn.prepareStatement(pathQuery);
            ResultSet rs = psOrderQuery.executeQuery();
            rs.next();
            Double last_x = rs.getDouble("TOLONGITUDE");
            Double last_y = rs.getDouble("TOLATITUDE");
            while (rs.next()) {
                Double start_x = rs.getDouble("FROMLONGITUDE");
                Double start_y = rs.getDouble("FROMLATITUDE");
                assertEquals(last_x, start_x);
                assertEquals(last_y, start_y);
                last_x = rs.getDouble("TOLONGITUDE");
                last_y = rs.getDouble("TOLATITUDE");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void testStartingEndingAPT() {
        final String pathQuery = "select * from FLIGHTPATH";
        String jdbcString = "jdbc:derby://localhost:" + DBport + "/derbyDB";

        try {
            conn = DriverManager.getConnection(jdbcString);
            statement = conn.createStatement();
            PreparedStatement psOrderQuery = this.conn.prepareStatement(pathQuery);
            ResultSet rs = psOrderQuery.executeQuery();
            rs.next();
            Double end_x = rs.getDouble("TOLONGITUDE");
            Double end_y = rs.getDouble("TOLONGITUDE");
            Double start_x = rs.getDouble("FROMLONGITUDE");
            Double start_y = rs.getDouble("FROMLATITUDE");
            LongLat start = new LongLat(start_x, start_y);
            while (rs.next()) {
                end_x = rs.getDouble("TOLONGITUDE");
                end_y = rs.getDouble("TOLATITUDE");
            }
            LongLat end = new LongLat(end_x, end_y);
            assertTrue(start.closeTo(end));
            assertTrue(start.closeTo(appletonTower));
            assertTrue(end.closeTo(appletonTower));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void testBattery() {

        final String pathQuery = "select * from FLIGHTPATH";
        String jdbcString = "jdbc:derby://localhost:" + DBport + "/derbyDB";
        int battery = 0;
        try {
            conn = DriverManager.getConnection(jdbcString);
            statement = conn.createStatement();
            PreparedStatement psOrderQuery = this.conn.prepareStatement(pathQuery);
            ResultSet rs = psOrderQuery.executeQuery();
            while (rs.next()) {
                Double start_x = rs.getDouble("FROMLONGITUDE");
                Double start_y = rs.getDouble("FROMLATITUDE");
                Double end_x = rs.getDouble("TOLONGITUDE");
                Double end_y = rs.getDouble("TOLATITUDE");
                battery = battery + batteryCalculator(start_x, start_y, end_x, end_y);
            }
            assertTrue(battery <= 1500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void testNoFlyZone() {
        final String pathQuery = "select * from FLIGHTPATH";
        String jdbcString = "jdbc:derby://localhost:" + DBport + "/derbyDB";
        try {
            conn = DriverManager.getConnection(jdbcString);
            statement = conn.createStatement();
            PreparedStatement psOrderQuery = this.conn.prepareStatement(pathQuery);
            ResultSet rs = psOrderQuery.executeQuery();
            while (rs.next()) {
                Double start_x = rs.getDouble("FROMLONGITUDE");
                Double start_y = rs.getDouble("FROMLATITUDE");
                LongLat start = new LongLat(start_x, start_y);
                Double end_x = rs.getDouble("TOLONGITUDE");
                Double end_y = rs.getDouble("TOLATITUDE");
                LongLat end = new LongLat(end_x, end_y);
                assertEquals(true, canFly(start, end, noFlyLongLat));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to calculate the degree of the direct connected line between start and
     * end respect to X axis
     *
     * @param start_x the starting position
     * @param start_y the ending position
     * @param end_x   the starting position
     * @param end_y   the ending position
     * 
     * @return the degree of the line between start and end respect to X axis
     */
    private int degreeTwoPoints(Double start_x, Double start_y, Double end_x, Double end_y) {
        double delta_X = end_x - start_x;
        double delta_Y = end_y - start_y;
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
     * Method to calculate the battery cost to move from the input start to end
     *
     * @param start_x the starting position
     * @param start_y the ending position
     * @param end_x   the starting position
     * @param end_y   the ending position
     * 
     * @return the battery cost from the start to end
     */
    private int batteryCalculator(Double start_x, Double start_y, Double end_x, Double end_y) {
        double distance = Math.sqrt(Math.pow(start_x - end_x, 2) + Math.pow(start_y - end_y, 2));
        int cost = (int) Math.floor(distance / 0.00015);
        return cost;
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
    private boolean canFly(LongLat start, LongLat end, List<List<LongLat>> noFlyLongLat) {
        // go over every small zones in the no-fly zones
        for (List<LongLat> currZonePoints : noFlyLongLat) {
            for (int i = 0; i < currZonePoints.size() - 1; i++) {
                // get two adjacent points in the no-fly zone
                LongLat currLinePoint1 = currZonePoints.get(i);
                LongLat currLinePoint2 = currZonePoints.get(i + 1);
                // check if the edge point is too close to the fly path
                // check if the fly path intersect with the no-fly zone edge line
                boolean intersect = Line2D.linesIntersect(start.longitude, start.latitude, end.longitude, end.latitude,
                        currLinePoint1.longitude, currLinePoint1.latitude, currLinePoint2.longitude,
                        currLinePoint2.latitude);
                // return false if fly path is too close to the edge point of it intersect with
                // the no-fly zone edge line
                if (intersect) {
                    return false;
                }
            }
        }
        // return true if fly path is not too close to the edge point, and it is not
        // intersect with the no-fly zone edge line
        return true;
    }
}
