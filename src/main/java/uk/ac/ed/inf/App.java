/**
 * Main class
 *
 * @author Duan Wenxing
 */
package uk.ac.ed.inf;

public class App {

    public static void main(String[] args) {

        // String date = args[0];
        // String month = args[1];
        // String year = args[2];
        // String webPort = args[3];
        // String dbPort = args[4];

        // PathBuilder p = new PathBuilder(webPort, dbPort);
        // p.buildPath(year + "-" + month + "-" + date);

        // PathBuilder p = new PathBuilder("9898", "1527");
        // p.buildPath("2022-1-11");

        LongLat a = new LongLat(-3.18848468363285, 55.94529070363015);
        LongLat b = new LongLat(-3.1884, 55.9454);
        System.out.println((a.distanceTo(b)));
    }

}
