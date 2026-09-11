package ITMO.Lab5.commands;

import ITMO.Lab5.exceptions.CommandException;
import ITMO.Lab5.managers.CollectionManager;

import java.io.PrintStream;

/**
 * Prints collection metadata.
 */
public final class InfoCommand extends AbstractCommand {
    private final CollectionManager collectionManager;
    private final PrintStream out;

    public InfoCommand(CollectionManager collectionManager, PrintStream out) {
        super("info", "print information about the collection");
        this.collectionManager = collectionManager;
        this.out = out;
    }

    @Override
    public boolean execute(String argument) throws CommandException {
        requireNoArgument(argument);
        out.println("Collection type: " + collectionManager.getCollectionType());
        out.println("Initialization date: " + collectionManager.getInitializationDate());
        out.println("Elements count: " + collectionManager.size());
        return true;
    }
}
