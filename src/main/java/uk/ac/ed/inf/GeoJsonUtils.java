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

public class GeoJsonUtils {

    private String port;
    private String address;
    private FeatureCollection noFlyFeatureCollection;
    private FeatureCollection landmarkFeatureCollection;
    private List<List<LongLat>> noFlyFeatureCollectionLongLat = new ArrayList<List<LongLat>>();

    /**
     * Constructor of GeoJsonUtils class. In the constructor method, function get
     * the landmark and no-fly zone location in type of {@link FeatureCollection},
     * then transform no-fly zone location into list of the list of {@link LongLat}.
     *
     * @param port the communication web port to the server
     */
    public GeoJsonUtils(String port) {
        this.port = port;
        this.address = "http://localhost:" + port + "/buildings/";
        this.noFlyFeatureCollection = getFeatureCollection("no-fly-zones.geojson");
        this.landmarkFeatureCollection = getFeatureCollection("landmarks.geojson");
        buildNoFlyFeatureCollectionLongLat();
    }

    /**
     * Method to get all landmarks in type of a list of {@link LongLat}.
     * 
     * @return all landmarks in type of the list of {@link LongLat}
     */

    public List<LongLat> getLandmarksLongLat() {
        List<LongLat> landmarksLongLat = new ArrayList<LongLat>();
        for (Feature theFeature : landmarkFeatureCollection.features()) {
            Point point = (Point) theFeature.geometry();
            LongLat landmark = new LongLat(point.longitude(), point.latitude());
            landmarksLongLat.add(landmark);
        }
        return landmarksLongLat;
    }

    /**
     * Method to get the private no-fly zones edgepoint list.
     * 
     * @return no-fly zones edgepoint in type of list of the list of
     *         {@link LongLat}.
     */
    public List<List<LongLat>> getNoFlyLongLat() {
        return this.noFlyFeatureCollectionLongLat;
    }

    /**
     * Method to create a geojson file in the current dictionary with the input path
     * and date.
     * 
     * @param paths      list of key points of the path to be written
     * @param dateString date of the input path in form of yyyy-MM-dd
     */
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
            BufferedWriter bf = new BufferedWriter(
                    new FileWriter("drone-" + date_ + "-" + month_ + "-" + year_ + ".geojson"));
            bf.write(pathFeatureCollection.toJson());
            bf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to get the geojson file information of the input name and return in
     * type of {@link FeatureCollection}.
     * 
     * @param name the name of the geojson file
     * @return the information of the geojson file in type of
     *         {@link FeatureCollection}.
     * 
     */
    private FeatureCollection getFeatureCollection(String name) {
        String fcString = ServerUtils.get(address + name);
        return FeatureCollection.fromJson(fcString);
    }

    /**
     * Method to build the private no-fly zone edgepoint list .
     * 
     */
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

}
