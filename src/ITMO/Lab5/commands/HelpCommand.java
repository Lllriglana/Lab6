package ITMO.Lab5.commands;

import ITMO.Lab5.exceptions.CommandException;
import ITMO.Lab5.managers.CommandManager;

import java.io.PrintStream;

/**
 * Prints available commands and their descriptions.
 */
public final class HelpCommand extends AbstractCommand {
    private final CommandManager commandManager;
    private final PrintStream out;

    public HelpCommand(CommandManager commandManager, PrintStream out) {
        super("help", "print help for available commands");
        this.commandManager = commandManager;
        this.out = out;
    }

    @Override
    public boolean execute(String argument) throws CommandException {
        requireNoArgument(argument);
        for (Command command : commandManager.getCommands()) {
            out.println(command.getName() + " : " + command.getDescription());
        }
        return true;
    }
}
