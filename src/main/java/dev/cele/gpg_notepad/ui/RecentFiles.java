package dev.cele.gpg_notepad.ui;

import lombok.Getter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class RecentFiles extends LinkedList<String> {
    private ArrayList<Consumer<RecentFiles>> watchers = new ArrayList<>();

    @Getter
    static private RecentFiles instance = new RecentFiles();

    private RecentFiles() {
        super();

        //read file from ~/ if it exists
        var path = Path.of(System.getProperty("user.home"), ".gpg_notepad", "recent_files");
        if(Files.exists(path)) {
            try {
                this.addAll(Files.readAllLines(path));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean add(String s) {
        if(this.contains(s)) {
            this.remove(s);
        }
        if(this.size() >= 10) {
            this.remove(0);
        }
        var output = super.add(s);
        watchers.forEach(w -> w.accept(this));
        return output;
    }

    @Override
    public boolean addAll(Collection<? extends String> c) {
        var it = c.iterator();
        while(it.hasNext()) {
            this.add(it.next());
        }
        return true;
    }

    public boolean save() {
        var path = Path.of(System.getProperty("user.home"), ".gpg_notepad", "recent_files");
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, this, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void addWatcher(Consumer<RecentFiles> watcher) {
        watchers.add(watcher);
        watcher.accept(this);
    }

    public void removeWatcher(Consumer<RecentFiles> watcher) {
        watchers.remove(watcher);
    }

}
