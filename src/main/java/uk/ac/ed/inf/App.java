/**
 * Enpty class
 *
 * @author Duan Wenxing
 */
package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.List;

public class App {

    public static void main(String[] args) {

        LongLat a = new LongLat(-3.1913, 55.9456);
        LongLat b = new LongLat(-3.1878, 55.9435);
        // LongLat c = new LongLat(-3.187263607978821, 55.943543468726304);
        // LongLat d = new LongLat(-3.188443779945373, 55.943525443970906);
        // LongLat x = new LongLat(-3.191594, 55.943658);
        // LongLat y = new LongLat(-3.186199, 55.945734);
        // LongLat z = new LongLat(-3.191603422164917, 55.94575744571186);

        // LongLat e = new LongLat(-3.191187311208956, 55.94339756321238);
        // LongLat f = new LongLat(-3.188502788543701, 55.94387091698859);

        // LongLatCatcher("9898").getCenterLongLat("blocks.found.civic");

        // System.out.println(Line2D.linesIntersect(0, 0, 1, 1, 0, 1, 1, 0));
        // GeoController g = new GeoController("9898");
        // // List<List<LongLat>> no = g.getNoFlyLongLat();
        // List<List<LongLat>> no = new ArrayList<List<LongLat>>();

        // List<LongLat> paths = PathUtiles.advanceOrganizePath(a, b, 40, 60, no);

        // for (LongLat point : paths) {
        // System.out.print("[" + point.longitude + "," + point.latitude + "],");
        // }
        // System.out.println();
        // System.out.println(PathUtiles.degreeTwoPoints(paths.get(1), paths.get(2)));
        // System.out.println(PathOrganizer.distanceCalculator(paths));
        // System.out.println(g.getLandmarksLongLat().get(1).longitude);
        // System.out.println(App.stepCalculater(e, f));
        // System.out.println(Math.floor(e.distanceTo(f)/0.00015));

        // System.out.println(e.longitude);
        // System.out.println(e.latitude);

        PathBuilder p = new PathBuilder("9898", "1527");
        List<PathBuilder.OrderDestination> paths = p.generatePath("2022-09-15");
        for (PathBuilder.OrderDestination point : paths) {
            // System.out.println(point.orderNumber);
            for (LongLat thepoint : point.destinations)
                System.out.println("[" + thepoint.longitude + "," + thepoint.latitude + "],");
        }
        // p.chooseShortestLandmark(a, b);

    }

}
