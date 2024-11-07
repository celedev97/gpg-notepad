package dev.cele.gpg_notepad.ui.components;

import javax.swing.*;

public class StatusBar extends JMenuBar {

    private final JLabel statusLabel = new JLabel();
    private final JLabel charetPositionLabel = new JLabel();
    private final JLabel charactersCountLabel = new JLabel();
    private final JLabel zoomLevelLabel = new JLabel();

    private Editor.EditorStatus status;
    
    public StatusBar() {
        super();
        //add the status bar to the bottom with 5px padding
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(statusLabel);
        add(new JSeparator(SwingConstants.VERTICAL));
        add(charetPositionLabel);
        add(new JSeparator(SwingConstants.VERTICAL));
        add(charactersCountLabel);
        //add a big gap between the status bar and the right side
        add(Box.createHorizontalGlue());
        //add the zoom level
        add(new JSeparator(SwingConstants.VERTICAL));
        add(zoomLevelLabel);
        add(new JSeparator(SwingConstants.VERTICAL));
    }

    public void updateStatus(Editor.EditorStatus status) {
        this.status = status;
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(this.status.getStatus());
            charetPositionLabel.setText("Ln " + this.status.getLine() + ", Col " + this.status.getColumn());
            charactersCountLabel.setText(this.status.getCharacters() + " characters");
            zoomLevelLabel.setText(this.status.getZoomLevel() + "%");
        });
    }
}
