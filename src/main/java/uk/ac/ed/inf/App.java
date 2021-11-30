/**
 * Main class
 *
 * @author Duan Wenxing
 */
package uk.ac.ed.inf;

public class App {
    /**
     * Main
     * 
     * @param args
     *             <ul>
     *             <li>[0] date</li>
     *             <li>[1] month</li>
     *             <li>[2] year</li>
     *             <li>[3] web server port</li>
     *             <li>[4] database server port</li>
     *             </ul>
     */
    public static void main(String[] args) {

        String date = args[0];
        String month = args[1];
        String year = args[2];
        String webPort = args[3];
        String dbPort = args[4];

        PathBuilder p = new PathBuilder(webPort, dbPort);
        p.buildPath(year + "-" + month + "-" + date);

        // ================================================================
        // generate required dates
        // ================================================================
        // PathBuilder p = new PathBuilder("9898", "1527");
        // for (int i = 1; i < 13; i++){
        // p.buildPath("2022" + "-" + i + "-" + i);
        // }
    }

}
