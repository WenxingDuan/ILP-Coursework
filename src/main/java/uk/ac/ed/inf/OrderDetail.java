/**
 * Class for strore detail for each order. Used for easy communication between classes.
 *
 * @author Duan Wenxing
 */

package uk.ac.ed.inf;

import java.util.List;

public class OrderDetail {
    public String orderNumber;
    public String deliveryDate;
    public String customer;
    public String deliverTo;
    public List<String> items;

    /**
     * Constructor of OrderDetail class.
     *
     * @param orderNumber  the order number of the order
     * @param deliveryDate the delivery date of the order
     * @param customer     the customer school ID of the order
     * @param deliverTo    the destination WhatThreeWords location of the order
     * @param items        the ordered items of the order
     */
    public OrderDetail(String orderNumber, String deliveryDate, String customer, String deliverTo, List<String> items) {
        this.orderNumber = orderNumber;
        this.deliveryDate = deliveryDate;
        this.customer = customer;
        this.deliverTo = deliverTo;
        this.items = items;
    }

    /**
     * Method to print the order details for testing.
     *
     */
    public void printInformation() {
        System.out.println(orderNumber);
        System.out.println(deliveryDate);
        System.out.println(customer);
        System.out.println(deliverTo);
        System.out.println(String.join(",", items));
    }

}
