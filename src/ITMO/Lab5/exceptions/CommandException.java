package ITMO.Lab5.exceptions;

/**
 * Signals command parsing or execution failure.
 */
public class CommandException extends Exception {
    private static final long serialVersionUID = 1L;
    public CommandException(String message) {
        super(message);
    }

    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
