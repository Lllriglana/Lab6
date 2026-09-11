package ITMO.Lab5.commands;

import ITMO.Lab5.exceptions.CommandException;
import ITMO.Lab5.exceptions.InputException;
import ITMO.Lab5.exceptions.ValidationException;
import ITMO.Lab5.input.SpaceMarineInputReader;
import ITMO.Lab5.managers.CollectionManager;
import ITMO.Lab5.model.SpaceMarine;

import java.io.PrintStream;

/**
 * Updates an existing collection element by id.
 */
public final class UpdateCommand extends AbstractCommand {
    private final CollectionManager collectionManager;
    private final SpaceMarineInputReader inputReader;
    private final PrintStream out;

    public UpdateCommand(CollectionManager collectionManager, SpaceMarineInputReader inputReader, PrintStream out) {
        super("update", "update element by id: update id");
        this.collectionManager = collectionManager;
        this.inputReader = inputReader;
        this.out = out;
    }

    @Override
    public boolean execute(String argument) throws CommandException {
        int id = parsePositiveInt(argument, "id");

        SpaceMarine existing = collectionManager.findById(id);
        if (existing == null) {
            out.println("Element with id=" + id + " was not found.");
            return true;
        }

        try {
            SpaceMarine updated = inputReader.readSpaceMarine(existing.getId(), existing.getCreationDate());
            boolean changed = collectionManager.updateById(id, updated);
            if (changed) {
                out.println("Element with id=" + id + " was updated.");
            } else {
                out.println("Element with id=" + id + " was not found.");
            }
            return true;
        } catch (InputException | ValidationException e) {
            throw new CommandException("Failed to update element: " + e.getMessage(), e);
        }
    }
}
