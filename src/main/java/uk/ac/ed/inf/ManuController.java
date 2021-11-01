/**
 * Class used to get the location for each item
 *
 * @author Duan Wenxing
 */

package uk.ac.ed.inf;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ManuController {
    String port;
    String address;
    ArrayList<Manu> menus;
    ArrayList<Item> items = new ArrayList<Item>();

    /**
     * The class stores the name and price of a certain food item
     */
    class Item {
        String item;
        int pence;
    }

    /**
     * The class stores the name, location and manu of store
     */
    class Manu {
        String name;
        String location;
        List<Item> menu = new ArrayList<Item>();
    }

    /**
     * Constructer of Menus class. In the constructer method, function get all the
     * manus and stored in the local.
     *
     * @param port the communication port to the server
     */
    public ManuController(String port) {

        this.port = port;
        this.address = "http://localhost:" + port;
        this.buildMenu();
    }

    /**
     * Method to get the manu JSON and construct by GSON into an arraylist of the
     * type of {@link Manu}
     * 
     */
    private void buildMenu() {
        String manuString = Client.get(address + "/menus/menus.json");
        Type respondType = new TypeToken<List<Manu>>() {
        }.getType();
        this.menus = new Gson().fromJson(manuString, respondType);
    }

    /**
     * Method to calculate the totla cost by go over the item list
     * 
     * @param itemList the names of the items in the order
     * @return the totla cost of the price include standard delivery charge
     */
    public int getDeliveryCost(List<String> itemList) {
        int price = 50;
        for (String itemName : itemList) {
            for (Item currItem : items) {
                if (itemName.equals(currItem.item)) {
                    price = price + currItem.pence;
                    break;
                }
            }
        }
        return price;
    }

    public String getLocation(String itemName) {
        for (Manu theMenu : this.menus) {
            for (Item currItem : theMenu.menu) {
                if (itemName.equals(currItem.item)) {
                    return theMenu.location;
                }
            }
        }
        return null;
    }

}