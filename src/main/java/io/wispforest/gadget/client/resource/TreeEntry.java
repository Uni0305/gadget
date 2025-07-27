package io.wispforest.gadget.client.resource;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class TreeEntry<K> {
    public final String name;
    public final Map<String, TreeEntry<K>> children = new TreeMap<>();
    public @Nullable K key;
    
    public TreeEntry(String name) {
        this.name = name;
    }

    public void addUnder(String[] path, K key) {
        addUnder(Arrays.asList(path), key);
    }

    public void addUnder(Collection<String> path, K key) {
        TreeEntry<K> current = this;

        for (String s : path) {
            if (!current.children.containsKey(s)) {
                TreeEntry<K> newEntry = new TreeEntry<>(s);
                current.children.put(s, newEntry);
            }

            current = current.children.get(s);
        }

        current.key = key;
    }
}

