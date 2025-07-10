package dev.cele.gpg_notepad;

import lombok.Getter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class OpenedTabs extends LinkedList<String> {
    @Getter
    static private OpenedTabs instance = new OpenedTabs();

    private OpenedTabs() {
        super();

        //read file from ~/.gpg_notepad/opened_tabs if it exists
        var path = Path.of(System.getProperty("user.home"), ".gpg_notepad", "opened_tabs");
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
            return false; // Don't add duplicates
        }
        return super.add(s);
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
        var path = Path.of(System.getProperty("user.home"), ".gpg_notepad", "opened_tabs");
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, this, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void clear() {
        super.clear();
        save();
    }
}