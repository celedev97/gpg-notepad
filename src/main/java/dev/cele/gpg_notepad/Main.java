package dev.cele.gpg_notepad;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLaf;
import dev.cele.gpg_notepad.ui.MainWindow;

public class Main {
    public static void main(String[] args) {
        var laf = new FlatDarculaLaf();
        FlatLaf.setup(laf);

        var mainWindow = new MainWindow();

        //parse args to check if there's a file to open
        if (args.length > 0) {
            //open the file
            mainWindow.openFile(args[0]);
        } else {
            //open a new file
            mainWindow.newTab();
        }
    }
}