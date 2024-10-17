package com.example.crypto.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CryptoNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public String handleCryptoNotFoundException(CryptoNotFoundException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(NoCryptoDataAvailableException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public String handleNoCryptoDataAvailableException(NoCryptoDataAvailableException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(FileLoadingException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public String handleFileLoadingException(FileLoadingException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(CryptoDataProcessingException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String handleCryptoDataProcessingException(CryptoDataProcessingException ex) {
        return "Error processing crypto data: " + ex.getMessage();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String handleIllegalArgumentException(IllegalArgumentException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String handleGeneralException(Exception ex) {
        return "An unexpected error occurred: " + ex.getMessage();
    }
}
