package pizza_shop_system.gui.order;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.json.JSONArray;
import org.json.JSONObject;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import pizza_shop_system.gui.base.BaseController;
import pizza_shop_system.gui.utils.StyleUtil;
import pizza_shop_system.order.services.OrderService;
import pizza_shop_system.utils.StringUtil;

import java.io.IOException;

public class CartController extends BaseController {
    private final OrderService orderService = new OrderService();
    private static CustomizePizzaController customizePizzaController;
    private static CustomizeBeverageController customizeBeverageController;
    private final StyleUtil styleUtil = new StyleUtil();

    @FXML private ScrollPane cartItemsScrollPane;
    @FXML private Button buttonCheckout;
    @FXML private VBox cartItemsVBox;
    @FXML private Label subTotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;

    @FXML
    public void initialize() throws IOException {
        orderService.setCartController(this); // Set the cart controller in OrderService
        displayCurrentOrder(orderService.loadOrders()); // Load the current order

        buttonCheckout.setOnAction(_ -> sceneController.switchScene("Checkout"));
        styleUtil.fadeButtonOnHover(buttonCheckout);
    }

    public void displayCurrentOrder(JSONObject orderData) throws IOException {
        cartItemsVBox.getChildren().clear(); // Clear cart items

        JSONObject currentOrder = orderService.getCurrentOrder(orderData);
        JSONArray orderItems = currentOrder.getJSONArray("orderItems");

        for (int i = 0; i < orderItems.length(); i++) {
            JSONObject orderItem = orderItems.getJSONObject(i);
            HBox row = createItemRow(orderItem);
            cartItemsVBox.getChildren().add(row);
        }

        double tax = currentOrder.optDouble("tax");
        double total = currentOrder.optDouble("total");
        double subtotal = currentOrder.optDouble("subtotal");

        subTotalLabel.setText(String.format("$%.2f", subtotal));
        taxLabel.setText(String.format("$%.2f", tax));
        totalLabel.setText(String.format("$%.2f", total));

        buttonCheckout.setDisable(orderItems.isEmpty()); // Disable the checkout button if there are no order items
    }

    private final StringUtil stringUtil = new StringUtil();

    public String generatePizzaName(JSONObject orderItem) {
        StringBuilder name = new StringBuilder();
        String size = stringUtil.captilizeWord(orderItem.optString("pizzaSize"));
        String crust = stringUtil.captilizeWord(orderItem.optString("crust"));
        JSONArray toppings = orderItem.getJSONArray("toppings");

        name.append(size).append(" ").append(crust).append(" Crust");

        for (int i = 0; i < toppings.length(); i++) {
            String topping = toppings.getString(i);
            name.append(" ").append(stringUtil.captilizeWord(topping));
        }

        name.append(" Pizza");
        return name.toString();
    }

    public String generateBeverageName(JSONObject orderItem) {
        StringBuilder name = new StringBuilder();
        String beverage = stringUtil.captilizeWord(orderItem.optString("name"));
        String size = stringUtil.captilizeWord(orderItem.optString("beverageSize"));
        String ice = stringUtil.captilizeWord(orderItem.optString("ice"));
        name.append(size).append(" ").append(beverage).append(" With ").append(ice).append(" Ice");
        return name.toString();
    }

    private HBox createItemRow(JSONObject orderItem) {
        try {

            // Try to get a name first because beverages use name element to determine price
            String name = orderItem.optString("name");
            String displayName = null;

            // Pizza needs to have its name dynamically created
            if (orderItem.has("pizzaSize")) {
                name = generatePizzaName(orderItem);
                displayName = name;
            } else if (orderItem.has("beverageSize")) {
                displayName = generateBeverageName(orderItem);
            }

            orderItem.put("name", name);

            // remove button
            Button removeButton = new Button("Remove");
            removeButton.getStyleClass().add("cart-remove-button");
            styleUtil.fadeButtonOnHover(removeButton);
            removeButton.setOnAction(_ -> {
                try {
                    removeItemFromCart(orderItem);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });

            // edit button
            Button editButton = new Button("Edit");
            editButton.getStyleClass().add("cart-edit-button");
            styleUtil.fadeButtonOnHover(editButton);
            editButton.setOnAction(_ -> {
                try {
                    editItemInCart(orderItem);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });

            // Setup cart item vbox
            cartItemsVBox.setFillWidth(true);

            // Setup scroll pane
            cartItemsScrollPane.setFitToWidth(true);

            // Setup item row
            HBox itemRow = new HBox();
            itemRow.setSpacing(10);
            itemRow.setMaxWidth(Double.MAX_VALUE);
            itemRow.getStyleClass().add("cart-item-row");

            // Setup name label
            Label nameLabel = new Label(displayName);
            nameLabel.getStyleClass().add("cart-item-name");
            nameLabel.setPrefHeight(Region.USE_COMPUTED_SIZE);
            nameLabel.setMaxHeight(Double.MAX_VALUE);

            // Setup region
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.SOMETIMES);
            spacer.setPrefWidth(Region.USE_COMPUTED_SIZE);

            // Setup price label
            double orderItemPrice = orderItem.optDouble("price");
            String price = "$" + Double.toString(orderItemPrice);
            Label priceLabel = new Label(price);
            priceLabel.getStyleClass().add("cart-item-price");
            priceLabel.setPrefHeight(Region.USE_COMPUTED_SIZE);
            priceLabel.setMaxHeight(Double.MAX_VALUE);

            // Add all elements to item row
            itemRow.getChildren().addAll(nameLabel, spacer, priceLabel, removeButton, editButton);

            return itemRow;
        } catch (Exception e) {
            System.out.println("Error creating row for item: " + orderItem.toString());
            e.printStackTrace();
        }
        return null;
    }

    private void removeItemFromCart(JSONObject orderItem) throws IOException {
        orderService.removeOrderItem(orderItem.getInt("orderItemId"));
    }

    private void editItemInCart(JSONObject orderItem) throws IOException {
        // Just use customizations that are unique to menu item to determine what customization screen is needed
        if (orderItem.has("pizzaSize")) {
            try {
                customizePizzaController.customizePizza(orderItem);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (orderItem.has("beverageSize")) {
            customizeBeverageController.customizeBeverage(orderItem);
        }
    }

    // Set customize pizza controller for customization
    public void setCustomizePizzaController(CustomizePizzaController customizePizzaController) {
        CartController.customizePizzaController = customizePizzaController;
    }

    // Set customize beverage controller for customization
    public void setCustomizeBeverageController(CustomizeBeverageController customizeBeverageController) {
        CartController.customizeBeverageController = customizeBeverageController;
    }

    // for testing
    public static void main(String[] args){

    }
}

