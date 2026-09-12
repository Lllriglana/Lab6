package itmo.lab6.exceptions;

/**
 * Signals invalid data in the domain model or user input.
 */
public class ValidationException extends Exception {
    private static final long serialVersionUID = 1L;
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
