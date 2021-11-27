package uk.ac.ed.inf;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.Random;

public class Apptest {
    private Connection conn;
    private Statement statement;
    String DBport = "1527";
    String Webport = "9898";

    @Test
    public void testDBAngle() {
        Random r = new Random();
        int date = r.nextInt(29);
        int month = r.nextInt(13);
        int year = r.nextInt(2) + 2022;

        String[] a = { String.valueOf(date), String.valueOf(month), String.valueOf(year), Webport, DBport };

        App.main(a);

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

    @Test
    public void testPathConnectness() {
        Random r = new Random();
        int date = r.nextInt(28);
        int month = r.nextInt(12);
        int year = r.nextInt(1) + 2022;

        String[] a = { String.valueOf(date), String.valueOf(month), String.valueOf(year), Webport, DBport };

        App.main(a);

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

    @Test
    public void testStartingEndingAPT() {
        Random r = new Random();
        int date = r.nextInt(28);
        int month = r.nextInt(12);
        int year = r.nextInt(1) + 2022;

        String[] a = { String.valueOf(date), String.valueOf(month), String.valueOf(year), Webport, DBport };

        App.main(a);
        LongLat appletonTower = new LongLat(-3.186874, 55.944494);
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

    @Test
    public void testBattery() {
        Random r = new Random();
        int date = r.nextInt(28);
        int month = r.nextInt(12);
        int year = r.nextInt(1) + 2022;

        String[] a = { String.valueOf(date), String.valueOf(month), String.valueOf(year), Webport, DBport };

        App.main(a);
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

    @Test
    public void testEveryday() {
        for (int year = 2022; year <= 2023; year++) {
            for (int month = 1; month <= 12; month++) {
                for (int date = 1; date <= 28; date++) {
                    String[] a = { String.valueOf(date), String.valueOf(month), String.valueOf(year), Webport, DBport };
                    System.err.println(String.valueOf(date));
                    System.err.println(String.valueOf(month));
                    System.err.println(String.valueOf(year));
                    System.err.println("===============================");
                    App.main(a);
                }
            }
        }
    }

    // =================================================================
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
}
