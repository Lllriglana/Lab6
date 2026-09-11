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
 * Adds element only if it is less than current minimum.
 */
public final class AddIfMinCommand extends AbstractCommand {
    private final CollectionManager collectionManager;
    private final SpaceMarineInputReader inputReader;
    private final PrintStream out;

    public AddIfMinCommand(CollectionManager collectionManager, SpaceMarineInputReader inputReader, PrintStream out) {
        super("add_if_min", "add element if it is less than the current minimum");
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
            boolean added = collectionManager.addIfMin(marine);
            if (added) {
                out.println("Element was added (id=" + id + ").");
            } else {
                out.println("Element is not less than current minimum. Nothing added.");
            }
            return true;
        } catch (InputException | ValidationException e) {
            throw new CommandException("Failed to process add_if_min: " + e.getMessage(), e);
        }
    }
}
