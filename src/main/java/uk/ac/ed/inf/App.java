/**
 * Enpty class
 *
 * @author Duan Wenxing
 */
package uk.ac.ed.inf;


public class App {
    public static void main(String[] args) {

        LongLat a = new LongLatCatcher("9898").getCenterLongLat("blocks.found.civic");
        System.out.println(a.latitude);
        System.out.println(a.longitude);
    }
}
