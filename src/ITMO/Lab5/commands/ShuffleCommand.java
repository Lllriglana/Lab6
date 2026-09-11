package ITMO.Lab5.commands;

import ITMO.Lab5.exceptions.CommandException;
import ITMO.Lab5.managers.CollectionManager;

import java.io.PrintStream;

/**
 * Randomly shuffles collection elements.
 */
public final class ShuffleCommand extends AbstractCommand {
    private final CollectionManager collectionManager;
    private final PrintStream out;

    public ShuffleCommand(CollectionManager collectionManager, PrintStream out) {
        super("shuffle", "shuffle collection elements randomly");
        this.collectionManager = collectionManager;
        this.out = out;
    }

    @Override
    public boolean execute(String argument) throws CommandException {
        requireNoArgument(argument);
        collectionManager.shuffle();
        out.println("Collection was shuffled.");
        return true;
    }
}
