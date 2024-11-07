package dev.cele.gpg_notepad.ui;

import jnafilechooser.api.JnaFileChooser;

import java.awt.Window;
import java.io.File;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileDialogHelper {
    private FileDialogHelper() {}

    private static final JnaFileChooser fc = new JnaFileChooser();

    static {
        var gpgArmoredFileExtension = new String[]{"asc", "txt", "gpgtxt", "gpg", "gpg.txt"};
        var extensionsString = Stream.of(gpgArmoredFileExtension)
                .map(it -> "." + it)
                .collect(Collectors.joining(","));

        fc.addFilter("GPG Ascii Armored Files ("+extensionsString+")", gpgArmoredFileExtension);
        fc.addFilter("All Files", "*");
    }


    public static File showOpenDialog(Window parent) {
        if(fc.showOpenDialog(parent)){
            return fc.getSelectedFile();
        }
        return null;
    }

    public static File showSaveDialog(Window parent) {
        if(fc.showSaveDialog(parent)){
            return fc.getSelectedFile();
        }
        return null;
    }

}
