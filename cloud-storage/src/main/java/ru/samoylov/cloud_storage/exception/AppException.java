package ru.samoylov.cloud_storage.exception;

public class AppException extends RuntimeException {
    private final String errorCode;

    private final String userMessage;

    public AppException(String errorCode, String userMessage) {
        super(userMessage);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }

    public AppException(String errorCode, String userMessage, Throwable cause) {
        super(userMessage, cause);
        this.errorCode=errorCode;
        this.userMessage=userMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getUserMessage() {
        return userMessage;
    }
}
