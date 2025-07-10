package dev.cele.gpg_notepad;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLaf;
import dev.cele.gpg_notepad.ui.MainWindow;
import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        LafHelper.setupThemeLaf();
        Settings.openIfNeeded();

        var mainWindow = new MainWindow();

        //parse args to check if there's a file to open
        if (args.length > 0) {
            //open the file
            mainWindow.openFile(args[0]);
        } else {
            // Restore previously opened tabs
            var openedTabs = OpenedTabs.getInstance();
            boolean hasOpenedTabs = false;
            
            // Open all previously opened files that still exist
            for (String filePath : openedTabs) {
                if (Files.exists(Path.of(filePath))) {
                    mainWindow.openFile(filePath);
                    hasOpenedTabs = true;
                }
            }
            
            // If no tabs were opened, create a new empty tab
            if (!hasOpenedTabs) {
                mainWindow.newTab();
            }
        }

        setupGlobalExceptionHandling();
    }

    public static void setupGlobalExceptionHandling() {
        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(MainWindow.getInstance(), ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        });
    }

}