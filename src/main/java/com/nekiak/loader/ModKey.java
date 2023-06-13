package com.nekiak.loader;

import javax.swing.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class ModKey {
    private static final String JSON_FILE_PATH = "ghostmodkey.json";
    private static final String JSON_KEY_NAME = "modKey";

    public static String getKey() {
        JSONObject json = readJsonFile();

        while (true) {
            if (json.containsKey(JSON_KEY_NAME)) {
                return (String) json.get(JSON_KEY_NAME);
            } else {
                JFrame frame = new JFrame("Ghost Macro auth");

                frame.setUndecorated( true );
                frame.setVisible( true );
                frame.setLocationRelativeTo( null );

                String key = JOptionPane.showInputDialog(frame, "Enter a new key");

                if (key == null || key.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Invalid key. Please enter a valid key.");
                } else {
                    json.put(JSON_KEY_NAME, key.trim());
                    saveJsonToFile(json);
                    frame.dispose();
                    return key.trim();
                }
            }
        }
    }

    public static void resetKey() {
        new File(JSON_FILE_PATH).delete();
    };



    private static JSONObject readJsonFile() {
        JSONParser parser = new JSONParser();

        try (FileReader fileReader = new FileReader(JSON_FILE_PATH)) {
            Object obj = parser.parse(fileReader);
            return (JSONObject) obj;
        } catch (IOException | ParseException e) {
            return new JSONObject();
        }
    }

    private static void saveJsonToFile(JSONObject json) {
        try (FileWriter fileWriter = new FileWriter(JSON_FILE_PATH)) {
            fileWriter.write(json.toJSONString());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving JSON file.");
        }
    }
}
