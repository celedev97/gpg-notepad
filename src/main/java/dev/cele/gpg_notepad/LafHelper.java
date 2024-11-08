package dev.cele.gpg_notepad;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.util.Map;

public class LafHelper {
    private LafHelper() {}

    static {
        FlatLaf.registerCustomDefaultsSource("dev.cele.gpg_notepad.themes");
    }

    public static final Map<String, LookAndFeel> lafMap = Map.of(
        "Dark", new FlatDarculaLaf(),
        "Light", new FlatLightLaf()
    );


    public static void setupThemeLaf() {
        setupThemeLaf(Settings.theme);
    }

    public static void setupThemeLaf(String theme) {
        FlatLaf.setup(
                lafMap.get(theme)
        );
    }
}
