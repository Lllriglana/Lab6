package ITMO.Lab5.managers;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Stores the last executed command names.
 */
public final class HistoryManager {
    private final Deque<String> commandHistory;
    private final int capacity;

    public HistoryManager(int capacity) {
        this.capacity = capacity;
        this.commandHistory = new ArrayDeque<String>();
    }

    public void add(String commandName) {
        if (capacity <= 0) {
            return;
        }

        if (commandHistory.size() >= capacity) {
            commandHistory.removeFirst();
        }
        commandHistory.addLast(commandName);
    }

    public List<String> getHistory() {
        return new ArrayList<String>(commandHistory);
    }
}
