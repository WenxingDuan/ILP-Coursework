/**
 * Enpty class
 *
 * @author Duan Wenxing
 */
package uk.ac.ed.inf;

import java.util.List;

public class App {
    public static void main(String[] args) {
        try {
            DatabaseHandler d = new DatabaseHandler("1527");
            List<OrderDetail> data = d.orderSearch("2022-01-06");
            for(OrderDetail orderDetail : data){
                orderDetail.printInformation();
                System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
