package com.example.crypto.exception;

public class NoCryptoDataAvailableException extends RuntimeException {
    public NoCryptoDataAvailableException(String message) {
        super(message);
    }
}
