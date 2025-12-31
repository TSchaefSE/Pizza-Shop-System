package pizza_shop_system.gui.order;

import org.json.JSONObject;
import pizza_shop_system.gui.base.BaseController;
import pizza_shop_system.gui.reports.ReportsController;
import pizza_shop_system.gui.utils.StyleUtil;
import pizza_shop_system.order.services.OrderService;
import pizza_shop_system.order.entities.Payment;
import pizza_shop_system.account.services.AccountService;
import pizza_shop_system.account.entities.User;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;

public class CheckoutController extends BaseController {

    @FXML private Label totalLabel;
    @FXML private ComboBox<String> paymentMethodComboBox;

    @FXML private Label cardNumberLabel;
    @FXML private TextField cardNumberField;
    @FXML private Label cvvLabel;
    @FXML private TextField cvvField;
    @FXML private Label expirationLabel;
    @FXML private TextField expirationDateField;

    @FXML private Label checkNumberLabel;
    @FXML private TextField checkNumberField;

    @FXML private RadioButton deliveryRadioButton;
    @FXML private RadioButton pickupRadioButton;

    @FXML private Label addressLabel;
    @FXML private TextField addressField;

    @FXML private Button confirmButton;

    private static double orderTotal;

    private final OrderService orderService = new OrderService();
    private final AccountService accountService = new AccountService();
    private static OrderCompletionController orderCompletionController;
    private final ReportsController reportsController = new ReportsController();
    private final StyleUtil styleUtil = new StyleUtil();

    public void setOrderCompletionController (OrderCompletionController orderCompletionController) {
        CheckoutController.orderCompletionController = orderCompletionController;
    }

    private void stylize() {
        styleUtil.fadeButtonOnHover(confirmButton);
    }

    @FXML
    private void initialize() {
        stylize();

        ToggleGroup orderTypeGroup = new ToggleGroup();
        deliveryRadioButton.setToggleGroup(orderTypeGroup);
        pickupRadioButton.setToggleGroup(orderTypeGroup);
        pickupRadioButton.setSelected(true);

        paymentMethodComboBox.getItems().addAll("Credit Card", "Debit Card", "Check", "Cash");

        hideAllPaymentFields();
        addressLabel.setVisible(false);
        addressField.setVisible(false);

        deliveryRadioButton.setOnAction(e -> showDeliveryFields());
        pickupRadioButton.setOnAction(e -> hideDeliveryFields());
        paymentMethodComboBox.setOnAction(e -> handlePaymentMethodSelection());

        // confirm button action
        confirmButton.setOnAction(e -> {
            try {
                processOrder();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    public void setTotal(double total) {
        this.orderTotal = total;
        totalLabel.setText("Total: $" + String.format("%.2f", total));
    }

    private void hideAllPaymentFields() {
        cardNumberLabel.setVisible(false);
        cardNumberField.setVisible(false);
        cvvLabel.setVisible(false);
        cvvField.setVisible(false);
        expirationLabel.setVisible(false);
        expirationDateField.setVisible(false);
        checkNumberLabel.setVisible(false);
        checkNumberField.setVisible(false);
    }

    private void handlePaymentMethodSelection() {
        String paymentMethod = paymentMethodComboBox.getValue();
        hideAllPaymentFields();

        if ("Credit Card".equals(paymentMethod) || "Debit Card".equals(paymentMethod)) {
            cardNumberLabel.setVisible(true);
            cardNumberField.setVisible(true);
            cvvLabel.setVisible(true);
            cvvField.setVisible(true);
            expirationLabel.setVisible(true);
            expirationDateField.setVisible(true);
        } else if ("Check".equals(paymentMethod)) {
            checkNumberLabel.setVisible(true);
            checkNumberField.setVisible(true);
        }
    }

    private void showDeliveryFields() {
        addressLabel.setVisible(true);
        addressField.setVisible(true);
    }

    private void hideDeliveryFields() {
        addressLabel.setVisible(false);
        addressField.setVisible(false);
    }

    // If payment was success then add order information for the selected order type into current order
    private void addOrderInformationToOrder(String orderType, String address) throws IOException {
        JSONObject orderInfo = new JSONObject();
        orderInfo.put("orderType", orderType);
        if (address != null && orderType.equals("Delivery")) {
            orderInfo.put("address", address);
        }
        orderService.addOrderInformation(orderInfo);
    }

    // If payment was a success then put the payment information for the selected payment into current order
    private void addPaymentInformationToOrder(String paymentMethod) throws IOException {
        JSONObject paymentInfo = new JSONObject(); // Create a payment info JSONObject to put into the current orders paymentInformation
        paymentInfo.put("paymentMethod", paymentMethod);

        switch (paymentMethod) {

            case "Credit Card", "Debit Card" -> {
                paymentInfo.put("cardNumber", cardNumberField.getText());
                paymentInfo.put("cvv", cvvField.getText());
                paymentInfo.put("expirationDate", expirationDateField.getText());
            }

            case "Check" -> {
                paymentInfo.put("checkNumber", checkNumberField.getText());
            }

            case "Cash" -> {
                // Do nothing for cash. Only useful if we add change handling later on.
            }
        }

        orderService.addPaymentInformation(paymentInfo);
    }

    private void processOrder() throws IOException {

        setTotal(orderService.getCurrentOrderTotal());
        String orderType = deliveryRadioButton.isSelected() ? "Delivery" : "Pickup";
        String paymentMethod = paymentMethodComboBox.getValue();

        if (paymentMethod == null || paymentMethod.isEmpty()) {
            System.out.println("Please select a payment method.");
            return;
        }

        if ("Delivery".equals(orderType) &&
                (addressField.getText().isEmpty() || addressField.getText().isBlank())) {
            System.out.println("Please enter a valid delivery address.");
            return;
        }

        Payment payment = new Payment(orderService);
        boolean success = false;

        // Tbh this is not handled well, because I don't check for valid input before storing it
        // However, It's still perfectly functional because invalid input would just get overwritten by whatever input was valid later onn
        addPaymentInformationToOrder(paymentMethod);
        addOrderInformationToOrder(orderType, addressField.getText());

        switch (paymentMethod) {
            case "Credit Card":
            case "Debit Card": //Added debit card here just in case
                if (cardNumberField.getText().isEmpty() ||
                        cvvField.getText().isEmpty() ||
                        expirationDateField.getText().isEmpty()) {
                    System.out.println("Please fill in all credit card details.");
                    return;
                }

                AccountService accountService = new AccountService();
                User currentUser = accountService.getActiveUser();

                if (currentUser == null) {
                    System.out.println("No user logged in.");
                    return;
                }

                payment.processCard(currentUser);
                success = payment.getIsProcessed();
                break;

            case "Check": //These assume exact amount is paid
                if (checkNumberField.getText().isEmpty()) {
                    System.out.println("Please enter a valid check number.");
                    return;
                }

                payment.processCheck(orderTotal);
                success = payment.getIsProcessed();
                break;

            case "Cash": //Assumes exact amount will prob need to update with userInput to calc change
                payment.processCash(orderTotal);
                success = payment.getIsProcessed();
                break;

            default:
                System.out.println("Unsupported payment type.");
                return;
        }


        if (success) {
            if (accountService.getActiveUserId() == 0) {
                sceneController.switchScene("Login");
                reportsController.showError("Must be logged in to place an order.");
            } else {
                orderCompletionController.updateReceiptDisplay(orderService.getPreviousOrder());
                sceneController.switchScene("OrderCompletion");
            }
        }
        else {
            System.out.println("Payment failed. Please try again.");
        }
    }

}
