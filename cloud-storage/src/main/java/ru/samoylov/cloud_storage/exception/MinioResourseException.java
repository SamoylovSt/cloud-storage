package ru.samoylov.cloud_storage.exception;

import org.springframework.http.HttpStatus;

public class MinioResourseException extends AppException{
    private final HttpStatus httpStatus;
    private final String userMessage;

    public MinioResourseException (HttpStatus httpStatus, Throwable cause, String userMessage) {
        super("OBJECT_EXCEPTION", userMessage, cause);
        this.httpStatus = httpStatus;
        this.userMessage = userMessage;
    }

}
