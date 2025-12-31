package pizza_shop_system.gui.order;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import org.json.JSONArray;
import org.json.JSONObject;
import pizza_shop_system.account.services.AccountService;
import pizza_shop_system.gui.base.BaseController;
import pizza_shop_system.gui.utils.StyleUtil;

import javax.print.DocFlavor;
import java.io.IOException;

public class OrderCompletionController extends BaseController {
    @FXML
    private Button orderAgainButton;
    @FXML
    private VBox receiptContainer;

    private final CheckoutController checkoutController = new CheckoutController();
    private final CartController cartController = new CartController();
    private final AccountService accountService = new AccountService();
    private final StyleUtil styleUtil = new StyleUtil();

    private void stylize() {
        styleUtil.fadeButtonOnHover(orderAgainButton);
    }

    @FXML
    private void initialize() {
        stylize();

        checkoutController.setOrderCompletionController(this);
        orderAgainButton.setOnAction(e -> orderAgain());
    }

    private void orderAgain() {
        sceneController.switchScene("Menu");
    }

    public VBox createReceipt(JSONObject order) throws IOException {
        String BUSINESS_NAME = "Bob's Pizza Place";
        String BUSINESS_ADDRESS = "123 Pizza Street";
        String BUSINESS_NUMBER = "555-555-555";

        VBox receiptBox = new VBox();
        receiptBox.setFillWidth(true);
        receiptBox.setAlignment(Pos.TOP_CENTER);
        receiptBox.setSpacing(10);

        // Business Header
        receiptBox.getChildren().add(new Label("=== " + BUSINESS_NAME + " ==="));
        receiptBox.getChildren().add(new Label(BUSINESS_ADDRESS));
        receiptBox.getChildren().add(new Label(BUSINESS_NUMBER));
        receiptBox.getChildren().add(new Label("Customer Name: " + accountService.getUserName(order.getInt("accountId"))));

        // Order Details
        String date = order.getString("date");
        String orderType = order.getJSONObject("orderInformation").getString("orderType");
        String paymentMethod = order.getJSONObject("paymentInformation").getString("paymentMethod");
        double total = order.getDouble("total");

        receiptBox.getChildren().add(new Label("\nDate & Time: " + date));
        receiptBox.getChildren().add(new Label("Order Type: " + orderType));
        if (orderType.equals("Delivery") && order.getJSONObject("orderInformation").has("address")) {
            receiptBox.getChildren().add(new Label("Delivery Address: " + order.getJSONObject("orderInformation").getString("address")));
        }
        receiptBox.getChildren().add(new Label("Payment Method: " + paymentMethod));

        // Ordered Items
        receiptBox.getChildren().add(new Label("\nItems Ordered:"));
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

            receiptBox.getChildren().add(new Label(" - " + itemName + ": $" + price));
        }

        // Total Amount
        receiptBox.getChildren().add(new Label("\nAmount Due: $" + total));

        // Signature Section (if payment method is Credit Card)
        if (paymentMethod.equalsIgnoreCase("Credit Card")) {
            receiptBox.getChildren().add(new Label("Signature:"));
            receiptBox.getChildren().add(new Region());
            receiptBox.getChildren().add(new Region());
            receiptBox.getChildren().add(new Region());
            receiptBox.getChildren().add(new Line(0, 0, 200, 0)); // Placeholder for signature
        }

        return receiptBox;
    }

    public void updateReceiptDisplay(JSONObject order) throws IOException {
        receiptContainer.getChildren().clear();
        receiptContainer.getChildren().add(createReceipt(order));
    }

    private void exitApplication() {
        System.out.println("Exiting application...");
    }

}