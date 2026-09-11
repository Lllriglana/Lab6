package ITMO.Lab5.commands;

import ITMO.Lab5.exceptions.CommandException;
import ITMO.Lab5.managers.CollectionManager;

import java.io.PrintStream;

/**
 * Removes an element from collection by id.
 */
public final class RemoveByIdCommand extends AbstractCommand {
    private final CollectionManager collectionManager;
    private final PrintStream out;

    public RemoveByIdCommand(CollectionManager collectionManager, PrintStream out) {
        super("remove_by_id", "remove element by id");
        this.collectionManager = collectionManager;
        this.out = out;
    }

    @Override
    public boolean execute(String argument) throws CommandException {
        int id = parsePositiveInt(argument, "id");

        boolean removed = collectionManager.removeById(id);
        if (removed) {
            out.println("Element with id=" + id + " was removed.");
        } else {
            out.println("Element with id=" + id + " was not found.");
        }
        return true;
    }
}
