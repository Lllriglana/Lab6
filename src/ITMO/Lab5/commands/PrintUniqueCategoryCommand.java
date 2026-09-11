package ITMO.Lab5.commands;

import ITMO.Lab5.exceptions.CommandException;
import ITMO.Lab5.managers.CollectionManager;
import ITMO.Lab5.model.AstartesCategory;

import java.io.PrintStream;
import java.util.Set;

/**
 * Prints unique category values in the collection.
 */
public final class PrintUniqueCategoryCommand extends AbstractCommand {
    private final CollectionManager collectionManager;
    private final PrintStream out;

    public PrintUniqueCategoryCommand(CollectionManager collectionManager, PrintStream out) {
        super("print_unique_category", "print unique category values");
        this.collectionManager = collectionManager;
        this.out = out;
    }

    @Override
    public boolean execute(String argument) throws CommandException {
        requireNoArgument(argument);

        Set<AstartesCategory> categories = collectionManager.getUniqueCategories();
        if (categories.isEmpty()) {
            out.println("No categories in collection.");
            return true;
        }

        for (AstartesCategory category : categories) {
            out.println(String.valueOf(category));
        }
        return true;
    }
}
