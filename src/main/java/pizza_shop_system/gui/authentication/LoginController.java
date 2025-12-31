package pizza_shop_system.gui.authentication;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import pizza_shop_system.account.services.AccountService;
import pizza_shop_system.gui.base.BaseController;
import pizza_shop_system.gui.utils.StyleUtil;

import java.io.IOException;
import java.util.Objects;

public class LoginController extends BaseController {
    private final AccountService accountService = new AccountService();
    private final StyleUtil styleUtil = new StyleUtil();

    @FXML
    private VBox resultsContainer;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton, signupButton;
    @FXML
    private ImageView logoImage;
    @FXML
    private TextField visiblePasswordField;
    @FXML
    private Button togglePasswordButton;

    private boolean passwordVisible = false;

    private void switchToSignUpScreen() {
        sceneController.switchScene("SignUp");
    }


    // Email or password was invalid so notify user of this
    private void displayIncorrectEmailOrPassword() {
        Label label = new Label("Incorrect Email or Password");
        label.setAlignment(Pos.CENTER);
        resultsContainer.getChildren().add(label);
    }

    // Clear displays of login screen
    private void displaySuccessfulLogin() {
        resultsContainer.getChildren().clear(); // Clear since login worked
        emailField.setText("");
        passwordField.setText("");
    }

    // Attempt to log in and display the result
    private void login() throws IOException {
        resultsContainer.getChildren().clear(); // Clear previous results

        String result = accountService.login(emailField.getText(), passwordField.getText());

        switch (result) {
            case "Success" -> {
                displaySuccessfulLogin();
                System.out.println("Log in Success");

                // Check account type of active user and switch accordingly
                var user = accountService.getActiveUser();

                if (user != null) {
                    if ("manager".equalsIgnoreCase(user.getAccountType())) {
                        sceneController.switchScene("AccountMenu");
                    } else {
                        sceneController.switchScene("Menu");
                    }
                } else {
                    System.out.println("No active user found.");
                }
            }
            case "EmailDoesNotExist" -> {
                displayIncorrectEmailOrPassword();
                System.out.println("Email Does Not Exist");
            }
            case "IncorrectPassword" -> {
                displayIncorrectEmailOrPassword();
                System.out.println("Incorrect Password");
            }
            default -> System.out.println("Unknown Error");
        }
    }

    @FXML
    public void initialize() {

        visiblePasswordField.toBack();

        logoImage.setImage(new Image(Objects.requireNonNull(getClass()
                .getResourceAsStream("/pizza_shop_system/images/Bobs_Logo.png"))));

        //Ensures password fields are synced
        passwordField.textProperty().addListener((obs, oldText, newText) -> visiblePasswordField.setText(newText));
        visiblePasswordField.textProperty().addListener((obs, oldText, newText) -> passwordField.setText(newText));

        togglePasswordButton.setOnAction(event -> {
            passwordVisible = !passwordVisible;
            visiblePasswordField.setVisible(passwordVisible);
            visiblePasswordField.setManaged(passwordVisible);

            passwordField.setVisible(!passwordVisible);
            passwordField.setManaged(!passwordVisible);
        });

        loginButton.setOnAction(e -> {
            try {
                login();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        signupButton.setOnAction(e -> switchToSignUpScreen());
        styleUtil.fadeButtonOnHover(loginButton);
        styleUtil.fadeButtonOnHover(signupButton);
    }
}