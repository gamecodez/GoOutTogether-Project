package dev.nncode.gooutbackend.common.exception;

public class RefreshTokenExpiredException extends RuntimeException{

    public RefreshTokenExpiredException() {
        super();
    }

    public RefreshTokenExpiredException(String message) {
        super(message);
    }
}
