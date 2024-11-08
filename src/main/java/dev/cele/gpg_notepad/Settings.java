package dev.cele.gpg_notepad;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import dev.cele.gpg_notepad.ui.MainWindow;
import dev.cele.gpg_notepad.ui.SettingsWindow;
import lombok.SneakyThrows;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.regex.Pattern;

public class Settings {

    public static String theme = LafHelper.lafMap.keySet().stream().findFirst().orElse(null);
    public static String recipient = "";

    static {
        //read the settings file from ~/.gpg_notepad/settings
        var path = Path.of(System.getProperty("user.home"), ".gpg_notepad", "settings");
        if(Files.exists(path)) {
            //read the settings file
            try {
                Files.readAllLines(path).forEach(line -> {
                    //check if line stats with #
                    if(line.startsWith("#")) {
                        return;
                    }

                    //split the line by =
                    var equalsIndex = line.indexOf("=");
                    var key = line.substring(0, equalsIndex);
                    var value = line.substring(equalsIndex + 1).trim();

                    //set the value
                    var field = Arrays.stream(Settings.class.getDeclaredFields()).filter(f -> f.getName().equals(key)).findFirst().orElse(null);

                    if(field != null) {
                        try {
                            if(field.getType().equals(String.class)) {
                                field.set(null, value);
                            } else if(field.getType().equals(Integer.class)) {
                                field.set(null, Integer.parseInt(value));
                            } else if(field.getType().equals(Boolean.class)) {
                                field.set(null, Boolean.parseBoolean(value));
                            }
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Settings() {}

    public static void openIfNeeded() {
        //check if the recipient is empty, in that case show the settings window
        if(recipient.isEmpty()) {
            //open a new Setting Windows on top of the main window
            SwingUtilities.invokeLater(() -> {
                new SettingsWindow(MainWindow.getInstance());
            });
        }
    }

    @SneakyThrows
    public static void save(){
        //check if the settings directory exists
        var settingsDir = Path.of(System.getProperty("user.home"), ".gpg_notepad");
        if(!Files.exists(settingsDir)) {
            Files.createDirectories(settingsDir);
        }

        //check if the settings file exists
        var settingsFile = Path.of(System.getProperty("user.home"), ".gpg_notepad", "settings");
        var settingsFileContent = Files.exists(settingsFile) ? Files.readString(settingsFile) : "";

        //update the settings file content
        for(var field : Settings.class.getDeclaredFields()) {
            var fieldRegex = Pattern.compile("^" + field.getName() + ".*$", Pattern.MULTILINE);
            var newLine = field.getName() + "=" + field.get(null);

            //check if the field is present in the settings file, if it is replace it, if not add it
            if(fieldRegex.matcher(settingsFileContent).find()) {
                settingsFileContent = fieldRegex.matcher(settingsFileContent).replaceAll(newLine);
            } else {
                settingsFileContent += newLine + "\n";
            }
        }

        //write the settings to the file
        Files.writeString(settingsFile, settingsFileContent, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
