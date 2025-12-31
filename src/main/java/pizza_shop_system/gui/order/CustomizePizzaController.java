package pizza_shop_system.gui.order;

import javafx.scene.control.*;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import org.json.JSONArray;
import org.json.JSONObject;
import pizza_shop_system.gui.base.BaseController;
import pizza_shop_system.gui.utils.StyleUtil;
import pizza_shop_system.order.services.OrderService;
import pizza_shop_system.utils.StringUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CustomizePizzaController extends BaseController {

    @FXML
    private GridPane toppingsContainer;
    @FXML
    private ToggleButton personalButton, smallButton, mediumButton, largeButton,
            regularButton, thinButton, stuffedButton;
    @FXML
    private Button addToOrderButton;
    @FXML
    private final ToggleGroup crustToggleGroup = new ToggleGroup();
    @FXML
    private final ToggleGroup sizeToggleGroup = new ToggleGroup();
    @FXML
    private ChoiceBox<Integer> quantityChoiceBox;

    private int orderItemId = 0;

    Map<String, CheckBox> toppingCheckBoxes = new HashMap<>(); // After setting up toppings store them in hashmap so they can be referenced when selecting defaults
    int MAX_TOPPINGS = 4;

    private final OrderService orderService = new OrderService();
    private final CartController cartController = new CartController();
    private final MenuController menuController = new MenuController();
    private JSONObject customizations;
    private final StyleUtil styleUtil = new StyleUtil();

    // Add buttons to their appropriate toggle groups
    private void setupToggleButtons() {

        // Size buttons
        personalButton.setToggleGroup(sizeToggleGroup);
        smallButton.setToggleGroup(sizeToggleGroup);
        mediumButton.setToggleGroup(sizeToggleGroup);
        largeButton.setToggleGroup(sizeToggleGroup);

        // Crust buttons
        regularButton.setToggleGroup(crustToggleGroup);
        thinButton.setToggleGroup(crustToggleGroup);
        stuffedButton.setToggleGroup(crustToggleGroup);
    }

    // Add all the options to quantity choice box up to MAX_QUANTITY
    private void setupQuantityChoiceBox() {

        //Constants
        int MAX_QUANTITY = 10;

        for (int i = 1; i <= MAX_QUANTITY; i++) {
            quantityChoiceBox.getItems().add(i);
        }
        quantityChoiceBox.setValue(1); // Default value
    }

    // Listen for check box selections and disable further selections if it exceeds or is equal to the MAX_TOPPINGS amount
    private void ensureMaxToppingsIsNotExceeded() {
        long selectedCount = toppingCheckBoxes.values().stream()
                .filter(CheckBox::isSelected)
                .count();

        // Disable unchecked checkboxes if limit is reached
        boolean disableOthers = selectedCount >= MAX_TOPPINGS;
        toppingCheckBoxes.values().forEach(cb -> {
            if (!cb.isSelected()) cb.setDisable(disableOthers);
        });
    }

    // Creates the checkboxes for each topping that is stored in the MenuItemCustomizations.json
    private void setupToppings() {

        StringUtil stringUtil = new StringUtil();
        JSONObject toppings = customizations.getJSONObject("toppings");

        // Constants
        int MAX_COLUMNS = 7;
        int MAX_ROWS = 999;

        int totalToppings = toppings.length();
        int columns = Math.min(MAX_COLUMNS, totalToppings);
        int row = 0, col = 0;

        for (String toppingName : toppings.keySet()) {
            if (row >= MAX_ROWS) break;

            toppingName = stringUtil.captilizeWord(toppingName);
            CheckBox toppingCheckBox = new CheckBox(toppingName);
            toppingCheckBox.getStyleClass().add("customize-checkbox");
            toppingsContainer.add(toppingCheckBox, col, row);
            toppingCheckBoxes.put(toppingName, toppingCheckBox); // Store check boxes in map for later reference

            // bind topping check box action
            toppingCheckBox.setOnAction(_ -> ensureMaxToppingsIsNotExceeded());

            col++;
            if (col >= columns) { // Move to next row when column limit is reached
                col = 0;
                row++;
            }
        }
    }

    private void setupCustomizePizzaGUI() {
        setupToggleButtons();
        setupQuantityChoiceBox();
        setupToppings();
    }

    private void addToOrder() {
        for (int i = 0; i < quantityChoiceBox.getValue(); i++) {
            JSONObject orderItem = new JSONObject();

            ToggleButton selectedSizeButton = (ToggleButton) sizeToggleGroup.getSelectedToggle();
            ToggleButton selectedCrustButton = (ToggleButton) crustToggleGroup.getSelectedToggle();
            String selectedSize = selectedSizeButton.getText().toLowerCase();
            String selectedCrust = selectedCrustButton.getText().toLowerCase();

            orderItem.put("pizzaSize", selectedSize);
            orderItem.put("crust", selectedCrust);

            JSONArray selectedToppings = new JSONArray();
            toppingCheckBoxes.values().forEach(checkbox -> {
                if (checkbox.isSelected()) {
                    String toppingName = checkbox.getText().toLowerCase();
                    selectedToppings.put(toppingName);
                }
            });

            orderItem.put("toppings", selectedToppings);

            try {

                if (orderItemId == 0) {
                    orderService.addOrderItem(orderItem);
                    sceneController.switchScene("Menu"); // Switch to menu so user can continue to add items
                } else {
                    orderService.updateOrderItem(orderItemId, orderItem); // If there was an order item then we update the order item selected to be edited
                    sceneController.switchScene("Cart"); // Switch to cart so user can see their changes
                    break;
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    // Set default customization option methods

    private void setDefaultPizzaSize(String pizzaSize) {
        switch (pizzaSize) {
            case "personal":
                personalButton.setSelected(true);
                break;
            case "small":
                smallButton.setSelected(true);
                break;
            case "large":
                largeButton.setSelected(true);
                break;
            default:
                mediumButton.setSelected(true);
        }
    }

    private void setDefaultCrustSize(String crust) {
        switch (crust) {
            case "thin":
                thinButton.setSelected(true);
                break;
            case "stuffed":
                stuffedButton.setSelected(true);
                break;
            default:
                regularButton.setSelected(true);
        }
    }

    private void setDefaultToppings(JSONArray itemToppings) {

        StringUtil stringUtil = new StringUtil();

        // Reset checkboxes to prevent rollovers
        toppingCheckBoxes.values().forEach(checkbox -> {checkbox.setSelected(false);});

        for (int i = 0; i < itemToppings.length(); i++) {
            String toppingName = itemToppings.getString(i);
            toppingName = stringUtil.captilizeWord(toppingName); // Just for capitalization
            toppingCheckBoxes.get(toppingName).setSelected(true); // Get topping check box by its name and then set it to true because it was selected by default
        }
        ensureMaxToppingsIsNotExceeded();
    }

    // Takes in an orderItem/MenuItem to get default customizations and switches to this scene for customization
    public void customizePizza(JSONObject orderItem) throws IOException {
        orderItemId = orderItem.optInt("orderItemId"); // try to get an order item id, if there is not one (0) this is a menu item not an order item from the cart

        String pizzaSize = orderItem.getString("pizzaSize");
        String crust = orderItem.getString("crust");
        JSONArray itemToppings = orderItem.getJSONArray("toppings");

        setDefaultPizzaSize(pizzaSize);
        setDefaultCrustSize(crust);
        setDefaultToppings(itemToppings);

        sceneController.switchScene("CustomizePizza");
    }

    private void stylize() {
        styleUtil.fadeButtonOnHover(addToOrderButton);
    }

    @FXML
    public void initialize() throws IOException {
        stylize();

        menuController.setCustomizePizzaController(this);
        customizations = orderService.loadCustomizations();
        setupCustomizePizzaGUI();
        cartController.setCustomizePizzaController(this);

        // bind add to order button action
        addToOrderButton.setOnAction(_ -> addToOrder());
    }
}
