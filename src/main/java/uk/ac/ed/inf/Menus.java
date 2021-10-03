/**
 * Class used to communicate with the server
 *
 * @author Duan Wenxing
 */

package uk.ac.ed.inf;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Menus {
    String machine;
    String port;
    String address;
    ClientIO client;
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
     * @param machine the communication endpoints to the server
     * @param port    the communication port to the server
     */
    public Menus(String machine, String port) {
        this.machine = machine;
        this.port = port;
        this.address = "http://" + machine + ":" + port;
        this.buildMenu();
        this.buildItem();
    }

    /**
     * Method to get the manu JSON and construct by GSON into an arraylist of the
     * type of {@link Manu}
     * 
     */
    private void buildMenu() {
        String manuString = ClientIO.get(address + "/menus/menus.json");
        Type respondType = new TypeToken<List<Manu>>() {
        }.getType();
        this.menus = new Gson().fromJson(manuString, respondType);
    }

    /**
     * Method to extract items information from the manu list and construct an
     * arraylist of the type of {@link Item}
     */
    private void buildItem() {
        for (Manu theManu : this.menus) {
            this.items.addAll(theManu.menu);
        }
    }

    /**
     * Method to calculate the totla cost by go over the item list
     * 
     * @param itemArray the names of the items in the order
     * @return the totla cost of the price include standard delivery charge
     */
    public int getDeliveryCost(String... itemArray) {
        int price = 50;
        for (String itemName : itemArray) {
            for (Item currItem : items) {
                if (itemName.equals(currItem.item)) {
                    price = price + currItem.pence;
                    break;
                }
            }
        }
        return price;
    }
}
