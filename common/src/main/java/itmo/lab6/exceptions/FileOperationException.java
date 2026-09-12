package itmo.lab6.exceptions;

/**
 * Signals file read/write problems for collection persistence.
 */
public class FileOperationException extends Exception {
    private static final long serialVersionUID = 1L;
    public FileOperationException(String message) {
        super(message);
    }

    public FileOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
