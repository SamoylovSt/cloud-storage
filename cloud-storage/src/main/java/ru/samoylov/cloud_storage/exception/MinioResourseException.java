package ru.samoylov.cloud_storage.exception;

import org.springframework.http.HttpStatus;

public class MinioResourseException extends AppException{
    private final HttpStatus httpStatus;
    private final String userMessage;


    public MinioResourseException (HttpStatus httpStatus, String userMessage) {
        super("OBJECT_EXCEPTION", userMessage);
        this.httpStatus = httpStatus;
        this.userMessage = userMessage;
    }


    public MinioResourseException (HttpStatus httpStatus, Throwable cause, String userMessage) {
        super("OBJECT_EXCEPTION", userMessage, cause);
        this.httpStatus = httpStatus;
        this.userMessage = userMessage;
    }


    public MinioResourseException (String errorCode, HttpStatus httpStatus, String userMessage) {
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
