/**
 * Class for getting data from Derby server.
 *
 * @author Duan Wenxing
 */
package uk.ac.ed.inf;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUtils {
    private String jdbcString;
    private Connection conn;
    private Statement statement;

    /**
     * Constructor of DatabaseUtils class. In the constructor method, function
     * generate the derbyDB link string delete "deliveries" and "flightpath" table
     * in the database and create new one
     *
     * @param port the communication derbyDB port to the server
     */
    public DatabaseUtils(String port) {
        try {
            this.jdbcString = "jdbc:derby://localhost:" + port + "/derbyDB";
            this.conn = DriverManager.getConnection(jdbcString);
            this.statement = conn.createStatement();
            try {
                statement.execute("drop table deliveries");
                statement.execute("drop table flightpath");
                String deliverCommand = "create table deliveries(orderNo char(8), deliveredTo varchar(19), costInPence int)";
                String pathCommand = "create table flightpath(orderNo char(8), fromLongitude double, fromLatitude double, angle integer, toLongitude double, toLatitude double)";
                statement.execute(deliverCommand);
                statement.execute(pathCommand);
            } catch (Exception e) {
                String deliverCommand = "create table deliveries(orderNo char(8), deliveredTo varchar(19), costInPence int)";
                String pathCommand = "create table flightpath(orderNo char(8), fromLongitude double, fromLatitude double, angle integer, toLongitude double, toLatitude double)";
                statement.execute(deliverCommand);
                statement.execute(pathCommand);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Writ the input information into "deliveries" table.
     *
     * @param orderNumber the order number
     * @param deliveryTo  the order destination
     * @param costInPence the price include delivery fee
     */
    public void writeDeliver(String orderNumber, String deliveryTo, int costInPence) {
        try {
            PreparedStatement psDeliver = conn.prepareStatement("insert into deliveries values (?, ?, ?)");
            psDeliver.setString(1, orderNumber);
            psDeliver.setString(2, deliveryTo);
            psDeliver.setInt(3, costInPence);
            psDeliver.execute();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Writ the input information into "flightpath" table.
     *
     * @param orderNumber the order number
     * @param startLong   the starting point longitude
     * @param startLat    the starting point latitude
     * @param angle       the angle of the flight path (multiples of 10)
     * @param endLong     the ending point longitude
     * @param endLat      the ending point latitude
     */
    public void writePath(String orderNumber, double startLong, double startLat, int angle, double endLong,
            double endLat) {
        try {
            PreparedStatement psPath = conn.prepareStatement("insert into flightpath values (?, ?, ?, ?, ?, ?)");
            psPath.setString(1, orderNumber);
            psPath.setDouble(2, startLong);
            psPath.setDouble(3, startLat);
            psPath.setInt(4, angle);
            psPath.setDouble(5, endLong);
            psPath.setDouble(6, endLat);
            psPath.execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to search the order detail information by the input date
     * 
     * @param date the date to be search
     * @return order detail information in type of the list of {@link OrderDetail}
     */
    public List<OrderDetail> orderSearch(String date) {
        List<OrderDetail> orders = new ArrayList<OrderDetail>();
        try {
            final String orderQuery = "select * from ORDERS where DELIVERYDATE=(?)";
            PreparedStatement psOrderQuery = this.conn.prepareStatement(orderQuery);
            psOrderQuery.setString(1, date);

            ResultSet rs = psOrderQuery.executeQuery();
            while (rs.next()) {
                String orderNumber = rs.getString("ORDERNO");
                String deliveryDate = rs.getString("DELIVERYDATE");
                String customer = rs.getString("CUSTOMER");
                String deliverTo = rs.getString("DELIVERTO");
                List<String> items = orderDetailSearch(orderNumber);
                orders.add(new OrderDetail(orderNumber, deliveryDate, customer, deliverTo, items));
            }
            rs.close();
            return orders;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Method to search the order detail information by the input order number
     * 
     * @param orderNumber the order number to be search
     * @return order detail information in type of list of {@link OrderDetail}
     */
    private List<String> orderDetailSearch(String orderNumber) {
        final String orderDetailQuery = "select * from ORDERDETAILS where ORDERNO=(?)";
        List<String> detailList = new ArrayList<String>();
        try {
            PreparedStatement psOrderDetailQuery = this.conn.prepareStatement(orderDetailQuery);
            psOrderDetailQuery.setString(1, orderNumber);
            ResultSet rs = psOrderDetailQuery.executeQuery();
            while (rs.next()) {
                detailList.add(rs.getString("ITEM"));
            }
            rs.close();
            return detailList;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

}
