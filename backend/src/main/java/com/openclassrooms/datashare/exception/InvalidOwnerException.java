package com.openclassrooms.datashare.exception;

public class InvalidOwnerException extends RuntimeException {
    public InvalidOwnerException(String message) {
        super(message);
    }
}
