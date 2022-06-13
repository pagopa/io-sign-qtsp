package it.pagopa.firmaconio.firma_qtsp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UploadedFileException extends RuntimeException {

    public UploadedFileException(String message) {
        super(message);
    }

    public UploadedFileException(String message, Throwable cause) {
        super(message, cause);
    }
}