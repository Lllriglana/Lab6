package ITMO.Lab5.commands;

import ITMO.Lab5.exceptions.CommandException;
import ITMO.Lab5.exceptions.InputException;
import ITMO.Lab5.input.InputManager;

import java.io.PrintStream;

/**
 * Starts execution of commands from the specified script file.
 */
public final class ExecuteScriptCommand extends AbstractCommand {
    private final InputManager inputManager;
    private final PrintStream out;

    public ExecuteScriptCommand(InputManager inputManager, PrintStream out) {
        super("execute_script", "execute commands from script file");
        this.inputManager = inputManager;
        this.out = out;
    }

    @Override
    public boolean execute(String argument) throws CommandException {
        String fileName = requireArgument(argument);

        try {
            inputManager.pushScript(fileName);
            out.println("Script added to execution stack: " + fileName);
            return true;
        } catch (InputException e) {
            throw new CommandException("Failed to execute script: " + e.getMessage(), e);
        }
    }
}
