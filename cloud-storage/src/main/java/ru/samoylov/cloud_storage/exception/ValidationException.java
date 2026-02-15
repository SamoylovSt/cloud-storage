package ru.samoylov.cloud_storage.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends RuntimeException {

    private final int errorCode;
    private final String message;


    public ValidationException(String message, int errorCode) {
        super(message);
        this.message = message;
        this.errorCode = errorCode;
    }

    public ValidationException(String message, HttpStatus status) {
        super(message);
        this.message = message;
        this.errorCode = status.value();
    }

    public int getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
