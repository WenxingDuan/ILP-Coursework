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

    class Item {
        String item;
        int pence;
    }

    class Manu {
        String name;
        String location;
        List<Item> menu = new ArrayList<Item>();
    }

    public Menus(String machine, String port) {
        this.machine = machine;
        this.port = port;
        this.address = "http://" + machine + ":" + port;
        this.client = new ClientIO(address);
        this.buildMenu();
        this.buildItem();
    }

    private void buildMenu() {
        String manuString;
        try {
            manuString = client.get("menus/menus.json");
            Type respondType = new TypeToken<List<Manu>>() {
            }.getType();
            this.menus = new Gson().fromJson(manuString, respondType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buildItem() {
        for (Manu theManu : this.menus) {
            this.items.addAll(theManu.menu);
        }
    }

    // public static void main(String[] args) {
    //     Menus a = new Menus("localhost", "80");
    //     a.buildMenu();
    //     a.buildItem();
    // }

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
