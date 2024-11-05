package dev.cele.gpg_notepad.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import dev.cele.gpg_notepad.ui.components.DnDTabbedPane;
import dev.cele.gpg_notepad.ui.components.Editor;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.BiConsumer;

public class MainWindow extends JFrame {
    @Getter
    private static MainWindow instance;

    JTabbedPane tabbedPane = new DnDTabbedPane();
    JMenuBar menuBar = new JMenuBar();


    public MainWindow() {
        super("GPG Notepad");

        if(instance != null) {
            throw new RuntimeException("Only one instance of MainWindow is allowed");
        }

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                var allSaved = true;
                for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                    var editor = (Editor) tabbedPane.getComponentAt(i);
                    if (!editor.isSaved()) {
                        allSaved = false;
                        break;
                    }
                }

                if(allSaved) {
                    e.getWindow().dispose();
                }else {
                    var result = JOptionPane.showConfirmDialog(MainWindow.this, "There are unsaved changes. Do you want to save them?", "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION);
                    if(result == JOptionPane.YES_OPTION) {
                        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                            var editor = (Editor) tabbedPane.getComponentAt(i);
                            if(!editor.saveFile()){
                                return;
                            }
                        }
                        e.getWindow().dispose();
                    }else if(result == JOptionPane.NO_OPTION) {
                        e.getWindow().dispose();
                    }
                }
            }

            @Override
            public void windowClosed(WindowEvent e) {
                System.out.println("Saving recent files");
                RecentFiles.getInstance().save();
            }
        });

        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);

        //create a border layout
        setLayout(new BorderLayout());

        //add the tabbed pane to the center
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_CLOSABLE, true);
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_CLOSE_CALLBACK, (BiConsumer<JTabbedPane, Integer>) (tabbedPane, tabIndex) -> {
            var editor = (Editor) tabbedPane.getComponentAt(tabIndex);
            editor.close();
        });


        add(tabbedPane, BorderLayout.CENTER);

        //create the file menu
        JMenu fileMenu = new JMenu("File");
        //New File
        JMenuItem newTabMenuItem = new JMenuItem("New File");
        newTabMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        newTabMenuItem.addActionListener(e -> newTab());
        fileMenu.add(newTabMenuItem);
        //Open File
        JMenuItem openFileMenuItem = new JMenuItem("Open File");
        openFileMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        openFileMenuItem.addActionListener(e -> openFile(null));
        fileMenu.add(openFileMenuItem);
        //Open Recent Files submenu
        JMenu openRecentFilesMenu = new JMenu("Open Recent File");
        var recentFiles = RecentFiles.getInstance();
        recentFiles.addWatcher(rf -> {
            openRecentFilesMenu.removeAll();
            for (var file : rf) {
                var menuItem = new JMenuItem(file);
                menuItem.addActionListener(e -> openFile(file));
                openRecentFilesMenu.add(menuItem);
            }
        });
        fileMenu.add(openRecentFilesMenu);
        //Save File
        JMenuItem saveFileMenuItem = new JMenuItem("Save File");
        saveFileMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        saveFileMenuItem.addActionListener(e -> {
            ((Editor) tabbedPane.getSelectedComponent()).saveFile();
        });
        fileMenu.add(saveFileMenuItem);
        //Save As File
        JMenuItem saveAsFileMenuItem = new JMenuItem("Save As");
        saveAsFileMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.SHIFT_DOWN_MASK));
        saveAsFileMenuItem.addActionListener(e -> {
            ((Editor) tabbedPane.getSelectedComponent()).saveAsFile();
        });
        fileMenu.add(saveAsFileMenuItem);
        //Save All Files
        JMenuItem saveAllFilesMenuItem = new JMenuItem("Save All");
        saveAllFilesMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.ALT_DOWN_MASK));
        saveAllFilesMenuItem.addActionListener(e -> {
            for(int i = 0; i < tabbedPane.getTabCount(); i++) {
                ((Editor) tabbedPane.getComponentAt(i)).saveFile();
            }
        });
        menuBar.add(fileMenu);

        //separator
        fileMenu.addSeparator();

        //close the tab
        JMenuItem closeTabMenuItem = new JMenuItem("Close Tab");
        closeTabMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        closeTabMenuItem.addActionListener(e -> {
            ((Editor) tabbedPane.getSelectedComponent()).close();
        });
        fileMenu.add(closeTabMenuItem);

        //close all tabs
        JMenuItem closeAllTabsMenuItem = new JMenuItem("Close All Tabs");
        closeAllTabsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.SHIFT_DOWN_MASK));
        closeAllTabsMenuItem.addActionListener(e -> {
            for(int i = tabbedPane.getTabCount() - 1; i >= 0; i--) {
                tabbedPane.setSelectedIndex(i);
                closeTabMenuItem.doClick();
            }
        });

        //exit
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(e -> {
            dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });

        //add the jmenu bar
        setJMenuBar(menuBar);

        instance = this;
    }

    public void openFile(String filePath) {
        if(filePath == null) {
            int result = Editor.openFileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                filePath = Editor.openFileChooser.getSelectedFile().getAbsolutePath();
            }else {
                return;
            }
        }

        var editor = new Editor(filePath);
        tabbedPane.addTab(editor.getTitle(), editor);

        //check if the current tab is an untitled empty tab
        var currentEditor = (Editor) tabbedPane.getSelectedComponent();
        if(currentEditor.getFilePath() == null && currentEditor.getText().isEmpty()) {
            tabbedPane.remove(tabbedPane.getSelectedIndex());
        }

        //go to the new tab
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
    }

    public void newTab() {
        var editor = new Editor();
        tabbedPane.addTab(editor.getTitle(), editor);
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
    }
}
