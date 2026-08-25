package com.openclassrooms.datashare.exception;

public class InvalidDownloadPasswordException extends RuntimeException {

    public InvalidDownloadPasswordException() {
        super("Invalid download password");
    }
}