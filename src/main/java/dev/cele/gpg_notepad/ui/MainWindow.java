package dev.cele.gpg_notepad.ui;

import com.formdev.flatlaf.FlatClientProperties;
import dev.cele.gpg_notepad.RecentFiles;
import dev.cele.gpg_notepad.ui.components.DnDTabbedPane;
import dev.cele.gpg_notepad.ui.components.Editor;
import dev.cele.gpg_notepad.ui.components.StatusBar;
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
    JMenuBar menuBar;

    @Getter
    StatusBar statusBar = new StatusBar();

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

        //add the status bar
        add(statusBar, BorderLayout.SOUTH);

        //add a listener to the tabbed pane to update the status bar
        tabbedPane.addChangeListener(e -> {
            var editor = (Editor) tabbedPane.getSelectedComponent();
            if(editor != null) {
                statusBar.updateStatus(editor.getStatus());
            }
        });

        //add the jmenu bar
        setJMenuBar(
                initializeMenuBar()
        );

        instance = this;
    }

    private JMenuBar initializeMenuBar() {
        if(menuBar != null) {
            return menuBar;
        }

        menuBar = new JMenuBar();

        //#region File Menu
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
        fileMenu.add(closeAllTabsMenuItem);

        //exit
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(e -> {
            dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);
        //#endregion

        //#region Edit Menu
        JMenu editMenu = new JMenu("Edit");
        //Undo
        JMenuItem undoMenuItem = new JMenuItem("Undo");
        undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        undoMenuItem.addActionListener(e -> {
            var editor = (Editor) tabbedPane.getSelectedComponent();
            editor.undo();
        });
        editMenu.add(undoMenuItem);

        //Redo
        JMenuItem redoMenuItem = new JMenuItem("Redo");
        redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        redoMenuItem.addActionListener(e -> {
            var editor = (Editor) tabbedPane.getSelectedComponent();
            editor.redo();
        });
        editMenu.add(redoMenuItem);

        //separator
        editMenu.addSeparator();

        //Cut
        JMenuItem cutMenuItem = new JMenuItem("Cut");
        cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        cutMenuItem.addActionListener(e -> {
            var editor = (Editor) tabbedPane.getSelectedComponent();
            editor.cut();
        });
        editMenu.add(cutMenuItem);

        //Copy
        JMenuItem copyMenuItem = new JMenuItem("Copy");
        copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        copyMenuItem.addActionListener(e -> {
            var editor = (Editor) tabbedPane.getSelectedComponent();
            editor.copy();
        });
        editMenu.add(copyMenuItem);

        //Paste
        JMenuItem pasteMenuItem = new JMenuItem("Paste");
        pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        pasteMenuItem.addActionListener(e -> {
            var editor = (Editor) tabbedPane.getSelectedComponent();
            editor.paste();
        });

        //Delete
        JMenuItem deleteMenuItem = new JMenuItem("Delete");
        deleteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        deleteMenuItem.addActionListener(e -> {
            var editor = (Editor) tabbedPane.getSelectedComponent();
            editor.delete();
        });
        editMenu.add(pasteMenuItem);

        //separator
        editMenu.addSeparator();

        //Find
        JMenuItem findMenuItem = new JMenuItem("Find");
        findMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        findMenuItem.addActionListener(e -> {
            var editor = (Editor) tabbedPane.getSelectedComponent();
            editor.find();
        });
        editMenu.add(findMenuItem);

        //Find next
        JMenuItem findNextMenuItem = new JMenuItem("Find Next");
        findNextMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
        findNextMenuItem.addActionListener(e -> {
            var editor = (Editor) tabbedPane.getSelectedComponent();
            editor.findNext();
        });
        editMenu.add(findNextMenuItem);

        //Find previous
        JMenuItem findPreviousMenuItem = new JMenuItem("Find Previous");
        findPreviousMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_DOWN_MASK));
        findPreviousMenuItem.addActionListener(e -> {
            var editor = (Editor) tabbedPane.getSelectedComponent();
            editor.findPrevious();
        });
        editMenu.add(findPreviousMenuItem);

        //Replace
        JMenuItem replaceMenuItem = new JMenuItem("Replace");
        replaceMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        replaceMenuItem.addActionListener(e -> {
            var editor = (Editor) tabbedPane.getSelectedComponent();
            editor.replace();
        });
        editMenu.add(replaceMenuItem);

        //Go to line
        JMenuItem goToLineMenuItem = new JMenuItem("Go to Line");
        goToLineMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        goToLineMenuItem.addActionListener(e -> {
            var editor = (Editor) tabbedPane.getSelectedComponent();
            editor.goToLine();
        });
        editMenu.add(goToLineMenuItem);

        //separator
        editMenu.addSeparator();

        //Select All
        JMenuItem selectAllMenuItem = new JMenuItem("Select All");
        selectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        selectAllMenuItem.addActionListener(e -> {
            var editor = (Editor) tabbedPane.getSelectedComponent();
            editor.selectAll();
        });
        editMenu.add(selectAllMenuItem);

        //Time/Date
        JMenuItem timeDateMenuItem = new JMenuItem("Time/Date");
        timeDateMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        timeDateMenuItem.addActionListener(e -> {
            var editor = (Editor) tabbedPane.getSelectedComponent();
            editor.insertTimeDate();
        });
        editMenu.add(timeDateMenuItem);

        menuBar.add(editMenu);
        //#endregion

        //#region View Menu
        JMenu viewMenu = new JMenu("View");
        //Zoom menu
        JMenu zoomMenu = new JMenu("Zoom");
        //Zoom In
        JMenuItem zoomInMenuItem = new JMenuItem("Zoom In");
        zoomInMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        zoomInMenuItem.addActionListener(e -> {
            var editor = (Editor) tabbedPane.getSelectedComponent();
            editor.zoomIn();
        });
        zoomMenu.add(zoomInMenuItem);
        //Zoom Out
        JMenuItem zoomOutMenuItem = new JMenuItem("Zoom Out");
        zoomOutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        zoomOutMenuItem.addActionListener(e -> {
            var editor = (Editor) tabbedPane.getSelectedComponent();
            editor.zoomOut();
        });

        zoomMenu.add(zoomOutMenuItem);
        //Reset Zoom
        JMenuItem resetZoomMenuItem = new JMenuItem("Reset Zoom");
        resetZoomMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));

        resetZoomMenuItem.addActionListener(e -> {
            var editor = (Editor) tabbedPane.getSelectedComponent();
            editor.resetZoom();
        });
        zoomMenu.add(resetZoomMenuItem);

        viewMenu.add(zoomMenu);

        //word wrap
        JCheckBoxMenuItem wordWrapMenuItem = new JCheckBoxMenuItem("Word Wrap");
        wordWrapMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.SHIFT_DOWN_MASK));
        wordWrapMenuItem.addActionListener(e -> {
            var editor = (Editor) tabbedPane.getSelectedComponent();
            editor.toggleWordWrap();
        });

        menuBar.add(viewMenu);


        return menuBar;
    }

    public void openFileDialog() {
        SwingUtilities.invokeLater(() -> {
            var file = FileDialogHelper.showOpenDialog(this);
            if(file == null) {
                return;
            }
            openFile(file.getAbsolutePath());
        });
    }

    public void openFile(String filePath) {
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
