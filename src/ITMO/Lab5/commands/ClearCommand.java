package ITMO.Lab5.commands;

import ITMO.Lab5.exceptions.CommandException;
import ITMO.Lab5.managers.CollectionManager;

import java.io.PrintStream;

/**
 * Clears the collection.
 */
public final class ClearCommand extends AbstractCommand {
    private final CollectionManager collectionManager;
    private final PrintStream out;

    public ClearCommand(CollectionManager collectionManager, PrintStream out) {
        super("clear", "clear the collection");
        this.collectionManager = collectionManager;
        this.out = out;
    }

    @Override
    public boolean execute(String argument) throws CommandException {
        requireNoArgument(argument);
        collectionManager.clear();
        out.println("Collection was cleared.");
        return true;
    }
}
