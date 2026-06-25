package com.manthau.authservice.application.exception;

public class ApplicationException extends RuntimeException {

    private final ErrorCode errorCode;

    public ApplicationException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public static ApplicationException notFound(String message) {
        return new ApplicationException(message, ErrorCode.NOT_FOUND);
    }

    public static ApplicationException conflict(String message) {
        return new ApplicationException(message, ErrorCode.CONFLICT);
    }

    public static ApplicationException unauthorized(String message) {
        return new ApplicationException(message, ErrorCode.UNAUTHORIZED);
    }

    public static ApplicationException badRequest(String message) {
        return new ApplicationException(message, ErrorCode.BAD_REQUEST);
    }

    public enum ErrorCode {
        NOT_FOUND, CONFLICT, UNAUTHORIZED, BAD_REQUEST
    }
}
