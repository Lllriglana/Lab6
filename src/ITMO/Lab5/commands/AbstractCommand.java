package ITMO.Lab5.commands;

import ITMO.Lab5.exceptions.CommandException;

/**
 * Base class for commands with common argument validation.
 */
public abstract class AbstractCommand implements Command {
    private final String name;
    private final String description;

    protected AbstractCommand(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    protected void requireNoArgument(String argument) throws CommandException {
        if (argument != null && !argument.trim().isEmpty()) {
            throw new CommandException("Command '" + name + "' does not accept arguments");
        }
    }

    protected String requireArgument(String argument) throws CommandException {
        if (argument == null || argument.trim().isEmpty()) {
            throw new CommandException("Command '" + name + "' requires an argument");
        }
        return argument.trim();
    }

    protected int parsePositiveInt(String argument, String fieldName) throws CommandException {
        String value = requireArgument(argument);
        final int parsed;
        try {
            parsed = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new CommandException(fieldName + " must be an integer number");
        }
        if (parsed <= 0) {
            throw new CommandException(fieldName + " must be greater than 0");
        }
        return parsed;
    }

    protected long parsePositiveLong(String argument, String fieldName) throws CommandException {
        String value = requireArgument(argument);
        final long parsed;
        try {
            parsed = Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new CommandException(fieldName + " must be a long number");
        }
        if (parsed <= 0) {
            throw new CommandException(fieldName + " must be greater than 0");
        }
        return parsed;
    }
}
