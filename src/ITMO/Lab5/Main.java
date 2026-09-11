package ITMO.Lab5;

import ITMO.Lab5.commands.AddCommand;
import ITMO.Lab5.commands.AddIfMinCommand;
import ITMO.Lab5.commands.ClearCommand;
import ITMO.Lab5.commands.Command;
import ITMO.Lab5.commands.CountLessThanWeaponTypeCommand;
import ITMO.Lab5.commands.ExecuteScriptCommand;
import ITMO.Lab5.commands.ExitCommand;
import ITMO.Lab5.commands.FilterByWeaponTypeCommand;
import ITMO.Lab5.commands.HelpCommand;
import ITMO.Lab5.commands.HistoryCommand;
import ITMO.Lab5.commands.InfoCommand;
import ITMO.Lab5.commands.PrintUniqueCategoryCommand;
import ITMO.Lab5.commands.RemoveByIdCommand;
import ITMO.Lab5.commands.SaveCommand;
import ITMO.Lab5.commands.ShowCommand;
import ITMO.Lab5.commands.ShuffleCommand;
import ITMO.Lab5.commands.UpdateCommand;
import ITMO.Lab5.exceptions.FileOperationException;
import ITMO.Lab5.exceptions.InputException;
import ITMO.Lab5.exceptions.ValidationException;
import ITMO.Lab5.input.InputManager;
import ITMO.Lab5.input.SpaceMarineInputReader;
import ITMO.Lab5.managers.CollectionManager;
import ITMO.Lab5.managers.CommandManager;
import ITMO.Lab5.managers.FileManager;
import ITMO.Lab5.managers.HistoryManager;
import ITMO.Lab5.model.SpaceMarine;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Entry point for Lab5 collection manager application.
 */
public class Main {
	private Main() {
	}

	public static void main(String[] args) {
		PrintStream out = System.out;

		if (args == null || args.length != 1 || args[0] == null || args[0].trim().isEmpty()) {
			out.println("Usage: java ITMO.Lab5.Main <json_file_path>");
			return;
		}

		Path filePath = Paths.get(args[0]);
		CollectionManager collectionManager = new CollectionManager();
		HistoryManager historyManager = new HistoryManager(11);
		FileManager fileManager = new FileManager(filePath, out);

		loadInitialCollection(collectionManager, fileManager, out);

		try (InputManager inputManager = new InputManager(System.in, out)) {
			SpaceMarineInputReader marineInputReader = new SpaceMarineInputReader(inputManager, out);
			CommandManager commandManager = new CommandManager(historyManager, out);

			registerCommands(commandManager, collectionManager, fileManager, inputManager, marineInputReader, out);

			out.println("Collection manager is ready. Type 'help' for command list.");
			runLoop(inputManager, commandManager, out);
		}
	}

	private static void loadInitialCollection(CollectionManager collectionManager, FileManager fileManager, PrintStream out) {
		try {
			List<SpaceMarine> loaded = fileManager.load();
			collectionManager.replaceAll(loaded);
			out.println("Loaded elements: " + loaded.size());
		} catch (FileOperationException e) {
			out.println("Load error: " + e.getMessage());
			out.println("Starting with empty collection.");
		} catch (ValidationException e) {
			out.println("Validation error in loaded data: " + e.getMessage());
			out.println("Starting with empty collection.");
		}
	}

	private static void runLoop(InputManager inputManager, CommandManager commandManager, PrintStream out) {
		while (true) {
			String line;
			try {
				line = inputManager.readCommandLine();
			} catch (InputException e) {
				out.println("Input error: " + e.getMessage());
				continue;
			}

			if (line == null) {
				out.println("Input stream closed. Program terminated.");
				return;
			}

			boolean shouldContinue = commandManager.executeLine(line);
			if (!shouldContinue) {
				return;
			}
		}
	}

	private static void registerCommands(
			CommandManager commandManager,
			CollectionManager collectionManager,
			FileManager fileManager,
			InputManager inputManager,
			SpaceMarineInputReader marineInputReader,
			PrintStream out
	) {
		Command[] commands = new Command[]{
				new HelpCommand(commandManager, out),
				new InfoCommand(collectionManager, out),
				new ShowCommand(collectionManager, out),
				new AddCommand(collectionManager, marineInputReader, out),
				new UpdateCommand(collectionManager, marineInputReader, out),
				new RemoveByIdCommand(collectionManager, out),
				new ClearCommand(collectionManager, out),
				new SaveCommand(collectionManager, fileManager, out),
				new ExecuteScriptCommand(inputManager, out),
				new ExitCommand(out),
				new AddIfMinCommand(collectionManager, marineInputReader, out),
				new ShuffleCommand(collectionManager, out),
				new HistoryCommand(commandManager.getHistoryManager(), out),
				new CountLessThanWeaponTypeCommand(collectionManager, out),
				new FilterByWeaponTypeCommand(collectionManager, out),
				new PrintUniqueCategoryCommand(collectionManager, out)
		};

		for (Command command : commands) {
			commandManager.register(command);
		}
	}
}
