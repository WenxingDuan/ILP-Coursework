/**
 * Class to build the path for the specific date
 *
 * @author Duan Wenxing
 */

package uk.ac.ed.inf;

import java.util.List;
import java.util.ArrayList;

public class PathBuilder {
    private final LongLat appletonTower = new LongLat(-3.186874, 55.944494);

    private MenuUtils menuUtils;
    private LongLatCatcher longLatCatcher;
    private DatabaseUtils databaseUtils;
    private GeoJsonUtils geoJsonUtils;
    private List<LongLat> landmarks;
    private List<List<LongLat>> noFlyLongLat;

    /**
     * The class stores a single straight movement.
     */
    class Move {
        int batteryCost;
        int angle;
        LongLat startLocation;
        LongLat endLocation;

        /**
         * Constructor of Move class
         * 
         * @param batteryCost   the battery cost of this movement
         * @param angle         the angle of this movement
         * @param startLocation the starting position of this movement
         * @param endLocation   the destination of this movement
         */
        Move(int batteryCost, int angle, LongLat startLocation, LongLat endLocation) {
            this.batteryCost = batteryCost;
            this.angle = angle;
            this.startLocation = startLocation;
            this.endLocation = endLocation;
        }
    }

    /**
     * The class stores the detail of an order used to write into deliveries table.
     */
    class OrderDestination {
        String orderNumber;
        List<LongLat> destinations;
        int cost;
        String deliverTo;

        /**
         * Constructor of OrderDestination class.
         * 
         * @param orderNumber  the order number of this order
         * @param destinations the fly path of the order
         * @param cost         the cost of this order
         * @param deliverTo    the final destination of this order
         */
        OrderDestination(String orderNumber, List<LongLat> destinations, int cost, String deliverTo) {
            this.orderNumber = orderNumber;
            this.destinations = destinations;
            this.cost = cost;
            this.deliverTo = deliverTo;
        }

    }

    /**
     * Constructor of PathBuilder class.
     * 
     * @param webPort the communication web port to the server
     * @param dbPort  the communication derbyDB port to the server
     */
    public PathBuilder(String webPort, String dbPort) {
        this.longLatCatcher = new LongLatCatcher(webPort);
        this.databaseUtils = new DatabaseUtils(dbPort);
        this.menuUtils = new MenuUtils(webPort);
        this.geoJsonUtils = new GeoJsonUtils(webPort);
        this.landmarks = this.geoJsonUtils.getLandmarksLongLat();
        this.noFlyLongLat = this.geoJsonUtils.getNoFlyLongLat();
    }

    /**
     * Method to build the complete path for all possible orders in the input date
     * without run out of the battery, and write the information to the database,
     * also generate a geojson file.
     * 
     * @param date the date to build the path for
     */
    public void buildPath(String date) {
        // get the order details and complete path without concerning the step length
        // and battery for the day
        List<OrderDestination> finalPath = generatePath(date);
        List<LongLat> percisePath;
        // write the order details into deliveries table
        writeDeliveriesTable(finalPath);
        // find the path when concerning the step limit of 0.00015 unit
        percisePath = writeFlightpathTable(finalPath);
        // write the path details into flightpath table
        geoJsonUtils.storeFlightPath(percisePath, date);

    }


    /**
     * Method to get the orders details and generate complete path without
     * concerning the step length for the input date
     * 
     * @param date the date to be used
     * 
     * @return the order details and path for every order in type of list of
     *         {@link OrderDestination}
     */
    private List<OrderDestination> generatePath(String date) {
        int battery = 1500;
        // find the order information from database
        List<OrderDetail> orders = databaseUtils.orderSearch(date);
        // extract the all destinations of all the order in the day
        List<OrderDestination> destinationList = getDestination(orders);
        // organize the path when concern the angle limit but not when concern the step
        // length limit
        List<OrderDestination> finalPath = findDeliverPath(battery, destinationList);
        return finalPath;
    }

    /**
     * Method to write delivered the order information into deliveries table
     * 
     * @param finalPath the list contain all delivered order information
     */
    private void writeDeliveriesTable(List<OrderDestination> finalPath) {
        for (OrderDestination orderDestination : finalPath) {
            // ignore the final retuning to appleton tower process
            if (!orderDestination.orderNumber.equals("00000000")) {
                databaseUtils.writeDeliver(orderDestination.orderNumber, orderDestination.deliverTo,
                        orderDestination.cost);
            }
        }
    }

    /**
     * Method to write the precise path when concern both step length limit and
     * angle limit into flightpath table, and return the precise path as a list of
     * key points
     * 
     * @param finalPath the list contain the path not when concern the step length
     *                  limit
     * 
     * @return the precise path when concern both step length limit and angle limit
     *         in type of list of {@link LongLat}
     */
    private List<LongLat> writeFlightpathTable(List<OrderDestination> finalPath) {
        List<LongLat> precisePath = new ArrayList<LongLat>();
        precisePath.add(this.appletonTower);
        LongLat currentPosition = finalPath.get(0).destinations.get(0);
        // go over all orders
        for (int i = 0; i < finalPath.size(); i++) {
            OrderDestination orderDestination = finalPath.get(i);
            // go over the waypoint in the order
            for (int j = 0; j < orderDestination.destinations.size() - 1; j++) {

                LongLat nextPosition = orderDestination.destinations.get(j + 1);
                LongLat planingCurrPosition = orderDestination.destinations.get(j);
                // check if the drone need to hovering
                Move samePoint = movementCalculator(planingCurrPosition, nextPosition, orderDestination.orderNumber);
                if (samePoint.angle == -999) {
                    // write the movement information into flightpath table
                    databaseUtils.writePath(orderDestination.orderNumber, currentPosition.longitude,
                            currentPosition.latitude, -999, currentPosition.longitude, currentPosition.latitude);
                    // currentPosition = samePoint.endLocation;
                    precisePath.add(currentPosition);
                } else {
                    // write the movement information into flightpath table
                    Move move = movementCalculator(currentPosition, nextPosition, orderDestination.orderNumber);
                    // if the organize path distance is too small, ignore it
                    if (move.batteryCost == 0)
                        continue;
                    databaseUtils.writePath(orderDestination.orderNumber, move.startLocation.longitude,
                            move.startLocation.latitude, move.angle, move.endLocation.longitude,
                            move.endLocation.latitude);
                    // System.err.println(String.valueOf( move.startLocation.longitude) + " ,4 " +
                    // String.valueOf( move.startLocation.latitude));

                    currentPosition = move.endLocation;
                    precisePath.add(currentPosition);

                    // dealing with the last order before returning to Appleton Tower
                    if ((j + 1 == orderDestination.destinations.size() - 1) && (i != finalPath.size() - 1)) {
                        databaseUtils.writePath(orderDestination.orderNumber, currentPosition.longitude,
                                currentPosition.latitude, -999, currentPosition.longitude, currentPosition.latitude);
                        precisePath.add(currentPosition);
                        currentPosition = move.endLocation;
                    }
                }
            }
        }

        return precisePath;

    }

    /**
     * Method to find a path for the input order, when concern the battery and angle
     * limit, but not when concern the step length limit.
     * 
     * @param battery         total battery available
     * @param destinationList the list store all the necessary waypoint for all
     *                        orders
     * 
     * @return the list contain precise path when concern battery and angle limit
     *         for every order in type of list of {@link OrderDestination}
     */
    private List<OrderDestination> findDeliverPath(int battery, List<OrderDestination> destinationList) {
        List<OrderDestination> path = new ArrayList<OrderDestination>();
        LongLat currentPosition = appletonTower;
        int hoverBatteryCost = 0;
        // go over all orders
        for (OrderDestination orderDestination : destinationList) {
            List<LongLat> tempPath = new ArrayList<LongLat>();
            hoverBatteryCost = orderDestination.destinations.size();

            for (LongLat nextDestination : orderDestination.destinations) {
                if (currentPosition.closeTo(nextDestination))
                    continue;
                // find the path between two waypoints
                tempPath.addAll(PathUtils.organizeShortestPath(currentPosition, nextDestination, this.landmarks,
                        this.noFlyLongLat));
                currentPosition = nextDestination;
            }

            // check if the drone have enough battery to finish the current order and return
            int orderBatteryCost = PathUtils.batteryCalculator(tempPath) + hoverBatteryCost;
            int backBatteryCost = PathUtils.batteryCalculator(currentPosition, this.appletonTower);
            // if battery is enough, add to the returning path
            if (battery - (orderBatteryCost + backBatteryCost) > 0) {
                battery = battery - orderBatteryCost;
                path.add(new OrderDestination(orderDestination.orderNumber, tempPath, orderDestination.cost,
                        orderDestination.deliverTo));
            }
            // if battery is not enough\
            // returning to Appleton Tower immediately and set order number to 00000000
            else {
                OrderDestination lastOrderDestination = path.get(path.size() - 1);
                List<LongLat> lastDestinations = lastOrderDestination.destinations;
                currentPosition = lastDestinations.get(lastDestinations.size() - 1);
                tempPath = PathUtils.organizeShortestPath(currentPosition, this.appletonTower, this.landmarks,
                        this.noFlyLongLat);
                currentPosition = tempPath.get(tempPath.size() - 1);
                path.add(new OrderDestination("00000000", tempPath, 0, ""));
                break;
            }
            OrderDestination lastOrderDestination = path.get(path.size() - 1);
            List<LongLat> lastDestinations = lastOrderDestination.destinations;
            currentPosition = lastDestinations.get(lastDestinations.size() - 1);
        }
        // if battery is enough to deliver all orders
        // returning to Appleton Tower and set order number to 00000000
        String orderNumber = "00000000";
        if (!currentPosition.closeTo(this.appletonTower)) {
            List<LongLat> tempPath = PathUtils.organizeShortestPath(currentPosition, this.appletonTower, this.landmarks,
                    this.noFlyLongLat);
            path.add(new OrderDestination(orderNumber, tempPath, 0, ""));
        }

        return path;
    }

    /**
     * Method to calculate the battery cost and precise location when concern the
     * step length limit
     * 
     * @param start       the starting position
     * @param end         the ending position
     * @param orderNumber the order number
     * 
     * @return the object contain battery cost and precise location in type of
     *         {@link Move}
     */
    private Move movementCalculator(LongLat start, LongLat end, String orderNumber) {
        // check if is hovering
        if (start.samePoint(end))
            return new Move(0, -999, start, start);
        // calculate the angle
        int angle = PathUtils.degreeTwoPoints(start, end);
        double angleDigit = (double) angle;
        angleDigit = angleDigit / 10;
        angleDigit = Math.round(angleDigit) * 10;
        angle = (int) angleDigit;
        if (angle == 360)
            angle = 0;
        int step = 0;

        LongLat currentLongLat = new LongLat(start.longitude, start.latitude);
        Double distance = currentLongLat.distanceTo(end);

        // moving step by step along the path
        while (true) {
            LongLat tempLongLat;
            // if reach the destination
            if (currentLongLat.closeTo(end))
                break;

            tempLongLat = currentLongLat.nextPosition(angle);
            // check if the distance between current position and destination is decreasing
            //
            // see detail in project report 2.2.5
            if (tempLongLat.distanceTo(end) < distance) {
                step++;
                distance = tempLongLat.distanceTo(end);
                currentLongLat = new LongLat(tempLongLat.longitude, tempLongLat.latitude);
            }
            // drone swept across the edge of the destination's approach range.
            else {
                // write into database before changing the moving angle
                databaseUtils.writePath(orderNumber, start.longitude, start.latitude, angle, currentLongLat.longitude,
                        currentLongLat.latitude);
                // store the starting position
                LongLat fixStart = new LongLat(currentLongLat.longitude, currentLongLat.latitude);
                // calculate the new moving angle
                angle = PathUtils.degreeTwoPoints(currentLongLat, end);
                angleDigit = (double) angle;
                angleDigit = angleDigit / 10;
                angleDigit = Math.round(angleDigit) * 10;
                angle = (int) angleDigit;
                if (angle == 360) {
                    angle = 0;
                }
                step = 0;
                // move along new angle until close to the final destination
                while (!currentLongLat.closeTo(end)) {
                    step = step + 1;
                    currentLongLat = currentLongLat.nextPosition(angle);
                }
                return new Move(step, angle, fixStart, currentLongLat);
            }

        }
        return new Move(step, angle, start, currentLongLat);
    }

    /**
     * Method to sort the order by the cost in pence in decreasing order
     * 
     * @param orders the order list to be sort
     * 
     * @return sorted order detail list
     */
    private List<OrderDetail> sortByValue(List<OrderDetail> orders) {
        List<OrderDetail> sortedOrders = new ArrayList<OrderDetail>();
        while (true) {
            int highestOrderIndex = 0;
            int highestCost = 0;
            for (int i = 0; i < orders.size(); i++) {
                if (menuUtils.getDeliveryCost(orders.get(i).items) > highestCost) {
                    highestOrderIndex = i;
                    highestCost = menuUtils.getDeliveryCost(orders.get(i).items);
                }
            }
            sortedOrders.add(orders.get(highestOrderIndex));
            orders.remove(highestOrderIndex);
            if (orders.isEmpty())
                break;
        }
        return sortedOrders;
    }

    /**
     * Method to transform the order detail list to a list of
     * {@link OrderDestination} include all necessary waypoints
     * 
     * @param orders the order detail list
     * 
     * @return a list of object in type of {@link OrderDestination} contain all
     *         necessary waypoints and order details
     */
    private List<OrderDestination> getDestination(List<OrderDetail> orders) {

        List<OrderDestination> destinationList = new ArrayList<OrderDestination>();
        List<OrderDetail> ordersItem = sortByValue(orders);
        for (OrderDetail orderDetail : ordersItem) {
            List<LongLat> currOrderDestination = getOrderDestination(orderDetail);
            destinationList.add(new OrderDestination(orderDetail.orderNumber, currOrderDestination,
                    menuUtils.getDeliveryCost(orderDetail.items), orderDetail.deliverTo));
        }
        return destinationList;
    }

    /**
     * Method to extract the order waypoints detail from a {@link OrderDetail}
     * object
     * 
     * @param order the order detail
     * 
     * @return a path include all necessary waypoints of the input order
     */
    private List<LongLat> getOrderDestination(OrderDetail order) {
        List<LongLat> orderDestination = new ArrayList<LongLat>();
        List<String> items = order.items;
        for (String item : items) {
            String location = menuUtils.getLocation(item);
            LongLat storeLongLat = longLatCatcher.getCenterLongLat(location);
            orderDestination.add(storeLongLat);
        }
        orderDestination = PathUtils.removeSameLongLat(orderDestination);
        LongLat deliverTo = longLatCatcher.getCenterLongLat(order.deliverTo);
        orderDestination.add(deliverTo);
        return orderDestination;
    }

}
