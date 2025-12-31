package pizza_shop_system.order.services;

import org.json.JSONArray;
import org.json.JSONObject;
import pizza_shop_system.account.services.AccountService;
import pizza_shop_system.gui.order.CartController;
import pizza_shop_system.utils.DateUtil;
import pizza_shop_system.utils.JSONUtil;

import java.io.FileWriter;
import java.io.IOException;

public class OrderService {
    private final JSONUtil jsonUtil = new JSONUtil();
    private final DateUtil dateUtil = new DateUtil();
    private static CartController cartController;
    private final AccountService accountService = new AccountService();
    private final String ORDERS_FILE_PATH = "data_files/Orders.json";

    public JSONObject loadOrders() throws IOException {
        return jsonUtil.loadJSONObject(ORDERS_FILE_PATH);
    }

    // Load the customizations json (Used to calculate a price for each order item)
    public JSONObject loadCustomizations() throws IOException {
        return jsonUtil.loadJSONObject("data_files/MenuItemCustomizations.json");
    }

    // Gets the current order object and if it does not exist creates one
    public JSONObject getCurrentOrder(JSONObject ordersData) throws IOException {
        JSONArray orders = ordersData.getJSONArray("orders");
        int currentOrderId = ordersData.getInt("nextOrderId"); // Our current order id is the next available id since nextOrderId is not incremented until order is finalized

        // If a current order object is found then return it
        for (int i = 0; i < orders.length(); i++) {
            JSONObject order = orders.getJSONObject(i);
            if (order.getInt("orderId") == currentOrderId) {
                return order;
            }
        }

        // No current order object exist so one is created and saved
        JSONObject newOrder = new JSONObject();
        newOrder.put("orderId", currentOrderId); // Must have orderId
        newOrder.put("orderItems", new JSONArray()); // Must have JSONArray to insert order items
        orders.put(newOrder);
        return newOrder;
    }

    public JSONArray getOrdersByAccount(int accountId) throws IOException {
        JSONObject ordersData = loadOrders();
        JSONArray orders = ordersData.getJSONArray("orders");
        JSONArray ordersByAccount = new JSONArray();

        for (int i = 0; i < orders.length(); i++) {
            JSONObject order = orders.getJSONObject(i);
            int userAccountId = order.optInt("accountId");
            if (userAccountId != 0 && userAccountId == accountId) {
                ordersByAccount.put(order);
            }
        }

        return ordersByAccount;
    }

    // Save changes to the orders file
    public void saveOrders(JSONObject orders) throws IOException {
        try (FileWriter file = new FileWriter(ORDERS_FILE_PATH)) {
            file.write(orders.toString(4));
        }

        // Order item has been updated. Update the display of the cart
        cartController.displayCurrentOrder(orders);
    }

    // Set the current cart controller
    public void setCartController(CartController cartController) {
        this.cartController = cartController;
    }

    // Add a OrderItem JSONObject to the current order
    public int addOrderItem(JSONObject orderItem) throws IOException {
        JSONObject ordersData = loadOrders();
        JSONObject currentOrder = getCurrentOrder(ordersData);
        JSONArray orderItems = currentOrder.getJSONArray("orderItems");
        JSONObject customizations = loadCustomizations();

        int orderItemId = currentOrder.optInt("nextOrderItemId");
        if (orderItemId == 0) {
            orderItemId = 1; // if no order items exist yet then first will have id of 1
        }

        orderItem.put("orderItemId", orderItemId);

        // Increment nextOrderItemId
        int nextOrderItemId = orderItemId + 1;
        currentOrder.put("nextOrderItemId", nextOrderItemId);

        orderItems.put(orderItem); // Add passed orderItem into the orderItems array of the current order

        JSONArray orders = loadOrders().getJSONArray("orders");
        orders.put(currentOrder); // Put current order into orders

        updateOrderItemDetails(orderItem);
        updateOrderDetails(currentOrder);
        saveOrders(ordersData);

        //System.out.println("Order item added successfully: ");
        return orderItemId;
    }

    // Finds the selected orderItemId in the current order and removes it
    public void removeOrderItem(int orderItemId) throws IOException {
        JSONObject ordersData = loadOrders();
        JSONObject currentOrder = getCurrentOrder(ordersData);
        JSONArray orderItems = currentOrder.getJSONArray("orderItems");
        for (int i = 0; i < orderItems.length(); i++) {
            JSONObject orderItem = orderItems.getJSONObject(i);

            if (orderItem.getInt("orderItemId") == orderItemId)  {
                orderItems.remove(i); // Remove the order by its index
                //System.out.println("Order item removed successfully: ");

                updateOrderItemDetails(orderItem);
                updateOrderDetails(currentOrder);
                saveOrders(ordersData);
                return;
            }
        }
        //System.out.println("Failed to delete order item");
    }

    // Updates an orderItem by overwriting its old contents with new contents
    public void updateOrderItem(int orderItemId, JSONObject newOrderItem) throws IOException {
        JSONObject ordersData = loadOrders();
        JSONObject currentOrder = getCurrentOrder(ordersData);
        JSONArray orderItems = currentOrder.getJSONArray("orderItems");
        for (int i = 0; i < orderItems.length(); i++) {
            JSONObject orderItem = orderItems.getJSONObject(i);
            if (orderItem.getInt("orderItemId") == orderItemId) {
                orderItems.remove(i); // Remove old order item object
                newOrderItem.put("orderItemId", orderItemId); // Add old order items id to new order item object
                orderItems.put(newOrderItem);

                updateOrderItemDetails(newOrderItem);
                updateOrderDetails(currentOrder);
                saveOrders(ordersData);
                //.println("Order item updated successfully: ");
                return;
            }
        }
        //System.out.println("Order item with ID: " + orderItemId + " not found");
    }

    // Checkout will send the users selected orderType and details here to store in the current order
    public void addOrderInformation(JSONObject orderInfo) throws IOException {
        //System.out.println("Adding order information " + paymentInfo.toString());
        JSONObject ordersData = loadOrders();
        JSONObject currentOrder = getCurrentOrder(ordersData);
        currentOrder.put("orderInformation", orderInfo);
        saveOrders(ordersData);
    }

    // Checkout will send the users selected paymentMethod and details here to store in the current order
    public void addPaymentInformation(JSONObject paymentInfo) throws IOException {
        //System.out.println("Adding payment information " + paymentInfo.toString());
        JSONObject ordersData = loadOrders();
        JSONObject currentOrder = getCurrentOrder(ordersData);
        currentOrder.put("paymentInformation", paymentInfo);
        saveOrders(ordersData);
    }

    // Finalizes the current order. Applies date and time stamp, applies accountId, and increments to the nextOrderId (essentially starting a new order)
    public void finalizeOrder() throws IOException {

        // If no user is logged in then order will not be finalized
        int accountId = accountService.getActiveUserId(); // This should be fetched from AccountService to get the active user
        if (accountId == 0) {
            System.out.println("NO USER IS LOGGED IN. ORDER COULD NOT BE FINALIZED");
            return;
        }

        JSONObject orderData = loadOrders();
        JSONObject currentOrder = getCurrentOrder(orderData);

        currentOrder.put("date", dateUtil.getCurrentDateTime());
        currentOrder.put("accountId", accountId);

        // Add payment details here

        int orderId = orderData.getInt("nextOrderId");
        int nextOrderId = orderId + 1;
        orderData.put("nextOrderId",nextOrderId);

        updateOrderDetails(currentOrder);
        saveOrders(orderData);

        //System.out.println("Finalized order number: " + orderId + " Now on order number: " + nextOrderId);
    }

    // When an order item is added or updated its price information needs to be recalculated
    private void updateOrderItemDetails(JSONObject orderItem) throws IOException {
        JSONObject customizationData = loadCustomizations();
        double totalPrice = 0;

        // Check for toppings
        if (orderItem.has("toppings")) {
            JSONObject toppingsData = customizationData.getJSONObject("toppings"); // toppings from the customizations file
            JSONArray toppings = orderItem.getJSONArray("toppings"); // toppings in the order item

            for (int i = 0; i < toppings.length(); i++) {
                String topping = toppings.getString(i);
                if (toppingsData.has(topping)) {
                    totalPrice += toppingsData.getDouble(topping); // get price of topping and add it to total cost of menu item
                } else {
                    //System.out.println("WARNING: topping " + topping + " not found in customizations");
                }
            }
        }

        // Check for various customizations option variables so the price of the menu item can be calculated (this allows for very dynamic pricing)

        // Check for pizza sizes
        if (orderItem.has("pizzaSize")) {
            JSONObject pizzaSizeData = customizationData.getJSONObject("pizzaSizes");
            String pizzaSize = orderItem.getString("pizzaSize");
            if (pizzaSizeData.has(pizzaSize)) {
                totalPrice += pizzaSizeData.getDouble(pizzaSize);
            } else {
                //System.out.println("Pizza size " + pizzaSize + " not found in customizations");
            }
        }

        // Check for crust
        if (orderItem.has("crust")) {
            JSONObject crustData = customizationData.getJSONObject("crusts");
            String crust = orderItem.getString("crust");
            if (crustData.has(crust)) {
                totalPrice += crustData.getDouble(crust);
            } else {
                //System.out.println("Crust " + crust + " not found in customizations");
            }
        }

        // Check for beverage size
        if (orderItem.has("beverageSize")) {
            JSONObject beverageSizeData = customizationData.getJSONObject("beverageSizes");
            String beverageSizes = orderItem.getString("beverageSize");
            if (beverageSizeData.has(beverageSizes)) {
                totalPrice += beverageSizeData.getDouble(beverageSizes);
            } else {
                //System.out.println("Beverage size " + beverageSizes + " not found in customizations");
            }
        }

        // Check for ice options (Included but not functional because ice is free lol)
        if (orderItem.has("ice")) {
            // System.out.println("Ice option exist");
        }

        orderItem.put("price", floorCurrency(totalPrice)); // Set the price of the order item with the calculated total price
    }

    public double getCurrentOrderTotal() throws IOException {
        JSONObject ordersData = loadOrders();
        JSONObject currentOrder = getCurrentOrder(ordersData);
        updateOrderDetails(currentOrder); // Ensure it's up-to-date
        return currentOrder.optDouble("total", 0.00); //If no total present returns 0.00
    }

    private JSONObject getOrderById(int orderId) throws IOException {
        JSONObject ordersData = loadOrders();
        JSONArray orders = ordersData.getJSONArray("orders");
        for (int i = 0; i < orders.length(); i++) {
            JSONObject order = orders.getJSONObject(i);
            if (order.getInt("orderId") == orderId) {
                return order;
            }
        }
        return null;
    }

    public JSONArray getOrders() throws IOException {
        JSONObject ordersData = loadOrders();
        return ordersData.getJSONArray("orders");
    }

    public JSONObject getPreviousOrder() throws IOException {
        JSONObject ordersData = loadOrders();
        int previousOrderId = ordersData.getInt("nextOrderId") - 1;
        return getOrderById(previousOrderId);
    }

    // Cuts off to 2 decimal points and does not round
    private double floorCurrency(double currencyAmount) {
        return  Math.floor(currencyAmount * 100) / 100;
    }
    // When order items are added, removed, or updated. Orders details like price information needs to be recalculated
    private void updateOrderDetails(JSONObject currentOrder) throws IOException {
        JSONArray orderItems = currentOrder.getJSONArray("orderItems");

        double subTotal = 0;

        for (int i = 0; i < orderItems.length(); i++) {
            JSONObject orderItem = orderItems.getJSONObject(i);
            double price = orderItem.optDouble("price");
            subTotal += price;
        }

        double tax = subTotal * .07; // Apply 7% tax
        double total = subTotal + tax;

        // floor into proper currency format (0.00)
        subTotal = floorCurrency(subTotal);
        tax = floorCurrency(tax);
        total = floorCurrency(total);

        currentOrder.put("subtotal", subTotal);
        currentOrder.put("tax", tax);
        currentOrder.put("total", total);
    }


    // for testing
    public static void main(String[] args) throws IOException {
        OrderService orderService = new OrderService();

        JSONObject orderItem = new JSONObject();
        orderItem.put("name", "ArbitraryMenuItem");
        JSONArray toppings = new JSONArray();
        toppings.put("pepperoni");
        toppings.put("chicken");
        toppings.put("sausage");
        orderItem.put("toppings", toppings);

        JSONObject orderItem2 = new JSONObject();
        orderItem.put("name", "ArbitraryMenuItem2");
        JSONArray toppings2 = new JSONArray();
        toppings2.put("pepperoni");
        orderItem2.put("toppings", toppings2);

        // orderService.addOrderItem(orderItem);
        // orderService.addOrderItem(orderItem2);
        // orderService.updateOrderItem(1, orderItem2);
        // orderService.deleteOrderItem(2);
        // orderService.finalizeOrder();
    }
}
