module org.example.demo {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires org.json;

    opens pizza_shop_system to javafx.fxml;
    exports pizza_shop_system;
    exports pizza_shop_system.account.entities;
    opens pizza_shop_system.account.entities to javafx.fxml;
    exports pizza_shop_system.account.services;
    opens pizza_shop_system.account.services to javafx.fxml;
    exports pizza_shop_system.gui.base;
    opens pizza_shop_system.gui.base to javafx.fxml;
    exports pizza_shop_system.gui.reports;
    opens pizza_shop_system.gui.reports to javafx.fxml;
    exports pizza_shop_system.gui.order;
    opens pizza_shop_system.gui.order to javafx.fxml;
    exports pizza_shop_system.gui.home;
    opens pizza_shop_system.gui.home to javafx.fxml;
    exports pizza_shop_system.gui.authentication;
    opens pizza_shop_system.gui.authentication to javafx.fxml;
    exports pizza_shop_system.gui.account;
    opens pizza_shop_system.gui.account to javafx.fxml;
    exports pizza_shop_system.gui.navigation;
    opens pizza_shop_system.gui.navigation to javafx.fxml;
    exports pizza_shop_system.menu.entities;
    opens pizza_shop_system.menu.entities to javafx.fxml;
    exports pizza_shop_system.order.entities;
    opens pizza_shop_system.order.entities to javafx.fxml;
}