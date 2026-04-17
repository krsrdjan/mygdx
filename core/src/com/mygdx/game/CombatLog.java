package com.mygdx.game;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class CombatLog {
    private static final int MAX_ENTRIES = 3;
    private final Deque<String> entries = new ArrayDeque<>();

    public void add(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        entries.addLast(message);
        while (entries.size() > MAX_ENTRIES) {
            entries.removeFirst();
        }
    }

    public List<String> recent() {
        return new ArrayList<>(entries);
    }
}
