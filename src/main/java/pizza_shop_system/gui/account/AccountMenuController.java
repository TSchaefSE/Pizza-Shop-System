package pizza_shop_system.gui.account;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import org.json.JSONArray;
import org.json.JSONObject;
import pizza_shop_system.account.services.AccountService;
import pizza_shop_system.gui.base.BaseController;
import pizza_shop_system.gui.navigation.NavigationBarController;
import pizza_shop_system.gui.order.GenerateReceiptsController;
import pizza_shop_system.gui.utils.StyleUtil;

import java.io.IOException;

public class AccountMenuController extends BaseController {
    @FXML
    private Label accountTypeLabel, saveStatusLabel;
    @FXML
    private TextField nameField, emailField;
    @FXML
    private PasswordField oldPasswordField, newPasswordField;
    @FXML
    private Button saveButton, generateReportsButton, generateReceiptsButton, manageAccountsButton, logoutButton, viewTransactionsButton;
    @FXML
    private StackPane managerMenusContainer;

    private final AccountService accountService = new AccountService();
    private final NavigationBarController navigationBarController = new NavigationBarController();
    private static ViewTransactionsController viewTransactionsController;
    private static GenerateReceiptsController generateReceiptsController;
    private final StyleUtil styleUtil = new StyleUtil();

    public void setViewTransactionsController(ViewTransactionsController viewTransactionsController) {
        AccountMenuController.viewTransactionsController = viewTransactionsController;
    }

    public void setGenerateReceiptsController(GenerateReceiptsController generateReceiptsController) {
        AccountMenuController.generateReceiptsController = generateReceiptsController;
    }

    public void updateAccountInformationDisplay() throws IOException {
        int activeUserId = accountService.getActiveUserId();
        JSONArray users = accountService.loadUsers();
        JSONObject activeUser = null;

        // set the active user
        for (int i = 0; i < users.length(); i++) {
            JSONObject user = users.getJSONObject(i);
            if (user.getInt("user_id") == activeUserId) {
                activeUser = user;
                break;
            }
        }

        if (activeUser != null) {

            nameField.setText(activeUser.getString("name"));
            emailField.setText(activeUser.getString("email"));

        } else {
            System.out.println("Failed to load account information");
        }
    }

    private void setSaveStatusLabel(String text, int waitTime) {
        new Thread(() -> {
            try {
                Platform.runLater(() -> saveStatusLabel.setText(text)); // Wrapped in Platform.runLater so it executes on UI thread
                Thread.sleep(waitTime * 1000L);
                Platform.runLater(() -> saveStatusLabel.setText(""));
            } catch (InterruptedException e) {
                System.out.println("Could not set text of status label " + e.getClass());
            }
        }).start();
    }

    private void saveAccountInformation() throws IOException {

        int activeUserId = accountService.getActiveUserId();

        // Save name and email
        accountService.updateUser(activeUserId, "name", nameField.getText());
        accountService.updateUser(activeUserId, "email", emailField.getText());

        // Check if old password is correct and if it is set the users password to new password
        JSONArray users = accountService.loadUsers();
        for (int i = 0; i < users.length(); i++) {
            JSONObject user = users.getJSONObject(i);
            if (user.getInt("user_id") == activeUserId && !oldPasswordField.getText().isEmpty()) {
                if (user.getString("password").equals(oldPasswordField.getText())) {
                    accountService.updateUser(accountService.getActiveUserId(), "password", newPasswordField.getText());
                } else {
                   setSaveStatusLabel("Old password is incorrect", 3);
                }
                break;
            }
        }

        // Everything successfully saved
        setSaveStatusLabel("Saved!", 1);
    }

    private void logout() {
        accountService.logout();
        sceneController.switchScene("Login");
    }

    public void setManagerMenuVisible(boolean visible) {
        if (visible) {
            managerMenusContainer.setVisible(true);
            accountTypeLabel.setText("Manager"); // Can implicitly set the account type here as well
        } else {
            managerMenusContainer.setVisible(false);
            accountTypeLabel.setText("Customer");
        };
    }

    private void stylize() {
        styleUtil.fadeButtonOnHover(viewTransactionsButton);
        styleUtil.fadeButtonOnHover(generateReceiptsButton);
        styleUtil.fadeButtonOnHover(manageAccountsButton);
        styleUtil.fadeButtonOnHover(logoutButton);
        styleUtil.fadeButtonOnHover(saveButton);
        styleUtil.fadeButtonOnHover(generateReportsButton);
        styleUtil.fadeButtonOnHover(manageAccountsButton);
    }

    @FXML
    public void initialize() {
        stylize();

        accountService.setAccountMenuController(this);
        navigationBarController.setAccountMenuController(this);
        managerMenusContainer.setVisible(false);

        // set on actions for buttons
        generateReportsButton.setOnAction(_ -> sceneController.switchScene("Reports"));
        generateReceiptsButton.setOnAction(_ -> {
            try {
                generateReceiptsController.updateOrdersDisplay();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            sceneController.switchScene("GenerateReceipts");
        });
        manageAccountsButton.setOnAction(_ -> sceneController.switchScene("ManageAccounts"));
        logoutButton.setOnAction(_ -> logout());
        saveButton.setOnAction(_ -> {
            try {
                saveAccountInformation();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        viewTransactionsButton.setOnAction(_ -> {
            try {
                viewTransactionsController.updateTransactionsDisplay();
                sceneController.switchScene("ViewTransactions");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
