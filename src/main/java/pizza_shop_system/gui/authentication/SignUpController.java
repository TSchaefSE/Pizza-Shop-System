package pizza_shop_system.gui.authentication;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import pizza_shop_system.account.services.AccountService;
import pizza_shop_system.gui.base.BaseController;
import pizza_shop_system.gui.utils.StyleUtil;

import java.io.IOException;

public class SignUpController extends BaseController {
    private final AccountService accountService = new AccountService();

    @FXML
    private VBox resultsContainer;
    @FXML
    private TextField emailField, nameField, addressField, phoneNumberField;
    @FXML
    private PasswordField passwordField, verifyPasswordField;
    @FXML
    private Button signupButton, loginButton;

    private final StyleUtil styleUtil = new StyleUtil();

    private void displayResult(String result) {
        Label label = new Label(result);
        label.setAlignment(Pos.CENTER);
        resultsContainer.getChildren().add(label);
    }

    private void signUp() throws IOException {
        resultsContainer.getChildren().clear(); // Clear previous results

        String result = accountService.signUp(emailField.getText(), passwordField.getText(), verifyPasswordField.getText(), nameField.getText(), addressField.getText(), phoneNumberField.getText());

        // All possible results when attempting to sign up
        switch (result) {
            case "Success" -> sceneController.switchScene("Login");
            case "DuplicateEmail" -> displayResult("This email already belongs to an account.");
            case "PasswordsDoNotMatch" -> displayResult("Passwords do not match.");
            case "EmptyField" -> displayResult("Please fill out all fields.");
            default -> System.out.println("Unknown Error");
        }
    }

    private void switchToLoginScreen() {
        sceneController.switchScene("Login");
    }

    private void stylize() {
        styleUtil.fadeButtonOnHover(loginButton);
        styleUtil.fadeButtonOnHover(signupButton);
    }

    public void initialize() {
        stylize();

        signupButton.setOnAction(e -> {
            try {
                 signUp();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        loginButton.setOnAction(e -> switchToLoginScreen());
    }
}
