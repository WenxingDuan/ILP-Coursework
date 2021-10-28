/**
 * Class for strore detail for each order
 *
 * @author Duan Wenxing
 */

package uk.ac.ed.inf;

import java.util.List;

public class OrderDetail {
    String orderNumber;
    String deliveryDate;
    String customer;
    String deliverTo;
    List<String> items;

    public OrderDetail(String orderNumber, String deliveryDate, String customer, String deliverTo, List<String> items) {
        this.orderNumber = orderNumber;
        this.deliveryDate = deliveryDate;
        this.customer = customer;
        this.deliverTo = deliverTo;
        this.items = items;
    }

    public void printInformation() {
        System.out.println(orderNumber);
        System.out.println(deliveryDate);
        System.out.println(customer);
        System.out.println(deliverTo);
        System.out.println(String.join(",", items));
    }

}
