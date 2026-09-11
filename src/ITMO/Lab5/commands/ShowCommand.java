package ITMO.Lab5.commands;

import ITMO.Lab5.exceptions.CommandException;
import ITMO.Lab5.managers.CollectionManager;
import ITMO.Lab5.model.SpaceMarine;

import java.io.PrintStream;
import java.util.List;

/**
 * Prints all collection elements.
 */
public final class ShowCommand extends AbstractCommand {
    private final CollectionManager collectionManager;
    private final PrintStream out;

    public ShowCommand(CollectionManager collectionManager, PrintStream out) {
        super("show", "print all elements of the collection");
        this.collectionManager = collectionManager;
        this.out = out;
    }

    @Override
    public boolean execute(String argument) throws CommandException {
        requireNoArgument(argument);

        List<SpaceMarine> marines = collectionManager.getAll();
        if (marines.isEmpty()) {
            out.println("Collection is empty.");
            return true;
        }

        for (SpaceMarine marine : marines) {
            out.println(marine);
        }
        return true;
    }
}
