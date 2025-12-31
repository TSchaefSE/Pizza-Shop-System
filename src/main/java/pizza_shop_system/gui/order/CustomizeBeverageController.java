package pizza_shop_system.gui.order;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.json.JSONObject;
import pizza_shop_system.gui.base.BaseController;
import pizza_shop_system.gui.utils.StyleUtil;
import pizza_shop_system.order.services.OrderService;
import java.io.IOException;

public class CustomizeBeverageController extends BaseController {


    @FXML private ToggleButton smallSizeButton, mediumSizeButton, largeSizeButton,
            iceNoneButton, iceLightButton, iceRegularButton, iceExtraButton;

    @FXML final private ToggleGroup iceToggleGroup = new ToggleGroup();
    @FXML final private ToggleGroup sizeToggleGroup = new ToggleGroup();

    @FXML private ChoiceBox<Integer> quantityChoiceBox;

    @FXML private Button addToOrderButton;

    private int orderItemId = 0;
    private String orderItemName = "";

    private final MenuController menuController = new MenuController();
    private final CartController cartController = new CartController();
    private final OrderService orderService = new OrderService();
    private final StyleUtil styleUtil = new StyleUtil();

    // Add all the options to quantity choice box up to MAX_QUANTITY
    private void setupQuantityChoiceBox() {

        //Constants
        int MAX_QUANTITY = 10;

        for (int i = 1; i <= MAX_QUANTITY; i++) {
            quantityChoiceBox.getItems().add(i);
        }
        quantityChoiceBox.setValue(1); // Default value
    }

    private void setupToggleButtons() {
        smallSizeButton.setToggleGroup(sizeToggleGroup);
        mediumSizeButton.setToggleGroup(sizeToggleGroup);
        largeSizeButton.setToggleGroup(sizeToggleGroup);

        iceNoneButton.setToggleGroup(iceToggleGroup);
        iceLightButton.setToggleGroup(iceToggleGroup);
        iceRegularButton.setToggleGroup(iceToggleGroup);
        iceExtraButton.setToggleGroup(iceToggleGroup);
    }

    // Set default customization option methods

    private void setDefaultBeverageSize(String beverageSize) {
        switch (beverageSize) {
            case "small":
                smallSizeButton.setSelected(true);
                break;
            case "large":
                largeSizeButton.setSelected(true);
                break;
            default:
                mediumSizeButton.setSelected(true);
        }
    }

    private void setDefaultIce(String ice) {
        switch (ice) {
            case "none":
                iceNoneButton.setSelected(true);
                break;
            case "extra":
                iceExtraButton.setSelected(true);
                break;
            default:
                iceRegularButton.setSelected(true);
        }
    }

    // Takes in an orderItem/MenuItem to get default customizations and switches to this scene for customization
    public void customizeBeverage(JSONObject orderItem) {
        orderItemId = orderItem.optInt("orderItemId"); // try to get an order item id, if there is not one (0) this is a menu item not an order item from the cart
        orderItemName = orderItem.getString("name");

        String beverageSize = orderItem.getString("beverageSize");
        String ice = orderItem.getString("ice");

        setDefaultBeverageSize(beverageSize);
        setDefaultIce(ice);

        sceneController.switchScene("CustomizeBeverage");
    }

    private void addToOrder() {
        for (int i = 0; i < quantityChoiceBox.getValue(); i++) {
            JSONObject orderItem = new JSONObject();

            orderItem.put("name", orderItemName);

            ToggleButton selectedSizeButton = (ToggleButton) sizeToggleGroup.getSelectedToggle();
            ToggleButton selectedIceButton = (ToggleButton) iceToggleGroup.getSelectedToggle();
            String selectedSize = selectedSizeButton.getText().toLowerCase();
            String selectedIce = selectedIceButton.getText().toLowerCase();

            orderItem.put("beverageSize", selectedSize);
            orderItem.put("ice", selectedIce);

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

    private void stylize() {
        styleUtil.fadeButtonOnHover(addToOrderButton);
    }

    @FXML
    public void initialize() {
        stylize();

        menuController.setCustomizeBeverageController(this);
        cartController.setCustomizeBeverageController(this);

        setupToggleButtons();
        setupQuantityChoiceBox();

        // bind add to order button action
        addToOrderButton.setOnAction(_ -> addToOrder());
    }
    
}
