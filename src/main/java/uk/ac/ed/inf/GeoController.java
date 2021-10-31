/**
 * Class for dealing with geoJson.
 *
 * @author Duan Wenxing
 */

package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.List;
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
            System.out.println(point.longitude());
            landmarksLongLat.add(landmark);
        }
        return landmarksLongLat;
    }

    public List<List<LongLat>> getNoFlyLongLat() {
        return this.noFlyFeatureCollectionLongLat;
    }



}
