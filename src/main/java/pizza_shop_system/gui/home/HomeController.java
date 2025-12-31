package pizza_shop_system.gui.home;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import pizza_shop_system.account.services.AccountService;
import pizza_shop_system.gui.base.BaseController;
import pizza_shop_system.gui.utils.StyleUtil;

import java.util.Objects;

public class HomeController extends BaseController {
    public Button buttonLogin;
    @FXML
    private Button orderButton;
    @FXML
    private ImageView logoView;

    private final AccountService accountService = new AccountService();
    private final StyleUtil styleUtil = new StyleUtil();

    private void stylize() {
     styleUtil.fadeButtonOnHover(orderButton);
     styleUtil.fadeButtonOnHover(buttonLogin);
    }

    @FXML
    private void initialize() {
        stylize();

        logoView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pizza_shop_system/images/Bobs_Logo.png"))));
        orderButton.setOnAction(e -> sceneController.switchScene("Menu"));
        buttonLogin.setOnAction(e -> {
            if (accountService.getActiveUserId() != 0) {
                sceneController.switchScene("AccountMenu");
            } else {
                sceneController.switchScene("Login");
            }
        });
    }
}