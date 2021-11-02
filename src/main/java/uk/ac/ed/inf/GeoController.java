/**
 * Class for dealing with geoJson.
 *
 * @author Duan Wenxing
 */

package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.io.*;
import java.text.SimpleDateFormat;

import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.geojson.Feature;

public class GeoController {

    private String port;
    private String address;
    private FeatureCollection noFlyFeatureCollection;
    private FeatureCollection landmarkFeatureCollection;
    private List<List<LongLat>> noFlyFeatureCollectionLongLat = new ArrayList<List<LongLat>>();

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
            // System.out.println(point.longitude());
            landmarksLongLat.add(landmark);
        }
        return landmarksLongLat;
    }

    public List<List<LongLat>> getNoFlyLongLat() {
        return this.noFlyFeatureCollectionLongLat;
    }

    public void storeFlightPath(List<LongLat> paths, String dateString) {
        SimpleDateFormat dateDF = new SimpleDateFormat("yyyy-MM-dd");
        List<Point> points = new ArrayList<Point>();
        for (LongLat longlat : paths) {
            Point point = Point.fromLngLat(longlat.longitude, longlat.latitude);
            points.add(point);
        }
        LineString path = LineString.fromLngLats(points);
        Feature pathFeature = Feature.fromGeometry(path);
        FeatureCollection pathFeatureCollection = FeatureCollection.fromFeature(pathFeature);
        try {
            Date date = dateDF.parse(dateString);
            String year_ = String.format("%tY", date);
            String month_ = String.format("%tm", date);
            String date_ = String.format("%td", date);
            BufferedWriter bf = new BufferedWriter(new FileWriter("drone-" + date_ + "-" + month_ + "-" + year_));
            bf.write(pathFeatureCollection.toJson());
            bf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
