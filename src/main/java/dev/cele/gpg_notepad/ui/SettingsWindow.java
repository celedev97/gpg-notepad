package dev.cele.gpg_notepad.ui;

import dev.cele.gpg_notepad.Settings;

import javax.swing.*;
import java.awt.*;

public class SettingsWindow extends JDialog {

    public SettingsWindow(JFrame parent) {
        super(parent, "Settings", true);
        setSize(400, 300);

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
        themeComboBox.setSelectedItem(Settings.getTheme());
        gbc.gridx = 1;
        gbc.gridy = 0;
        add(themeComboBox, gbc);

        //row 2
        gbc.gridy++;

        //create the label for the recipient for encryption
        var recipientLabel = new JLabel("Recipient for encryption:");
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
            if(themeComboBox.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Please select a theme", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if(recipientTextField.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Please enter a recipient (it should be the email of the owner of the key that will be used for decryption)", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Settings.setTheme((String) themeComboBox.getSelectedItem());
            Settings.recipient = recipientTextField.getText();

            Settings.save();
        });

        //force the dialog to be in the center of the parent
        setLocationRelativeTo(parent);
        setVisible(true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

}
