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
    private final EditorStatus status = new EditorStatus("Ready", 1, 1, 0, 100);

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

    private FindPanel findPanel = new FindPanel(this);

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

        //add the find panel on top, then hide it
        add(findPanel, BorderLayout.NORTH);
        findPanel.setVisible(false);

        //add a change listener to the text area for changing the isSaved variable
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setSaved(false);
                status.setCharacters(textArea.getText().length());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setSaved(false);
                status.setCharacters(textArea.getText().length());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setSaved(false);
                status.setCharacters(textArea.getText().length());
            }
        });

        //add a caret listener to the text area for changing the caret position label
        textArea.addCaretListener(e -> {
            try {
                var caretPosition = textArea.getCaretPosition();
                var lineNumber = textArea.getLineOfOffset(caretPosition) + 1;
                var columnNumber = caretPosition - textArea.getLineStartOffset(lineNumber - 1) + 1;

                //update the status
                status.setCaret(lineNumber, columnNumber);
            } catch (Exception ignored) {
            }
        });

        scrollPane.addMouseWheelListener(e -> {
            if(e.isControlDown()) {
                e.consume();
                if(e.getWheelRotation() > 0) {
                    zoomOut();
                }else {
                    zoomIn();
                }
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
        this.status.setStatus("Creating temporary file...");
        var tempFile = Files.createTempFile("", "");
        tempFile.toFile().delete();
        var tmpPath = tempFile.toAbsolutePath().toString();

        //decrypt the file with gpg
        this.status.setStatus("Decrypting file...");
        ProcessBuilder processBuilder = new ProcessBuilder(
                "gpg", "--batch", "--yes", "-o", tmpPath, "--decrypt", filePath
        ).inheritIO();

        //redirect the error stream to the System.out
        processBuilder.redirectErrorStream(true);
        var process = processBuilder.start();

        //get the exit code
        var exitCode = process.waitFor();
        if (exitCode != 0) {
            this.status.setStatus("Failed to decrypt the file!");
            JOptionPane.showMessageDialog(this, "Failed to decrypt the file", "Error", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException("Failed to decrypt the file");
        }

        //read the decrypted file
        this.status.setStatus("Reading the decrypted file...");
        var text = Files.readString(Path.of(tmpPath), StandardCharsets.UTF_8);
        textArea.setText(text);

        //delete the temp file
        this.status.setStatus("Deleting temporary file...");
        Files.delete(tempFile);

        this.status.setStatus("Ready");
        setSaved(true);

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

    public void copy() {
        textArea.copy();
    }

    public void cut() {
        textArea.cut();
    }

    public void paste() {
        textArea.paste();
    }

    public void selectAll() {
        textArea.selectAll();
    }

    public void undo() {
        throw new RuntimeException("Not implemented");
    }

    public void redo() {
        throw new RuntimeException("Not implemented");
    }

    public void delete() {
        textArea.replaceSelection("");
    }

    public void find() {
        this.findPanel.setVisible(true);
    }

    public void findNext() {
    }

    public void findPrevious() {
    }

    public void replace() {
    }

    public void goToLine() {
        var lineStr = JOptionPane.showInputDialog(this, "Enter line number", "Go to Line", JOptionPane.QUESTION_MESSAGE);
        if (lineStr == null || lineStr.isEmpty()) {
            return;
        }

        //check if lineStr match a number or number:number
        var pattern = "(\\d+)(:\\d+)?";
        if (!lineStr.matches(pattern)) {
            JOptionPane.showMessageDialog(this, "Invalid line number", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //get the line number and column number
        var parts = lineStr.split(":");
        var lineNumber = Integer.parseInt(parts[0]);
        var columnNumber = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;

        //set the caret position
        try {
            var offset = textArea.getLineStartOffset(lineNumber - 1) + columnNumber - 1;
            textArea.setCaretPosition(offset);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid line number", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void insertTimeDate() {
        //get the current time and date in the user's locale format
        var timeDate = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG));
        textArea.insert(timeDate, textArea.getCaretPosition());
    }

    public void zoomIn() {
        this.status.addZoomLevel(+10);
        adjustFontSize();
    }

    public void zoomOut() {
        this.status.addZoomLevel(-10);
        adjustFontSize();
    }

    public void resetZoom() {
        this.status.setZoomLevel(100);
        adjustFontSize();
    }

    private void adjustFontSize(){
        var fontSize = 12 + (this.status.getZoomLevel() - 100) / 10f;
        textArea.setFont(textArea.getFont().deriveFont(fontSize));
    }

    public void toggleWordWrap() {
        textArea.setLineWrap(!textArea.getLineWrap());
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class EditorStatus {
        private String status;
        private Integer line;
        private Integer column;
        private Integer characters;
        private Integer zoomLevel;

        private void setStatus(String s) {
            this.status = s;
            updateStatus();
        }

        private void setCaret(int line, int column) {
            this.line = line;
            this.column = column;
            updateStatus();
        }

        private void addZoomLevel(int i) {
            this.setZoomLevel(this.zoomLevel + i);
            updateStatus();
        }

        private void setZoomLevel(int i) {
            this.zoomLevel = i;
            if (this.zoomLevel < 50) {
                this.zoomLevel = 50;
            }
            updateStatus();
        }

        public void setCharacters(int length) {
            this.characters = length;
            updateStatus();
        }

        private void updateStatus() {
            MainWindow.getInstance().getStatusBar().updateStatus(this);
        }
    }
}
