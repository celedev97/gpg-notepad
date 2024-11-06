package dev.cele.gpg_notepad.ui.components;

import dev.cele.gpg_notepad.Settings;
import dev.cele.gpg_notepad.RecentFiles;
import dev.cele.gpg_notepad.ui.MainWindow;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.format.FormatStyle;

public class Editor extends JPanel {

    public static final JFileChooser openFileChooser = new JFileChooser();
    public static final JFileChooser saveFileChooser = new JFileChooser();

    static {
        openFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        openFileChooser.setMultiSelectionEnabled(false);
        openFileChooser.setAcceptAllFileFilterUsed(false);
        openFileChooser.setFileFilter(new FileNameExtensionFilter("Armored Text File", "txt", "gpgtxt"));

        saveFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        saveFileChooser.setMultiSelectionEnabled(false);
        saveFileChooser.setFileFilter(new FileNameExtensionFilter("Armored TXT File", "txt"));
        saveFileChooser.setFileFilter(new FileNameExtensionFilter("Armored GPG TXT File", "gpgtxt"));
    }



    @Getter
    private String filePath;
    @Getter
    private boolean saved = true;

    public void setSaved(boolean saved) {
        if(saved != this.saved) {
            this.saved = saved;
            var tabbedPane = (JTabbedPane) getParent();
            tabbedPane.setTitleAt(tabbedPane.indexOfComponent(this), getTitle());
        }
        this.saved = saved;
    }

    private String recipient = Settings.recipient;

    private JTextArea textArea = new JTextArea();
    private String status = "Ready";
    private JLabel statusLabel = new JLabel("Ready");
    private JLabel charetPositionLabel = new JLabel("Ln 1, Col 1");
    private JLabel charactersCountLabel = new JLabel("0 Characters");
    private JLabel zoomLevelLabel = new JLabel("100%");
    private int zoomLevel = 100;

    public String getTitle() {
        var filePart = "Untitled";
        if(filePath != null) {
            filePart = Path.of(filePath).getFileName().toString();
            //remove extension if it exists
            var dotIndex = filePart.lastIndexOf(".");
            if(dotIndex != -1) {
                filePart = filePart.substring(0, dotIndex);
            }
        }
        return saved ? filePart : filePart + " *";
    }

    public Editor() {
        super();

        //set the layout to border layout
        setLayout(new BorderLayout());

        //add a scroll pane to the center, then add the text area to the scroll pane
        var scrollPane = new JScrollPane();
        scrollPane.setViewportView(textArea);
        add(scrollPane, BorderLayout.CENTER);

        //add the status bar to the bottom with 5px padding
        var statusBar = new JMenuBar();
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        statusBar.add(statusLabel);
        statusBar.add(new JSeparator(SwingConstants.VERTICAL));
        statusBar.add(charetPositionLabel);
        statusBar.add(new JSeparator(SwingConstants.VERTICAL));
        statusBar.add(charactersCountLabel);
        //add a big gap between the status bar and the right side
        statusBar.add(Box.createHorizontalGlue());
        //add the zoom level
        statusBar.add(new JSeparator(SwingConstants.VERTICAL));
        statusBar.add(zoomLevelLabel);
        statusBar.add(new JSeparator(SwingConstants.VERTICAL));


        add(statusBar, BorderLayout.SOUTH);

        //add a change listener to the text area for changing the isSaved variable
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setSaved(false);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setSaved(false);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setSaved(false);
            }
        });

        //add a caret listener to the text area for changing the caret position label
        textArea.addCaretListener(e -> {
            try {
                var caretPosition = textArea.getCaretPosition();
                var lineNumber = textArea.getLineOfOffset(caretPosition) + 1;
                var columnNumber = caretPosition - textArea.getLineStartOffset(lineNumber - 1) + 1;
                charetPositionLabel.setText("Ln " + lineNumber + ", Col " + columnNumber);
            } catch (Exception ex) {
                charetPositionLabel.setText("Ln 1, Col 1");
            }
        });
    }

    @SneakyThrows
    public Editor(String filePath) {
        this();
        this.filePath = filePath;
        new Thread(() -> this.readFile()).start();
    }

    @SneakyThrows
    private void readFile(){
        //create a temporary file and delete it, this will give us a temp file path
        status = "Creating temporary file...";
        SwingUtilities.invokeLater(() -> statusLabel.setText(status));
        var tempFile = Files.createTempFile("", "");
        tempFile.toFile().delete();
        var tmpPath = tempFile.toAbsolutePath().toString();

        //decrypt the file with gpg
        status = "Decrypting file...";
        SwingUtilities.invokeLater(() -> statusLabel.setText(status));
        ProcessBuilder processBuilder = new ProcessBuilder(
                "gpg", "--batch", "--yes", "-o", tmpPath, "--decrypt", filePath
        ).inheritIO();

        //redirect the error stream to the System.out
        processBuilder.redirectErrorStream(true);
        var process = processBuilder.start();

        //get the exit code
        var exitCode = process.waitFor();
        if (exitCode != 0) {
            SwingUtilities.invokeLater(() -> statusLabel.setText("Failed to decrypt the file!"));
            JOptionPane.showMessageDialog(this, "Failed to decrypt the file", "Error", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException("Failed to decrypt the file");
        }

        //read the decrypted file
        statusLabel.setText("Reading the decrypted file...");
        SwingUtilities.invokeLater(() -> statusLabel.setText(status));
        var text = Files.readString(Path.of(tmpPath), StandardCharsets.UTF_8);
        textArea.setText(text);

        //delete the temp file
        status = "Deleting temporary file...";
        SwingUtilities.invokeLater(() -> statusLabel.setText(status));
        Files.delete(tempFile);

        status = "Ready";
        setSaved(true);
        SwingUtilities.invokeLater(() -> statusLabel.setText(status));

        RecentFiles.getInstance().add(filePath);
    }

    public String getText() {
        return textArea.getText();
    }


    public boolean saveFile() {
        if(filePath != null) {
            //save the file
            return writeToFile(filePath);
        } else {
            //save as the file
            return saveAsFile();
        }
    }

    @SneakyThrows
    public boolean writeToFile(String filePath) {
        //write to a temp file
        var tempFile = Files.createTempFile("", "");
        var tmpPath = tempFile.toAbsolutePath().toString();

        //write the text to the file
        Files.writeString(tempFile, textArea.getText(), StandardCharsets.UTF_8);

        //encrypt the file with gpg
        ProcessBuilder processBuilder = new ProcessBuilder(
                "gpg", "--batch", "--yes", "--armor", "--recipient", recipient, "-o", (tmpPath + ".gpg"), "--encrypt", tmpPath
        );

        //redirect the error stream to the System.out
        processBuilder.redirectErrorStream(true);
        var process = processBuilder.start();
        process.getInputStream().transferTo(System.out);
        process.waitFor();

        //get the exit code
        var exitCode = process.exitValue();
        if (exitCode != 0) {
            throw new RuntimeException("Failed to encrypt the file");
        }

        //read the encrypted temp file and write it to the file path
        var output = Files.readAllBytes(Path.of(tmpPath + ".gpg"));

        //write the encrypted file to the file path
        Files.write(Path.of(filePath), output,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );

        //delete the temp file
        Files.delete(tempFile);
        Files.delete(Path.of(tmpPath + ".gpg"));

        setSaved(true);
        RecentFiles.getInstance().add(filePath);
        return true;
    }

    public boolean saveAsFile() {
        //save as
        int result = saveFileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            //save the file
            filePath = saveFileChooser.getSelectedFile().getAbsolutePath();
            return writeToFile(filePath);
        }
        return false;
    }

    public void close() {
        //check if the file is saved
        if (!saved) {
            //ask the user if they want to save the file
            var result = JOptionPane.showConfirmDialog(this, "Do you want to save the file?", "Save File", JOptionPane.YES_NO_CANCEL_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                saveFile();
            } else if (result == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }

        //remove the tab
        var tabbedPane = (JTabbedPane) getParent();
        tabbedPane.remove(tabbedPane.indexOfComponent(this));
    }
}
