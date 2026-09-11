package ITMO.Lab5.commands;

import ITMO.Lab5.exceptions.CommandException;

import java.io.PrintStream;

/**
 * Terminates command processing without automatic save.
 */
public final class ExitCommand extends AbstractCommand {
    private final PrintStream out;

    public ExitCommand(PrintStream out) {
        super("exit", "exit the program without saving");
        this.out = out;
    }

    @Override
    public boolean execute(String argument) throws CommandException {
        requireNoArgument(argument);
        out.println("Program terminated.");
        return false;
    }
}
