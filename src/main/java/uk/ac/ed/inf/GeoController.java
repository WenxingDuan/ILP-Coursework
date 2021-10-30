package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.List;
import java.awt.geom.Line2D;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.geojson.Feature;

public class GeoController {

    String port;
    String address;
    Client client;
    FeatureCollection noFlyFeatureCollection;
    FeatureCollection landmarkFeatureCollection;
    List<List<LongLat>> noFlyFeatureCollectionLongLat = new ArrayList<List<LongLat>>();

    public GeoController(String port) {
        this.port = port;
        this.address = "http://localhost:" + port + "/buildings/";
        this.noFlyFeatureCollection = getFeatureCollection("no-fly-zones.geojson");
        this.landmarkFeatureCollection = getFeatureCollection("landmarks.geojson");
        buildNoFlyFeatureCollectionLongLat();
    }

    private FeatureCollection getFeatureCollection(String name) {
        String fcString = Client.get(address + name);
        return FeatureCollection.fromJson(fcString);
    }

    private void buildNoFlyFeatureCollectionLongLat() {
        List<Feature> features = this.noFlyFeatureCollection.features();
        for (Feature theFeature : features) {
            List<LongLat> zoneLongLatList = new ArrayList<LongLat>();
            Polygon zone = (Polygon) theFeature.geometry();
            List<Point> points = zone.coordinates().get(0);
            for (Point thePoint : points) {
                LongLat currPoint = new LongLat(thePoint.longitude(), thePoint.latitude());
                zoneLongLatList.add(currPoint);
            }
            this.noFlyFeatureCollectionLongLat.add(zoneLongLatList);
        }
    }

    public List<LongLat> getLandmarksLongLat() {
        List<LongLat> landmarksLongLat = new ArrayList<LongLat>();
        for (Feature theFeature : landmarkFeatureCollection.features()) {
            Point point = (Point) theFeature.geometry();
            LongLat landmark = new LongLat(point.longitude(), point.latitude());
            landmarksLongLat.add(landmark);
        }
        return landmarksLongLat;

    }

    public boolean canFly(LongLat start, LongLat end) {
        for (List<LongLat> currZonePoints : this.noFlyFeatureCollectionLongLat) {
            for (int i = 0; i < currZonePoints.size() - 1; i++) {
                LongLat currLinePoint1 = currZonePoints.get(i);
                LongLat currLinePoint2 = currZonePoints.get(i + 1);
                if (Line2D.linesIntersect(start.longitude, start.latitude, end.longitude, end.latitude,
                        currLinePoint1.longitude, currLinePoint1.latitude, currLinePoint2.longitude,
                        currLinePoint2.latitude)) {
                    return false;
                }
            }
        }
        return true;
    }

}
