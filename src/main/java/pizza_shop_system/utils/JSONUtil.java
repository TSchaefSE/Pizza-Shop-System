package pizza_shop_system.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class JSONUtil {

    public JSONArray loadJSONArray(String filePath) throws IOException {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                return new JSONArray(content);
            } else {
                InputStream in = getClass().getResourceAsStream("/" + filePath);
                if (in == null) return new JSONArray();
                String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                return new JSONArray(content);
            }
        } catch (Exception e) {
            System.out.println("Failed to load JSONArray from: " + filePath);
            return new JSONArray();
        }
    }

    public JSONObject loadJSONObject(String filePath) throws IOException {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                return new JSONObject(content);
            } else {
                InputStream in = getClass().getResourceAsStream("/" + filePath);
                if (in == null) return new JSONObject();
                String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                return new JSONObject(content);
            }
        } catch (Exception e) {
            System.out.println("Failed to load JSONObject from: " + filePath);
            return new JSONObject();
        }
    }

    public void saveJSONArray(String filePath, JSONArray contents) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(contents.toString(4));
        }
    }

    public void saveJSONObject(String filePath, JSONObject contents) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(contents.toString(4));
        }
    }
}
