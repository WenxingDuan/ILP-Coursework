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

public class MenuUtils {
    String port;
    String address;
    ArrayList<Menu> menus;
    ArrayList<Item> items = new ArrayList<Item>();

    /**
     * The class stores the name and price of a certain food item.
     */
    class Item {
        String item;
        int pence;
    }

    /**
     * The class stores the name, location and menu of store.
     */
    class Menu {
        String name;
        String location;
        List<Item> menu = new ArrayList<Item>();
    }

    /**
     * Constructor of MenuUtils class. In the constructor method, function get all
     * the man's and stored in the local.
     *
     * @param port the communication web port to the server
     */
    public MenuUtils(String port) {

        this.port = port;
        this.address = "http://localhost:" + port;
        buildMenu();
        buildItem();
    }

    /**
     * Method to get the menu JSON and construct by GSON into an arraylist of the
     * type of {@link Menu}.
     * 
     */
    private void buildMenu() {
        String menuString = ServerUtils.get(address + "/menus/menus.json");
        Type respondType = new TypeToken<List<Menu>>() {
        }.getType();
        this.menus = new Gson().fromJson(menuString, respondType);
    }

    /**
     * Method to extract items information from the menu list and construct an
     * arraylist of the type of {@link Item}.
     */
    private void buildItem() {
        for (Menu theMenu : this.menus) {
            this.items.addAll(theMenu.menu);
        }
    }

    /**
     * Method to calculate the total cost by go over the item list.
     * 
     * @param itemList the names of the items in the order
     * @return the total cost of the price include standard delivery charge
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

    /**
     * Method to search the location of the store provide the input item.
     * 
     * @param itemName the name of the items to search
     * @return the WhatThreeWords location of the store provide the item
     */
    public String getLocation(String itemName) {
        for (Menu theMenu : this.menus) {
            for (Item currItem : theMenu.menu) {
                if (itemName.equals(currItem.item)) {
                    return theMenu.location;
                }
            }
        }
        return null;
    }

}
