package uk.ac.ed.inf;

import java.util.List;
import java.util.ArrayList;

public class PathBuilder {
    private final LongLat appletonTower = new LongLat(-3.186874, 55.944494);

    private ManuController manuController;
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
    }

    class OrderDestination {
        String orderNumber;
        List<LongLat> destinations;

        OrderDestination(String orderNumber, List<LongLat> destinations) {
            this.orderNumber = orderNumber;
            this.destinations = destinations;
        }

    }

    public PathBuilder(String webPort, String dbPort) {
        this.longLatCatcher = new LongLatCatcher(webPort);
        this.databaseController = new DatabaseController(dbPort);
        this.manuController = new ManuController(webPort);
        this.geoController = new GeoController(webPort);
        this.landmarks = this.geoController.getLandmarksLongLat();
        this.noFlyLongLat = this.geoController.getNoFlyLongLat();
    }

    public List<OrderDestination> generatePath(String date) {
        int battery = 1500;
        List<OrderDestination> path;

        List<OrderDetail> orders = databaseController.orderSearch(date);
        // System.out.println();
        // orders.get(0).printInformation();

        List<OrderDestination> destinationList = getDestination(orders);
        path = findDeliverPath(battery, destinationList);

        return path;
    }

    // ================================================================================================

    private List<OrderDestination> findDeliverPath(int battery, List<OrderDestination> destinationList) {
        List<OrderDestination> path = new ArrayList<OrderDestination>();
        // path.add(appletonTower);
        LongLat currentPosition = appletonTower;
        for (OrderDestination orderDestination : destinationList) {
            List<LongLat> tempPath = new ArrayList<LongLat>();

            for (LongLat nextDestination : orderDestination.destinations) {
                if (currentPosition.closeTo(nextDestination))
                    continue;
                tempPath.addAll(PathUtiles.organizeShortestPath(currentPosition, nextDestination, this.landmarks,
                        this.noFlyLongLat));
                currentPosition = nextDestination;
            }
            tempPath = removeSameLongLat(tempPath);
            int orderBatteryCost = PathUtiles.batteryCalculater(tempPath);
            int backBatteryCost = PathUtiles.batteryCalculater(currentPosition, this.appletonTower);

            if (battery - (orderBatteryCost + backBatteryCost) > 0) {
                battery = battery - orderBatteryCost;
                // tempPath.remove(0);
                path.add(new OrderDestination(orderDestination.orderNumber, tempPath));
            } else {
                OrderDestination lastOrderDestination = path.get(path.size() - 1);
                List<LongLat> lastDestinations = lastOrderDestination.destinations;
                currentPosition = lastDestinations.get(lastDestinations.size() - 1);
                tempPath = PathUtiles.organizeShortestPath(currentPosition, this.appletonTower, this.landmarks,
                        this.noFlyLongLat);
                // tempPath.remove(0);
                // System.out.println("==========================");
                path.add(new OrderDestination(orderDestination.orderNumber, tempPath));
                break;
            }
            OrderDestination lastOrderDestination = path.get(path.size() - 1);
            List<LongLat> lastDestinations = lastOrderDestination.destinations;
            currentPosition = lastDestinations.get(lastDestinations.size() - 1);

        }

        // OrderDestination lastOrderDestination = path.get(path.size() - 1);
        // List<LongLat> lastDestinations = lastOrderDestination.destinations;
        // currentPosition = lastDestinations.get(lastDestinations.size() - 1);

        // String orderNumber = path.get(path.size() - 1).orderNumber;
        String orderNumber = "00000000";
        if (!currentPosition.closeTo(this.appletonTower)) {
            List<LongLat> tempPath = PathUtiles.organizeShortestPath(currentPosition, this.appletonTower,
                    this.landmarks, this.noFlyLongLat);
            // tempPath.remove(0);
            path.add(new OrderDestination(orderNumber, tempPath));

        }

        return path;
    }

    private List<LongLat> removeSameLongLat(List<LongLat> longLatList) {
        for (int i = 0; i < longLatList.size() - 1; i++) {
            if (longLatList.get(i).closeTo(longLatList.get(i + 1)))
                longLatList.remove(i + 1);
        }
        return longLatList;
    }

    private Move movementCalculater(LongLat start, LongLat end) {
        int angle = PathUtiles.degreeTwoPoints(start, end);
        int step = 0;
        while (!start.closeTo(end)) {
            step++;
            start = start.nextPosition(angle);
        }
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
                if (manuController.getDeliveryCost(orders.get(i).items) > highestCost) {
                    highestOrderIndex = i;
                    highestCost = manuController.getDeliveryCost(orders.get(i).items);
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
        // orders.get(0).printInformation();
        // System.out.println("11111111111111111");
        List<OrderDestination> destinationList = new ArrayList<OrderDestination>();
        List<OrderDetail> ordersItem = sortByValue(orders);
        ordersItem = sortByValue(ordersItem);

        for (OrderDetail orderDetail : ordersItem) {
            List<LongLat> currOrderDestination = getOrderDestination(orderDetail);
            destinationList.add(new OrderDestination(orderDetail.orderNumber, currOrderDestination));
        }
        return destinationList;
    }

    private List<LongLat> getOrderDestination(OrderDetail order) {
        List<LongLat> orderDestination = new ArrayList<LongLat>();
        List<String> items = order.items;
        for (String item : items) {
            String location = manuController.getLocation(item);
            LongLat storeLongLat = longLatCatcher.getCenterLongLat(location);
            orderDestination.add(storeLongLat);
        }
        LongLat deliverTo = longLatCatcher.getCenterLongLat(order.deliverTo);
        orderDestination.add(deliverTo);
        return orderDestination;
    }

}
