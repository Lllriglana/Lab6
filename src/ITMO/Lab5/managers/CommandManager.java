package ITMO.Lab5.managers;

import ITMO.Lab5.commands.Command;
import ITMO.Lab5.exceptions.CommandException;
import ITMO.Lab5.util.CommandLineParser;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registers and executes commands by user input.
 */
public final class CommandManager {
    private final Map<String, Command> commands;
    private final HistoryManager historyManager;
    private final PrintStream out;

    public CommandManager(HistoryManager historyManager, PrintStream out) {
        this.commands = new LinkedHashMap<String, Command>();
        this.historyManager = historyManager;
        this.out = out;
    }

    public void register(Command command) {
        commands.put(command.getName(), command);
    }

    public Collection<Command> getCommands() {
        return Collections.unmodifiableCollection(commands.values());
    }

    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    public boolean executeLine(String line) {
        CommandLineParser.ParsedCommand parsed = CommandLineParser.parse(line);
        if (parsed.getName().isEmpty()) {
            return true;
        }

        Command command = commands.get(parsed.getName());
        if (command == null) {
            out.println("Unknown command: " + parsed.getName());
            out.println("Type 'help' to see available commands.");
            return true;
        }

        historyManager.add(command.getName());

        try {
            return command.execute(parsed.getArgument());
        } catch (CommandException e) {
            out.println("Command error: " + e.getMessage());
            return true;
        } catch (RuntimeException e) {
            out.println("Unexpected command error: " + e.getMessage());
            return true;
        }
    }
}
