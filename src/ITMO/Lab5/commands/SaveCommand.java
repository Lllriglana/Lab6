package ITMO.Lab5.commands;

import ITMO.Lab5.exceptions.CommandException;
import ITMO.Lab5.exceptions.FileOperationException;
import ITMO.Lab5.managers.CollectionManager;
import ITMO.Lab5.managers.FileManager;

import java.io.PrintStream;

/**
 * Saves collection to JSON file.
 */
public final class SaveCommand extends AbstractCommand {
    private final CollectionManager collectionManager;
    private final FileManager fileManager;
    private final PrintStream out;

    public SaveCommand(CollectionManager collectionManager, FileManager fileManager, PrintStream out) {
        super("save", "save the collection to file");
        this.collectionManager = collectionManager;
        this.fileManager = fileManager;
        this.out = out;
    }

    @Override
    public boolean execute(String argument) throws CommandException {
        requireNoArgument(argument);
        try {
            fileManager.save(collectionManager.getAll());
            out.println("Collection was saved to: " + fileManager.getFilePath());
            return true;
        } catch (FileOperationException e) {
            throw new CommandException("Failed to save collection: " + e.getMessage(), e);
        }
    }
}
