package pizza_shop_system.gui.order;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import org.json.JSONObject;
import pizza_shop_system.gui.account.AccountMenuController;
import pizza_shop_system.gui.base.BaseController;
import pizza_shop_system.order.services.OrderService;

import java.io.IOException;

public class GenerateReceiptsController extends BaseController {
    @FXML
    private VBox ordersContainer, receiptsContainer;

    private ToggleGroup ordersToggleGroup = new ToggleGroup();

    private final OrderService orderService = new OrderService();
    private final OrderCompletionController orderCompletionController = new OrderCompletionController();
    private final AccountMenuController accountMenuController = new AccountMenuController();

    private void updateReceiptDisplay(JSONObject order) throws IOException {
        receiptsContainer.getChildren().clear();
        receiptsContainer.getChildren().add(orderCompletionController.createReceipt(order));
    }

    public ToggleButton createOrderToggle(JSONObject order) {
        // Extract necessary order details
        int orderId = order.getInt("orderId");
        String date = order.optString("date");
        double total = order.getDouble("total");

        if (date.isEmpty()) {
            return null;
        }

        // Format button text
        String buttonText = String.format("Order #%d | %s | $%.2f", orderId, date, total);

        // Create and configure the ToggleButton
        ToggleButton toggleButton = new ToggleButton(buttonText);
        toggleButton.setMaxWidth(Double.MAX_VALUE);
        toggleButton.setPrefWidth(Region.USE_COMPUTED_SIZE);
        toggleButton.setToggleGroup(ordersToggleGroup);

        toggleButton.setOnAction(_ -> {
            try {
                updateReceiptDisplay(order);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return toggleButton;
    }

    public void updateOrdersDisplay() throws IOException {
        receiptsContainer.getChildren().clear();
        ordersContainer.getChildren().clear();

        JSONArray orders = orderService.getOrders();
        for (int i = 0; i < orders.length(); i++) {
            JSONObject order = orders.getJSONObject(i);
            ToggleButton toggleButton = createOrderToggle(order);
            if (toggleButton != null) {
                ordersContainer.getChildren().add(toggleButton);
            }
        }
    }

    @FXML
    private void initialize() {
        accountMenuController.setGenerateReceiptsController(this);
    }
}
