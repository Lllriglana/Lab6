package ITMO.Lab5.commands;

import ITMO.Lab5.exceptions.CommandException;
import ITMO.Lab5.managers.HistoryManager;

import java.io.PrintStream;
import java.util.List;

/**
 * Prints the last executed command names.
 */
public final class HistoryCommand extends AbstractCommand {
    private final HistoryManager historyManager;
    private final PrintStream out;

    public HistoryCommand(HistoryManager historyManager, PrintStream out) {
        super("history", "print last 11 executed commands");
        this.historyManager = historyManager;
        this.out = out;
    }

    @Override
    public boolean execute(String argument) throws CommandException {
        requireNoArgument(argument);

        List<String> history = historyManager.getHistory();
        if (history.isEmpty()) {
            out.println("History is empty.");
            return true;
        }

        for (String commandName : history) {
            out.println(commandName);
        }
        return true;
    }
}
