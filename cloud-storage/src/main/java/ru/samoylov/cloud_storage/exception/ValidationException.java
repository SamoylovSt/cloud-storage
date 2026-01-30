package ru.samoylov.cloud_storage.exception;

public class ValidationException extends RuntimeException {
    private final int errorCode;

    public ValidationException(String message, Throwable cause, int errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    public ValidationException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
