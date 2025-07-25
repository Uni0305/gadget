package io.wispforest.gadget.client.resource;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.TreeMap;

public class TreeEntry {
    public final String name;
    public final Map<String, TreeEntry> children = new TreeMap<>();
    public @Nullable String key;
    
    public TreeEntry(String name) {
        this.name = name;
    }

    public void addUnder(String[] path, String key) {
        TreeEntry current = this;

        for (String s : path) {
            if (!current.children.containsKey(s)) {
                TreeEntry newEntry = new TreeEntry(s);
                current.children.put(s, newEntry);
            }

            current = current.children.get(s);
        }

        current.key = key;
    }
}

