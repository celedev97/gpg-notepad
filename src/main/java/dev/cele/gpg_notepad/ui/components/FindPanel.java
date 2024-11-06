package dev.cele.gpg_notepad.ui.components;

import javax.swing.*;
import java.awt.*;

public class FindPanel extends JPanel {

    JButton expandButton = new JButton("^");
    JTextField findTextArea = new JTextField();
    JButton searchUpButton = new JButton("↑");
    JButton searchDownButton = new JButton("↓");
    JButton closeFindPanelButton = new JButton("X");

    JPanel replacePanel = new JPanel();
    JTextField replaceTextArea = new JTextField();
    JButton replaceButton = new JButton("Replace");
    JButton replaceAllButton = new JButton("Replace All");


    public FindPanel(Editor editor) {
        super();
        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 0;

        this.add(expandButton, gbc);
        gbc.gridx++;

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        this.add(findTextArea, gbc);

        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.gridx++;
        this.add(searchUpButton, gbc);
        gbc.gridx++;
        this.add(searchDownButton, gbc);
        gbc.gridx++;
        this.add(closeFindPanelButton, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 5;
        this.add(replacePanel, gbc);

        replacePanel.setLayout(new GridBagLayout());
        GridBagConstraints replaceGbc = new GridBagConstraints();
        replaceGbc.fill = GridBagConstraints.HORIZONTAL;
        replaceGbc.gridx = 0;
        replaceGbc.gridy = 0;
        replacePanel.add(replaceTextArea, replaceGbc);
        replaceGbc.gridx++;
        replacePanel.add(replaceButton, replaceGbc);
        replaceGbc.gridx++;
        replacePanel.add(replaceAllButton, replaceGbc);
        replacePanel.setVisible(false);


        this.closeFindPanelButton.addActionListener(e -> {
            this.setVisible(false);
            editor.requestFocus();
        });



    }
}
