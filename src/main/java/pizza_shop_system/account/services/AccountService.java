package pizza_shop_system.account.services;

import org.json.JSONArray;
import org.json.JSONObject;
import pizza_shop_system.account.entities.User;
import pizza_shop_system.gui.account.AccountMenuController;
import pizza_shop_system.gui.account.ManageAccountsController;
import pizza_shop_system.order.entities.CreditCard;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;

public class AccountService {
    private static final String DATA_FILE = "data_files/Users.json";
    private static int activeUserId = 0;
    private static AccountMenuController accountMenuController;
    private static ManageAccountsController manageAccountsController;

    // Load full user data from the file
    private JSONObject loadUserData() throws IOException {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            JSONObject root = new JSONObject();
            root.put("nextUserId", 1);
            root.put("users", new JSONArray());
            saveUserData(root);
            return root;
        } else {
            String content = new String(Files.readAllBytes(file.toPath()));
            return new JSONObject(content);
        }
    }

    // Load just the users array
    public JSONArray loadUsers() throws IOException {
        JSONObject root = loadUserData();
        return root.getJSONArray("users");
    }

    public User getActiveUser() throws IOException {
        JSONArray users = loadUsers();

        for (int i = 0; i < users.length(); i++) {
            JSONObject user = users.getJSONObject(i);
            if(user.has("user_id") && user.getInt("user_id") == activeUserId){
                User userObj = new User(
                        user.getInt("user_id"),
                        user.getString("account_type"),
                        user.getString("email"),
                        user.getString("password"),
                        user.getString("name"),
                        user.getString("address"),
                        user.getString("phone_number")
                );

                if (user.has("credit_card")) {
                    JSONObject cc = user.getJSONObject("credit_card");
                    LocalDate expDate = LocalDate.parse(cc.getString("exp_date"));

                    if (!expDate.isBefore(LocalDate.now())) {
                        CreditCard card = new CreditCard(
                                cc.getString("type"),
                                cc.getString("number"),
                                cc.getString("holder_name"),
                                expDate,
                                cc.getInt("cvc_num")
                        );
                        userObj.setCreditCard(card);
                    } else {
                        System.out.println("Expired credit card found for user ID " + activeUserId);
                    }
                }

                return userObj;
            }
        }

        return null;
    }

    private void saveUserData(JSONObject root) throws IOException {
        try (FileWriter fileWriter = new FileWriter(DATA_FILE)) {
            fileWriter.write(root.toString(4));
        }
    }

    public void saveUsers(JSONArray users) throws IOException {
        JSONObject root = loadUserData();
        root.put("users", users);
        saveUserData(root);
    }

    public void saveCardForActiveUser(CreditCard card) throws IOException {
        JSONObject root = loadUserData();
        JSONArray users = root.getJSONArray("users");

        for (int i = 0; i < users.length(); i++) {
            JSONObject user = users.getJSONObject(i);
            if (user.has("user_id") && user.getInt("user_id") == activeUserId) {
                JSONObject cc = new JSONObject();
                cc.put("type", card.getType());
                cc.put("number", card.getCardNumber());
                cc.put("holder_name", card.getHolderName());
                cc.put("exp_date", card.getExpDate().toString());
                cc.put("cvc_num", card.getCvcNum());

                user.put("credit_card", cc);
                saveUserData(root);
                break;
            }
        }
    }

    private String determineAccountType(String email) {
        String pizzaShopOrganization = "@pizzashop.org";
        return email.endsWith(pizzaShopOrganization) ? "manager" : "customer";
    }

    public String signUp(String email, String password, String verifyPassword, String name, String address, String phoneNumber) throws IOException {
        JSONObject root = loadUserData();
        JSONArray users = root.getJSONArray("users");

        if (email.isEmpty() || password.isEmpty() || verifyPassword.isEmpty() || name.isEmpty() || address.isEmpty() || phoneNumber.isEmpty()) {
            return "EmptyField";
        }

        for (int i = 0; i < users.length(); i++) {
            JSONObject user = users.getJSONObject(i);
            if (user.optString("email").equalsIgnoreCase(email)) {
                return "DuplicateEmail";
            }
        }

        if (!password.equals(verifyPassword)) {
            return "PasswordsDoNotMatch";
        }

        int userId = root.getInt("nextUserId");
        root.put("nextUserId", userId + 1);

        JSONObject newUser = new JSONObject();
        newUser.put("user_id", userId);
        newUser.put("email", email);
        newUser.put("password", password);
        newUser.put("account_type", determineAccountType(email));
        newUser.put("name", name);
        newUser.put("address", address);
        newUser.put("phone_number", phoneNumber);

        users.put(newUser);

        saveUserData(root);

        return "Success";
    }

    public String login(String email, String password) throws IOException {
        JSONArray users = loadUsers();

        for (int i = 0; i < users.length(); i++) {
            JSONObject user = users.getJSONObject(i);
            if (user.optString("email").equalsIgnoreCase(email)) {
                if (user.optString("password").equals(password)) {
                    activeUserId = user.getInt("user_id");

                    if (user.getString("account_type").equals("manager")) {
                        accountMenuController.setManagerMenuVisible(true);
                        manageAccountsController.updateAccountsDisplay();
                    } else {
                        accountMenuController.setManagerMenuVisible(false);
                    }
                    accountMenuController.updateAccountInformationDisplay();
                    return "Success";
                } else {
                    return "IncorrectPassword";
                }
            }
        }

        return "EmailDoesNotExist";
    }

    public void logout() {
        activeUserId = 0;
    }

    public int getActiveUserId() {
        return activeUserId;
    }

    public String updateUser(int userId, String field, String newValue) throws IOException {
        JSONObject root = loadUserData();
        JSONArray users = root.getJSONArray("users");

        for (int i = 0; i < users.length(); i++) {
            JSONObject user = users.getJSONObject(i);

            if (user.getInt("user_id") == userId) {
                if (user.has(field)) {
                    user.put(field, newValue);
                    saveUserData(root);
                    return "User " + field + " updated successfully.";
                } else {
                    return "Invalid field: " + field;
                }
            }
        }

        return "User not found.";
    }

    public String getUserName(int userId) throws IOException {
        JSONArray users = loadUsers();
        for (int i = 0; i < users.length(); i++) {
            JSONObject user = users.getJSONObject(i);
            if (user.getInt("user_id") == userId) {
                return user.getString("name");
            }
        }

        return "N/A";
    }

    public String changePassword(int userId, String oldPassword, String newPassword) throws IOException {
        JSONObject root = loadUserData();
        JSONArray users = root.getJSONArray("users");

        for (int i = 0; i < users.length(); i++) {
            JSONObject user = users.getJSONObject(i);

            if (user.getInt("user_id") == userId) {
                if (!user.optString("password").equals(oldPassword)) {
                    return "Incorrect old password.";
                }

                user.put("password", newPassword);
                saveUserData(root);
                return "Password updated successfully.";
            }
        }

        return "User not found.";
    }

    public String removeUser(int userId) throws IOException {
        JSONObject root = loadUserData();
        JSONArray users = root.getJSONArray("users");

        for (int i = 0; i < users.length(); i++) {
            JSONObject user = users.getJSONObject(i);

            if (user.getInt("user_id") == userId) {
                users.remove(i);
                saveUserData(root);
                return "User removed successfully.";
            }
        }

        return "User not found.";
    }

    public void setAccountMenuController(AccountMenuController controller) {
        AccountService.accountMenuController = controller;
    }

    public void setManageAccountsController(ManageAccountsController controller) {
        AccountService.manageAccountsController = controller;
    }
}
