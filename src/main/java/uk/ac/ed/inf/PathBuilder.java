package uk.ac.ed.inf;

import java.util.List;
import java.util.ArrayList;

public class PathBuilder {
    private final LongLat appletonTower = new LongLat(-3.186874, 55.944494);

    private MenuController menuController;
    private LongLatCatcher longLatCatcher;
    private DatabaseController databaseController;
    private GeoController geoController;
    private final List<LongLat> landmarks;
    private final List<List<LongLat>> noFlyLongLat;

    class Move {
        int batteryCost;
        int angle;
        LongLat endLocation;

        Move(int batteryCost, int angle, LongLat endLocation) {
            this.batteryCost = batteryCost;
            this.angle = angle;
            this.endLocation = endLocation;
        }

        // LongLat returnEndLongLat(){
        // return new LongLat(this.batteryCost)
        // }
    }

    class OrderDestination {
        String orderNumber;
        List<LongLat> destinations;
        int cost;
        String deliverTo;

        OrderDestination(String orderNumber, List<LongLat> destinations, int cost, String deliverTo) {
            this.orderNumber = orderNumber;
            this.destinations = destinations;
            this.cost = cost;
            this.deliverTo = deliverTo;
        }

    }

    public PathBuilder(String webPort, String dbPort) {
        this.longLatCatcher = new LongLatCatcher(webPort);
        this.databaseController = new DatabaseController(dbPort);
        this.menuController = new MenuController(webPort);
        this.geoController = new GeoController(webPort);
        this.landmarks = this.geoController.getLandmarksLongLat();
        this.noFlyLongLat = this.geoController.getNoFlyLongLat();
    }

    public void buildPath(String date) {
        List<OrderDestination> finalPath = generatePath(date);
        List<LongLat> percisePath;
        writeDeliveriesTable(finalPath);
        percisePath = writeFlightpathTable(finalPath);
        geoController.storeFlightPath(percisePath, date);

    }

    // ================================================================================================

    private List<OrderDestination> generatePath(String date) {
        int battery = 1500;
        List<OrderDetail> orders = databaseController.orderSearch(date);
        List<OrderDestination> destinationList = getDestination(orders);
        List<OrderDestination> finalPath = findDeliverPath(battery, destinationList);
        return finalPath;
    }

    private void writeDeliveriesTable(List<OrderDestination> finalPath) {
        for (OrderDestination orderDestination : finalPath) {
            if (!orderDestination.orderNumber.equals("00000000")) {
                databaseController.writeDeliver(orderDestination.orderNumber, orderDestination.deliverTo,
                        orderDestination.cost);
            }
        }
    }

    private List<LongLat> writeFlightpathTable(List<OrderDestination> finalPath) {

        List<LongLat> precisePath = new ArrayList<LongLat>();
        precisePath.add(this.appletonTower);
        System.out.println("[" + this.appletonTower.longitude + "," + this.appletonTower.latitude + "],");
        LongLat currentPosition = finalPath.get(0).destinations.get(0);

        for (int i = 0; i < finalPath.size(); i++) {
            OrderDestination orderDestination = finalPath.get(i);
            // System.out.println("[---------------------------------------------------------------------");

            for (int j = 0; j < orderDestination.destinations.size() - 1; j++) {

                // System.out.println("[" + orderDestination.destinations.get(j).longitude + ","
                // + orderDestination.destinations.get(j).latitude + "],");

                LongLat nextPosition = finalPath.get(i).destinations.get(j + 1);
                LongLat planingCurrPosition = finalPath.get(i).destinations.get(j);
                Move samePoint = movementCalculater(planingCurrPosition, nextPosition);

                // System.out.println("[---------------------------------------------------------------------");
                // System.out.println("[" + currentPosition.longitude + "," +
                // currentPosition.latitude + "],");
                // System.out.println("[" + nextPosition.longitude + "," + nextPosition.latitude
                // + "],");
                // System.out.println("[---------------------------------------------------------------------");
                if (samePoint.angle == -999) {
                    databaseController.writePath(orderDestination.orderNumber, currentPosition.longitude,
                            currentPosition.latitude, -999, currentPosition.longitude, currentPosition.latitude);
                    precisePath.add(currentPosition);
                } else {
                    Move move = movementCalculater(currentPosition, nextPosition);
                    databaseController.writePath(orderDestination.orderNumber, currentPosition.longitude,
                            currentPosition.latitude, move.angle, move.endLocation.longitude,
                            move.endLocation.latitude);
                    currentPosition = move.endLocation;

                    precisePath.add(currentPosition);
                    System.out.println("[" + currentPosition.longitude + "," + currentPosition.latitude + "],");

                    if ((j + 1 == orderDestination.destinations.size() - 1) && (i != finalPath.size() - 1)) {
                        databaseController.writePath(orderDestination.orderNumber, currentPosition.longitude,
                                currentPosition.latitude, -999, currentPosition.longitude, currentPosition.latitude);
                        precisePath.add(currentPosition);
                        currentPosition = move.endLocation;
                    }
                }
            }
            // System.out.println("[---------------------------------------------------------------------");

        }

        return precisePath;

    }

    private List<OrderDestination> findDeliverPath(int battery, List<OrderDestination> destinationList) {
        List<OrderDestination> path = new ArrayList<OrderDestination>();
        // path.add(appletonTower);
        LongLat currentPosition = appletonTower;
        int hoverBatteryCost = 0;
        for (OrderDestination orderDestination : destinationList) {
            List<LongLat> tempPath = new ArrayList<LongLat>();

            hoverBatteryCost = orderDestination.destinations.size();
            for (LongLat nextDestination : orderDestination.destinations) {

                if (currentPosition.closeTo(nextDestination))
                    continue;
                tempPath.addAll(PathUtiles.organizeShortestPath(currentPosition, nextDestination, this.landmarks,
                        this.noFlyLongLat));

                currentPosition = nextDestination;
            }

            int orderBatteryCost = PathUtiles.batteryCalculater(tempPath) + hoverBatteryCost;
            int backBatteryCost = PathUtiles.batteryCalculater(currentPosition, this.appletonTower);

            if (battery - (orderBatteryCost + backBatteryCost) > 0) {
                battery = battery - orderBatteryCost;
                // tempPath.remove(0);
                path.add(new OrderDestination(orderDestination.orderNumber, tempPath, orderDestination.cost,
                        orderDestination.deliverTo));
            } else {
                OrderDestination lastOrderDestination = path.get(path.size() - 1);
                List<LongLat> lastDestinations = lastOrderDestination.destinations;
                currentPosition = lastDestinations.get(lastDestinations.size() - 1);
                tempPath = PathUtiles.organizeShortestPath(currentPosition, this.appletonTower, this.landmarks,
                        this.noFlyLongLat);

                path.add(new OrderDestination("00000000", tempPath, 0, ""));
                break;
            }
            OrderDestination lastOrderDestination = path.get(path.size() - 1);
            List<LongLat> lastDestinations = lastOrderDestination.destinations;
            currentPosition = lastDestinations.get(lastDestinations.size() - 1);

        }

        String orderNumber = "00000000";
        if (!currentPosition.closeTo(this.appletonTower)) {
            List<LongLat> tempPath = PathUtiles.organizeShortestPath(currentPosition, this.appletonTower,
                    this.landmarks, this.noFlyLongLat);
            // tempPath.remove(0);
            path.add(new OrderDestination(orderNumber, tempPath, 0, ""));

        }

        return path;
    }

    private Move movementCalculater(LongLat start, LongLat end) {
        if (start.samePoint(end)) {
            // System.out.println("========");
            return new Move(0, -999, start);
        }

        int angle = PathUtiles.degreeTwoPoints(start, end);
        // System.out.println(angle);
        double angleDigit = (double) angle;
        angleDigit = angleDigit / 10;
        // System.out.println(angleDigit);
        angleDigit = Math.round(angleDigit) * 10;
        angle = (int) angleDigit;
        // System.out.println(angle);
        int step = 0;
        while (true) {
            if (start.closeTo(end))
                break;
            else {
                step++;
                // System.out.println(angle);
                start = start.nextPosition(angle);
            }
        }
        // System.out.println(step);

        return new Move(step, angle, start);
    }

    private List<OrderDetail> sortByValue(List<OrderDetail> orders) {
        List<OrderDetail> sortedOrders = new ArrayList<OrderDetail>();
        // orders.get(0).printInformation();

        while (true) {
            // System.out.println(orders.size());
            // orders.get(0).printInformation();

            int highestOrderIndex = 0;
            int highestCost = 0;

            for (int i = 0; i < orders.size(); i++) {
                if (menuController.getDeliveryCost(orders.get(i).items) > highestCost) {
                    highestOrderIndex = i;
                    highestCost = menuController.getDeliveryCost(orders.get(i).items);
                }
            }
            sortedOrders.add(orders.get(highestOrderIndex));
            orders.remove(highestOrderIndex);
            if (orders.isEmpty())
                break;
        }
        return sortedOrders;
    }

    private List<OrderDestination> getDestination(List<OrderDetail> orders) {

        List<OrderDestination> destinationList = new ArrayList<OrderDestination>();
        List<OrderDetail> ordersItem = sortByValue(orders);
        // ordersItem = sortByValue(ordersItem);

        for (OrderDetail orderDetail : ordersItem) {
            List<LongLat> currOrderDestination = getOrderDestination(orderDetail);
            destinationList.add(new OrderDestination(orderDetail.orderNumber, currOrderDestination,
                    menuController.getDeliveryCost(orderDetail.items), orderDetail.deliverTo));
        }
        return destinationList;
    }

    private List<LongLat> getOrderDestination(OrderDetail order) {
        List<LongLat> orderDestination = new ArrayList<LongLat>();
        List<String> items = order.items;
        for (String item : items) {
            String location = menuController.getLocation(item);
            LongLat storeLongLat = longLatCatcher.getCenterLongLat(location);
            orderDestination.add(storeLongLat);
        }
        orderDestination = PathUtiles.removeSameLongLat(orderDestination);
        LongLat deliverTo = longLatCatcher.getCenterLongLat(order.deliverTo);
        orderDestination.add(deliverTo);
        return orderDestination;
    }

}
