package id.ac.ui.cs.advprog.groupproject.auth.exception;

public class AuthOperationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AuthOperationException(String message) {
        super(message);
    }

    public AuthOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
