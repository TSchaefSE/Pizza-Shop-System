package pizza_shop_system.gui.base;

public class BaseController {
    protected SceneController sceneController;

    public void setSceneController(SceneController sceneController) {
        this.sceneController = sceneController;
    }

    public void switchScene(String sceneName) {
        if (sceneController != null) {
            sceneController.switchScene(sceneName);
        } else {
            System.out.println("SceneController is not set.");
        }
    }
}
