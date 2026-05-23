package com.mygdx.game;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class CombatLog {
    /** Stored for the HUD strip; renderCombatLog fits as many as the row width allows. */
    private static final int MAX_ENTRIES = 32;
    private final Deque<String> entries = new ArrayDeque<>();

    public void add(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        if (!entries.isEmpty() && message.equals(entries.peekLast())) {
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
