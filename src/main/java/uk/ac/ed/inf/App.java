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

        PathBuilder p = new PathBuilder("9898", "1527");
        p.buildPath("2023-12-31");
    }

}
