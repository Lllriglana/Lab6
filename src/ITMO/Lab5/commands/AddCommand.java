package ITMO.Lab5.commands;

import ITMO.Lab5.exceptions.CommandException;
import ITMO.Lab5.exceptions.InputException;
import ITMO.Lab5.exceptions.ValidationException;
import ITMO.Lab5.input.SpaceMarineInputReader;
import ITMO.Lab5.managers.CollectionManager;
import ITMO.Lab5.model.SpaceMarine;

import java.io.PrintStream;
import java.time.LocalDate;

/**
 * Adds a new element to the collection.
 */
public final class AddCommand extends AbstractCommand {
    private final CollectionManager collectionManager;
    private final SpaceMarineInputReader inputReader;
    private final PrintStream out;

    public AddCommand(CollectionManager collectionManager, SpaceMarineInputReader inputReader, PrintStream out) {
        super("add", "add a new element to the collection");
        this.collectionManager = collectionManager;
        this.inputReader = inputReader;
        this.out = out;
    }

    @Override
    public boolean execute(String argument) throws CommandException {
        requireNoArgument(argument);

        int id = collectionManager.generateNextId();
        LocalDate creationDate = LocalDate.now();

        try {
            SpaceMarine marine = inputReader.readSpaceMarine(id, creationDate);
            collectionManager.add(marine);
            out.println("Element was added with id=" + id);
            return true;
        } catch (InputException | ValidationException e) {
            throw new CommandException("Failed to add element: " + e.getMessage(), e);
        }
    }
}
