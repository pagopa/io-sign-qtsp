package it.pagopa.firmaconio.firma_qtsp.exception;

public class QtspException extends RuntimeException {

    public QtspException(String message) {
        super(message);
    }

    public QtspException(String message, Throwable cause) {
        super(message, cause);
    }
}
