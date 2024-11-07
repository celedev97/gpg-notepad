package dev.cele.gpg_notepad.ui.components;

import javax.swing.*;
import java.awt.*;

public class FindPanel extends JPanel {

    private JButton expandButton = new JButton("^");
    private JTextField findTextArea = new JTextField();
    private JButton searchUpButton = new JButton("↑");
    private JButton searchDownButton = new JButton("↓");
    private JButton closeFindPanelButton = new JButton("X");

    private JPanel replacePanel = new JPanel();
    private JTextField replaceTextArea = new JTextField();
    private JButton replaceButton = new JButton("Replace");
    private JButton replaceAllButton = new JButton("Replace All");


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

        gbc.gridx = 1;
        gbc.gridy++;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(replacePanel, gbc);

        replacePanel.setLayout(new GridBagLayout());
        GridBagConstraints replaceGbc = new GridBagConstraints();
        replaceGbc.fill = GridBagConstraints.HORIZONTAL;
        replaceGbc.gridx = 0;
        replaceGbc.gridy = 0;
        replaceGbc.weightx = 1;
        replacePanel.add(replaceTextArea, replaceGbc);

        replaceGbc.fill = GridBagConstraints.NONE;
        replaceGbc.weightx = 0;
        replaceGbc.gridx++;
        replacePanel.add(replaceButton, replaceGbc);
        replaceGbc.gridx++;
        replacePanel.add(replaceAllButton, replaceGbc);
        replacePanel.setVisible(false);


        this.closeFindPanelButton.addActionListener(e -> {
            this.setVisible(false);
            editor.requestFocus();
        });

        this.expandButton.addActionListener(e -> {
            setReplacePanelVisible(!replacePanel.isVisible());
        });

        this.searchUpButton.addActionListener(e -> {
            editor.findPrevious();
        });

        this.searchDownButton.addActionListener(e -> {
            editor.findNext();
        });

        this.replaceButton.addActionListener(e -> {
            editor.replaceNext();
        });

        this.replaceAllButton.addActionListener(e -> {
            editor.replaceAll();
        });

        this.findTextArea.addActionListener(e -> {
            editor.findNext();
        });
    }

    public String getFilter() {
        return findTextArea.getText();
    }

    public String getReplace() {
        return replaceTextArea.getText();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            findTextArea.requestFocus();
        }
    }

    public void setReplacePanelVisible(boolean visible) {
        replacePanel.setVisible(visible);
        expandButton.setText(visible ? "v" : "^");
    }
}
