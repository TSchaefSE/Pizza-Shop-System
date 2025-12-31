package pizza_shop_system.gui.account;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import org.json.JSONObject;
import pizza_shop_system.account.services.AccountService;
import pizza_shop_system.gui.base.BaseController;
import pizza_shop_system.gui.order.CartController;
import pizza_shop_system.order.services.OrderService;

import java.io.IOException;

public class ViewTransactionsController extends BaseController {

    @FXML
    private ScrollPane transactionsScrollPane;
    @FXML
    private VBox transactionsContainer;

    private final AccountMenuController accountMenuController = new AccountMenuController();
    private final AccountService accountService = new AccountService();
    private final OrderService orderService = new OrderService();
    private final CartController cartController = new CartController();


    private VBox createTransactionVBox(JSONObject order) {
        VBox vbox = new VBox();
        vbox.setFillWidth(true);
        vbox.setSpacing(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.getStyleClass().add("view-transaction-vbox");

        // Extract order details
        String date = order.getString("date");
        String orderType = order.getJSONObject("orderInformation").getString("orderType");
        String paymentMethod = order.getJSONObject("paymentInformation").getString("paymentMethod");
        double total = order.getDouble("total");

        // Create labels for order summary
        vbox.getChildren().add(new Label("Date & Time: " + date));
        vbox.getChildren().add(new Label("Order Type: " + orderType));
        if (orderType.equals("Delivery") && order.getJSONObject("orderInformation").has("address")) {
            vbox.getChildren().add(new Label("Delivery Address: " + order.getJSONObject("orderInformation").getString("address")));
        }
        vbox.getChildren().add(new Label("Payment Method: " + paymentMethod));
        vbox.getChildren().add(new Label("Total Amount: $" + total));

        // Process ordered items
        vbox.getChildren().add(new Label("Items Ordered:"));
        JSONArray orderItems = order.getJSONArray("orderItems");
        for (int i = 0; i < orderItems.length(); i++) {
            JSONObject item = orderItems.getJSONObject(i);
            String itemName = null;
            if (item.has("pizzaSize")) {
                itemName = cartController.generatePizzaName(item);
            } else if (item.has("beverageSize")) {
                itemName = cartController.generateBeverageName(item);
            }
            double price = item.getDouble("price");

            vbox.getChildren().add(new Label(" - " + itemName + ": $" + price));
        }

        return vbox;
    }

    private void displayTransaction(JSONObject order) {
        VBox transactionVBox = createTransactionVBox(order);
        transactionsContainer.getChildren().add(transactionVBox);
    }

    public void updateTransactionsDisplay() throws IOException {
        transactionsContainer.getChildren().clear();

        int activeUserId = accountService.getActiveUserId();
        JSONArray accountOrders = orderService.getOrdersByAccount(activeUserId);
        for (int i = 0; i < accountOrders.length(); i++) {
            JSONObject order = accountOrders.getJSONObject(i);
            displayTransaction(order);
        }
    }

    private void styalize() {
        transactionsContainer.setFillWidth(true);
        transactionsContainer.setSpacing(10);
    }

    @FXML
    public void initialize() {
        styalize();
        accountMenuController.setViewTransactionsController(this);
    }
}
