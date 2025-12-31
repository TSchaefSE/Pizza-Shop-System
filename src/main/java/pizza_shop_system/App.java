package pizza_shop_system;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import pizza_shop_system.gui.navigation.NavigationBarController;
import pizza_shop_system.gui.base.SceneController;
import pizza_shop_system.utils.DataFileManager;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.Enumeration;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class App extends Application {

    // Adds all scenes in scenes folder recursively
    private void addScenes(SceneController sceneController, String baseScenesPath) {
        try {
            URL url = getClass().getResource(baseScenesPath);
            if (url == null) {
                System.out.println("Scenes path not found: " + baseScenesPath);
                return;
            }

            if (url.getProtocol().equals("file")) {
                // Development mode (running from IntelliJ)
                File folder = new File(url.toURI());
                File[] files = folder.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile() && file.getName().endsWith(".fxml")) {
                            String sceneName = file.getName().replace(".fxml", "");
                            String scenePath = baseScenesPath + "/" + file.getName();
                            sceneController.addScene(sceneName, scenePath);
                        } else if (file.isDirectory()) {
                            // RECURSION to go into subfolders
                            addScenes(sceneController, baseScenesPath + "/" + file.getName());
                        }
                    }
                }
            } else if (url.getProtocol().equals("jar")) {
                // Running in packaged JAR: Read all JAR entries
                String jarBaseScenesPath = baseScenesPath.startsWith("/") ? baseScenesPath.substring(1) : baseScenesPath;
                String jarPath = URLDecoder.decode(url.getPath().substring(5, url.getPath().indexOf("!")), StandardCharsets.UTF_8); // Remove spaces

                try (JarFile jar = new JarFile(jarPath)) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if (name.startsWith(jarBaseScenesPath) && name.endsWith(".fxml")) {
                            String sceneFileName = name.substring(name.lastIndexOf("/") + 1);
                            String sceneName = sceneFileName.replace(".fxml", "");
                            String scenePath = "/" + name; // Absolute resource path
                            sceneController.addScene(sceneName, scenePath);
                        }
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            System.out.println("Could not find jar file " + e.getMessage());
        }
    }

    // Attaches all stylesheets in stylesheets recursively
    private void attachStylesheets(Scene scene, String baseStylesheetPath) {
        try {
            URL url = getClass().getResource(baseStylesheetPath);
            if (url == null) {
                System.out.println("Stylesheets path not found: " + baseStylesheetPath);
                return;
            }

            if (url.getProtocol().equals("file")) {
                // Development mode (inside IDE)
                File folder = new File(url.toURI());
                File[] files = folder.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile() && file.getName().endsWith(".css")) {
                            String stylesheetPath = baseStylesheetPath + "/" + file.getName();
                            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(stylesheetPath)).toExternalForm());
                        } else if (file.isDirectory()) {
                            attachStylesheets(scene, baseStylesheetPath + "/" + file.getName());
                        }
                    }
                }
            } else if (url.getProtocol().equals("jar")) {
                // Running in packaged JAR: Read all JAR entries
                String jarBaseStylesheetPath = baseStylesheetPath.startsWith("/") ? baseStylesheetPath.substring(1) : baseStylesheetPath;
                String jarPath = URLDecoder.decode(url.getPath().substring(5, url.getPath().indexOf("!")), StandardCharsets.UTF_8);

                try (JarFile jar = new JarFile(jarPath)) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if (name.startsWith(jarBaseStylesheetPath) && name.endsWith(".css")) {
                            String stylesheetPath = "/" + name; // Use absolute resource path
                            URL resourceUrl = getClass().getResource(stylesheetPath);

                            if (resourceUrl != null) {
                                scene.getStylesheets().add(resourceUrl.toExternalForm());
                            } else {
                                System.out.println("WARNING: Stylesheet not found -> " + stylesheetPath);
                            }
                        }
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            System.out.println("Could not find jar file " + e.getMessage());
        }
    }

    private SceneController loadScenes(BorderPane mainLayout, FXMLLoader navigationBarLoader) {
        SceneController sceneController = new SceneController(mainLayout);
        addScenes(sceneController, "/pizza_shop_system/scenes");

        // Set main SceneController inside the navigation bar
        NavigationBarController navigationBarController = navigationBarLoader.getController();
        navigationBarController.setSceneController(sceneController);

        return sceneController;
    }

    // Start the GUI
    @Override
    public void start(Stage primaryStage) throws IOException {

        DataFileManager dataFileManager = new DataFileManager();
        dataFileManager.initializeDataFiles(); // Important to create a copy of the data_files on the local machine for writing

        // Constants
        String DEFAULT_SCENE = "Home";
        int DEFAULT_WIDTH = 1000;
        int DEFAULT_HEIGHT = 800;
        boolean START_IN_FULLSCREEN = true;

        FXMLLoader navigationBarLoader = new FXMLLoader(getClass().getResource("/pizza_shop_system/scenes/navigation/NavigationBar.fxml"));
        Parent navigationBar = navigationBarLoader.load();

        // Create the border pane that holds the nav bar at the top and other scenes in the center
        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(navigationBar); // Keeps navigation bar fixed at the top of all scenes

        Scene scene = new Scene(mainLayout, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(START_IN_FULLSCREEN);
        primaryStage.show();

        // Load all scenes upfront so we don't have to reload each time (Return scene controller so we can switch to default scene)
        SceneController sceneController = loadScenes(mainLayout, navigationBarLoader);

        // Attach ALL CSS stylesheets
        attachStylesheets(scene, "/pizza_shop_system/stylesheets");

        // Switch to default scene
        sceneController.switchScene(DEFAULT_SCENE); // Set the default scene
    }

    public static void main(String[] args) {
        launch(args);
    }

}