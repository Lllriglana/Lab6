package ITMO.Lab5.commands;

import ITMO.Lab5.exceptions.CommandException;

/**
 * Command contract for interactive command processing.
 */
public interface Command {
    String getName();

    String getDescription();

    boolean execute(String argument) throws CommandException;
}
