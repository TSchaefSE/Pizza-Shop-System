package pizza_shop_system.gui.account;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import org.json.JSONObject;
import pizza_shop_system.account.services.AccountService;
import pizza_shop_system.gui.base.BaseController;
import pizza_shop_system.gui.utils.StyleUtil;
import pizza_shop_system.utils.StringUtil;

import java.io.IOException;

public class ManageAccountsController extends BaseController {
    @FXML
    private VBox accountsVBox;

    private final AccountService accountService = new AccountService();
    private final StringUtil stringUtil = new StringUtil();
    private final StyleUtil styleUtil = new StyleUtil();

    // Add a row to account vbox with attached id, name, and role of said account
    private void addAccountRow(int id, String name, String role) {

        // Setup item row
        HBox accountRow = new HBox();
        accountRow.setSpacing(10);
        accountRow.setMaxWidth(Double.MAX_VALUE);
        accountRow.getStyleClass().add("account-row");

        // Create labels for ID, Name, and Role
        Label idLabel = new Label("User ID: " + id);

        Label nameLabel = new Label("Name: " + name);

        Label roleLabel = new Label("Role: " + stringUtil.captilizeWord(role));

        // Setup region
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.SOMETIMES);
        spacer.setPrefWidth(Region.USE_COMPUTED_SIZE);

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("account-button");
        styleUtil.fadeButtonOnHover(deleteButton);

        deleteButton.setOnAction(e -> {
            try {
                removeAccount(id);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        // Add all elements to the HBox
        accountRow.getChildren().addAll(idLabel, nameLabel, roleLabel, spacer, deleteButton);

        // Add the row to the VBox
        accountsVBox.getChildren().add(accountRow);
    }

    // remove selected account id from files
    private void removeAccount(int id) throws IOException {
        accountService.removeUser(id);
        updateAccountsDisplay();
    }

    // display all the account information of all users
    public void updateAccountsDisplay() throws IOException {
        accountsVBox.getChildren().clear(); // Clear previous accounts

        int activeUserId = accountService.getActiveUserId();
        JSONArray users = accountService.loadUsers();

        for (int i = 0; i < users.length(); i++) {
            JSONObject user = users.getJSONObject(i);
            int userId = user.getInt("user_id");
            if (userId == activeUserId) {continue;} // skip over the current manager account so it cannot be removed by mistake
            addAccountRow(userId, user.getString("name"), user.getString("account_type"));
        }
    }

    @FXML
    private void initialize() throws IOException {
        accountService.setManageAccountsController(this);
    }

}
