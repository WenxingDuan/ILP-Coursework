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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseController {
    String jdbcString;
    Connection conn;
    Statement statement;

    public DatabaseController(String port) throws SQLException {
        this.jdbcString = "jdbc:derby://localhost:" + port + "/derbyDB";
        this.conn = DriverManager.getConnection(jdbcString);
        this.statement = conn.createStatement();
    }

    public List<OrderDetail> orderSearch(String date) throws SQLException {
        List<OrderDetail> orders = new ArrayList<OrderDetail>();

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
    }

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
