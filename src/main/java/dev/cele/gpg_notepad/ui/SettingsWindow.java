package dev.cele.gpg_notepad.ui;

import dev.cele.gpg_notepad.LafHelper;
import dev.cele.gpg_notepad.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;

public class SettingsWindow extends JDialog {

    private final WindowAdapter windowAdapter = new WindowAdapter() {
        @Override
        public void windowClosing(java.awt.event.WindowEvent windowEvent) {
            save();
        }
    };

    private JComboBox<String> themeComboBox;
    private JTextField recipientTextField;

    public SettingsWindow(JFrame parent) {
        super(parent, "Settings", true);

        initComponents();
    }

    private void initComponents(){
        //create a gridbag layout
        setLayout(new GridBagLayout());

        var gbc = new GridBagConstraints();

        //create the label for the theme
        var themeLabel = new JLabel("Theme:");
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 10, 5, 10);
        add(themeLabel, gbc);

        //row 1

        //create the combo box for the theme
        themeComboBox = new JComboBox<String>();
        LafHelper.lafMap.keySet().forEach(themeComboBox::addItem);
        themeComboBox.setSelectedItem(Settings.theme);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(themeComboBox, gbc);

        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;

        //row 2
        gbc.gridy++;

        //create the label for the recipient for encryption
        var recipientLabel = new JLabel("Recipient for encryption (email):");
        gbc.gridx = 0;
        add(recipientLabel, gbc);

        //create the text field for the recipient for encryption
        recipientTextField = new JTextField(20);
        gbc.gridx++;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        recipientTextField.setText(Settings.recipient);
        add(recipientTextField, gbc);

        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;

        //row 3

        //create the save button
        var saveButton = new JButton("Save");
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(saveButton, gbc);

        saveButton.addActionListener(e -> {
            save();
        });

        //force the dialog to be in the center of the parent
        setResizable(false);
        pack();
        setLocationRelativeTo(super.getParent());
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(windowAdapter);
        //disable resizing
    }

    private boolean save(){
        if(themeComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a theme", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if(recipientTextField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Please enter a recipient (it should be the email of the owner of the key that will be used for decryption)", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Settings.theme = (String) themeComboBox.getSelectedItem();
        Settings.recipient = recipientTextField.getText();

        Settings.save();

        //close the window
        LafHelper.setupThemeLaf();
        this.dispose();
        return true;
    }

}
