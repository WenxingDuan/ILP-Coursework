/**
 * Enpty class
 *
 * @author Duan Wenxing
 */
package uk.ac.ed.inf;

public class App {

    public static void main(String[] args) {

        // LongLat a = new
        // LongLatCatcher("9898").getCenterLongLat("blocks.found.civic");

        // System.out.println(Line2D.linesIntersect(0, 0, 1, 1, 0, 1, 1, 0));
        GeoController g = new GeoController("9898");

        System.out.println(g.canFly(new LongLat(-3.1875211000442505, 55.94454833556847),
                new LongLat(-3.1880843639373775, 55.94491182358009)));

        // System.out.println(g.getLandmarksLongLat().get(1).longitude);

    }
}
