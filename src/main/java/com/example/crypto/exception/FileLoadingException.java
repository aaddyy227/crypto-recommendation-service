package com.example.crypto.exception;

public class FileLoadingException extends RuntimeException {
    public FileLoadingException(String message) {
        super(message);
    }

    public FileLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
