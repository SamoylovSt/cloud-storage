package ru.samoylov.cloud_storage.exception;

import org.springframework.http.HttpStatus;

public class ObjectNotFountException extends AppException {
    private final HttpStatus httpStatus;
    private final String userMessage;


    public ObjectNotFountException(HttpStatus httpStatus, String userMessage) {
        super("OBJECT_NOT_FOUND", userMessage);
        this.httpStatus = httpStatus;
        this.userMessage = userMessage;
    }


    public ObjectNotFountException(HttpStatus httpStatus, Throwable cause, String userMessage) {
        super("OBJECT_NOT_FOUND", userMessage, cause);
        this.httpStatus = httpStatus;
        this.userMessage = userMessage;
    }


    public ObjectNotFountException(String errorCode, HttpStatus httpStatus, String userMessage) {
        super(errorCode, userMessage);
        this.httpStatus = httpStatus;
        this.userMessage = userMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getUserMessage() {
        return userMessage;
    }
}
