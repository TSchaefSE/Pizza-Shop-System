package pizza_shop_system.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class DataFileManager {
    private final String DATA_FOLDER = "data_files";

    private final String[] REQUIRED_FILES = {
            "Users.json",
            "Orders.json",
            "MenuItemCustomizations.json",
            "MenuItems.json"
    };

    // Called once at App startup
    public void initializeDataFiles() throws IOException {
        File dataFolder = new File(DATA_FOLDER);
        if (!dataFolder.exists()) {
            if (!dataFolder.mkdirs()) {
                throw new IOException("Failed to create data_files directory");
            }
        }

        for (String fileName : REQUIRED_FILES) {
            File realFile = new File(dataFolder, fileName);
            if (!realFile.exists()) {
                try {
                    copyResourceToFile("/pizza_shop_system/data_files/" + fileName, realFile);
                } catch (FileNotFoundException e) {
                    System.out.println("Failed to find file: " + fileName);
                }
            }
        }
    }

    private void copyResourceToFile(String resourcePath, File targetFile) throws IOException {
        try (InputStream in = DataFileManager.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new FileNotFoundException("Starter resource not found: " + resourcePath);
            }
            Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public File getDataFile(String fileName) {
        return new File(DATA_FOLDER, fileName);
    }
}
