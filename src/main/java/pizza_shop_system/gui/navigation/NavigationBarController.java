package pizza_shop_system.gui.navigation;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import pizza_shop_system.account.services.AccountService;
import pizza_shop_system.account.entities.User;
import pizza_shop_system.gui.account.AccountMenuController;
import pizza_shop_system.gui.base.BaseController;
import pizza_shop_system.gui.utils.StyleUtil;

import java.io.IOException;
import java.util.Objects;

public class NavigationBarController extends BaseController {

    public HBox navigationBar;
    @FXML private Button buttonHome;
    @FXML private Button buttonMenu;
    @FXML private Button buttonCart;
    @FXML private Button buttonBack;
    @FXML private Button buttonForward;
    @FXML private Button buttonAccount;

    private final AccountService accountService = new AccountService();
    private static AccountMenuController accountMenuController;
    private final StyleUtil styleUtil = new StyleUtil();

    private void setHomeButtonImage() {
        ImageView imageView = new ImageView();
        imageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pizza_shop_system/images/Bobs_Logo.png"))));
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(80);
        imageView.setFitHeight(40);
        buttonHome.setGraphic(imageView);
    }

    private void setBackAndFowardButtonImage() {
        ImageView backButtonImageView = new ImageView();
        backButtonImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pizza_shop_system/images/left-arrow.png"))));
        backButtonImageView.setPreserveRatio(true);
        backButtonImageView.setFitWidth(30);
        backButtonImageView.setFitHeight(25);

        ImageView fowardButtonImageView = new ImageView();
        fowardButtonImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pizza_shop_system/images/right-arrow.png"))));
        fowardButtonImageView.setPreserveRatio(true);
        fowardButtonImageView.setFitWidth(30);
        fowardButtonImageView.setFitHeight(25);

        // Make arrow white
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(1);
        backButtonImageView.setEffect(colorAdjust);
        fowardButtonImageView.setEffect(colorAdjust);

        buttonBack.setGraphic(backButtonImageView);
        buttonForward.setGraphic(fowardButtonImageView);

    }

    public void setAccountMenuController(AccountMenuController accountMenuController) {
        NavigationBarController.accountMenuController = accountMenuController;
    }

    @FXML
    public void initialize() {
        buttonHome.setOnAction(_ -> sceneController.switchScene("Home"));
        buttonMenu.setOnAction(_ -> sceneController.switchScene("Menu"));
        buttonCart.setOnAction(_ -> sceneController.switchScene("Cart"));
        buttonAccount.setOnAction(_ -> {
            if (accountService.getActiveUserId() != 0) {
                sceneController.switchScene("AccountMenu");
                try {
                    accountMenuController.updateAccountInformationDisplay();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                sceneController.switchScene("Login");
            }
        });

        buttonBack.setOnAction(_ -> sceneController.switchToPreviousScene());
        buttonForward.setOnAction(_ -> sceneController.switchToForwardScene());
        setHomeButtonImage();
        setBackAndFowardButtonImage();
        navigationBar.setViewOrder(-1); // Keep navbar on top of all other scenes so its drop shadow can be seen

        // Apply hover fade effect to every button in navbar
        for (Node node : navigationBar.getChildren()) {
            if (node instanceof Button button) {
                styleUtil.fadeButtonOnHover(button);
            }
        }

    }
}
