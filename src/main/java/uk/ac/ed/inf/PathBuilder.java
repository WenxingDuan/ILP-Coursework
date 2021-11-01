package uk.ac.ed.inf;

import java.util.List;
import java.util.ArrayList;

public class PathBuilder {
    private final LongLat appletonTower = new LongLat(-3.186874, 55.944494);

    private ManuController manuController;
    private LongLatCatcher longLatCatcher;
    private DatabaseController databaseController;
    private GeoController geoController;
    private List<LongLat> landmarks;
    private List<List<LongLat>> noFlyLongLat;

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

    public PathBuilder(String webPort, String dbPort) {
        this.longLatCatcher = new LongLatCatcher(webPort);
        this.databaseController = new DatabaseController(dbPort);
        this.manuController = new ManuController(webPort);
        this.geoController = new GeoController(webPort);
        this.landmarks = this.geoController.getLandmarksLongLat();
        this.noFlyLongLat = this.geoController.getNoFlyLongLat();
    }

    public List<LongLat> generatePath(String date) {
        int battery = 1500;
        List<LongLat> path;

        List<OrderDetail> orders = databaseController.orderSearch(date);
        // System.out.println();
        // orders.get(0).printInformation();

        List<List<LongLat>> destinationList = getDestination(orders);
        path = findDeliverPath(battery, destinationList);

        return path;
    }

    // ================================================================================================

    private List<LongLat> findDeliverPath(int battery, List<List<LongLat>> destinationList) {
        List<LongLat> path = new ArrayList<LongLat>();
        path.add(appletonTower);
        LongLat currentPosition = appletonTower;
        for (List<LongLat> orderDestination : destinationList) {
            currentPosition = path.get(path.size() - 1);
            List<LongLat> tempPath = new ArrayList<LongLat>();

            for (LongLat nextDestination : orderDestination) {
                tempPath.addAll(chooseShortestLandmark(currentPosition, nextDestination));
                currentPosition = nextDestination;
            }
            int orderBatteryCost = batteryCalculater(tempPath);
            int backBatteryCost = batteryCalculater(currentPosition, this.appletonTower);

            if (battery - (orderBatteryCost + backBatteryCost) > 0) {
                battery = battery - orderBatteryCost;
                path.addAll(tempPath);
            } else {
                path.addAll(chooseShortestLandmark(currentPosition, this.appletonTower));
                break;
            }
        }
        if (!path.get(path.size() - 1).closeTo(this.appletonTower))
            path.addAll(chooseShortestLandmark(currentPosition, this.appletonTower));

        return path;
    }

    private List<LongLat> chooseShortestLandmark(LongLat start, LongLat end) {
        List<List<LongLat>> multiplePaths = new ArrayList<List<LongLat>>();
        List<LongLat> directPath = PathOrganizer.organizePath(start, end, 10, this.noFlyLongLat);
        if (directPath != null)
            multiplePaths.add(directPath);
        for (LongLat landmark : this.landmarks) {
            List<LongLat> currPath = PathOrganizer.organizePathThoughLandmark(start, landmark, end, this.noFlyLongLat);
            if (currPath != null)
                multiplePaths.add(currPath);
        }
        return chooseShortestPath(multiplePaths);
    }

    private List<LongLat> chooseShortestPath(List<List<LongLat>> pathOptions) {
        int shortestOrderIndex = 0;
        int shortestCost = batteryCalculater(pathOptions.get(0));
        for (int i = 0; i < pathOptions.size(); i++) {
            if (batteryCalculater(pathOptions.get(i)) < shortestCost) {
                shortestOrderIndex = i;
                shortestCost = batteryCalculater(pathOptions.get(i));
            }
        }
        return pathOptions.get(shortestOrderIndex);
    }

    private int batteryCalculater(List<LongLat> path) {
        int cost = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            cost = cost + batteryCalculater(path.get(i), path.get(i + 1));
        }
        return cost;
    }

    private int batteryCalculater(LongLat start, LongLat end) {
        int cost = (int) Math.floor(start.distanceTo(end) / 0.00015);
        return cost;

    }

    private Move movementCalculater(LongLat start, LongLat end) {
        int angle = PathOrganizer.degreeTwoPoints(start, end);
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
        System.out.println(orders.isEmpty());

        while (true) {
            System.out.println(orders.size());
            orders.get(0).printInformation();

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

    private List<List<LongLat>> getDestination(List<OrderDetail> orders) {
        // orders.get(0).printInformation();
        // System.out.println("11111111111111111");
        List<List<LongLat>> destinationList = new ArrayList<List<LongLat>>();
        List<OrderDetail> ordersItem = sortByValue(orders);
        ordersItem = sortByValue(ordersItem);
        for (OrderDetail orderDetail : ordersItem) {
            List<LongLat> currOrderDestination = getOrderDestination(orderDetail);
            destinationList.add(currOrderDestination);
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
