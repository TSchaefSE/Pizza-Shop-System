package pizza_shop_system.gui.utils;

import javafx.animation.*;
import javafx.scene.control.Button;
import javafx.util.Duration;

public class StyleUtil {

    public void fadeButtonOnHover(Button button) {
        // Create a fade transition for hover effect
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), button);
        fadeIn.setFromValue(1.0);
        fadeIn.setToValue(0.7);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), button);
        fadeOut.setFromValue(0.7);
        fadeOut.setToValue(1.0);

        // Apply transitions when hovering
        button.setOnMouseEntered(e -> {
            fadeOut.stop(); // Ensure fadeOut doesn't interfere
            fadeIn.play();
        });

        button.setOnMouseExited(e -> {
            fadeIn.stop(); // Stop fade-in before playing fade-out
            fadeOut.play();
        });
    }

}
