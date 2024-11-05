package dev.cele.gpg_notepad.ui;

import javax.swing.*;
import java.awt.*;

public class SettingsWindow extends JPanel {

    public SettingsWindow(JFrame parent) {
        setSize(400, 300);
        setVisible(true);

        //create a gridbag layout
        setLayout(new GridBagLayout());

        var gbc = new GridBagConstraints();

        //create the label for the theme
        var themeLabel = new JLabel("Theme:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        add(themeLabel, gbc);

        //row 1

        //create the combo box for the theme
        var themeComboBox = new JComboBox<String>();
        themeComboBox.addItem("Dark");
        themeComboBox.addItem("Light");
        themeComboBox.setSelectedItem(Settings.theme);
        gbc.gridx = 1;
        gbc.gridy = 0;
        add(themeComboBox, gbc);

        //row 2
        gbc.gridy++;

        //create the label for the recipient for encryption
        var recipientLabel = new JLabel("Recipient:");
        gbc.gridx = 0;
        add(recipientLabel, gbc);

        //create the text field for the recipient for encryption
        var recipientTextField = new JTextField();
        gbc.gridx++;
        recipientTextField.setText(Settings.recipient);
        add(recipientTextField, gbc);

        //row 3

        //create the save button
        var saveButton = new JButton("Save");
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        add(saveButton, gbc);

        saveButton.addActionListener(e -> {
            Settings.theme = (String) themeComboBox.getSelectedItem();
            Settings.recipient = recipientTextField.getText();

            Settings.save();
        });

        //draw ui
        revalidate();
    }

}
