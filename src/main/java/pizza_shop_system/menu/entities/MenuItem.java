package pizza_shop_system.menu.entities;

import java.util.ArrayList;

public class MenuItem {
    private String itemID;
    private String category;
    private double price;
    private int quantity;
    private String name;
    private String description;
    private ArrayList<String> toppings;

    public MenuItem(String itemID, String category, double price, String name, String description) {
        this.itemID = itemID;
        this.category = category;
        this.price = price;
        this.name = name;
        this.description = description;
        this.toppings = new ArrayList<>();
    }

    public MenuItem(String itemID, String category, double price, int quantity, String name, String description, ArrayList<String> toppings) {
        this.itemID = itemID;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.name = name;
        this.description = description;
        this.toppings = (toppings != null) ? toppings : new ArrayList<>();
    }

    public String getItemID() { return itemID; }
    public String getCategory() { return category; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getDescription() { return description; }
    public ArrayList<String> getToppings() { return toppings; }

    public void setQuantity(int quantity) { this.quantity = quantity; }

    public void setToppings(ArrayList<String> toppings) {
        this.toppings = (toppings != null) ? toppings : new ArrayList<>();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        MenuItem other = (MenuItem) obj;

        return itemID != null && itemID.equals(other.itemID);
    }

    @Override
    public int hashCode() {
        return itemID != null ? itemID.hashCode() : 0;
    }

}
