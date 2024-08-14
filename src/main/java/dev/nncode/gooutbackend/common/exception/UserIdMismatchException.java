package dev.nncode.gooutbackend.common.exception;

public class UserIdMismatchException extends RuntimeException {

    public UserIdMismatchException() {
        super();
    }

    public UserIdMismatchException(String message) {
        super(message);
    }

}
