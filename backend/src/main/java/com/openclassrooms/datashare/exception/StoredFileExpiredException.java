package com.openclassrooms.datashare.exception;

public class StoredFileExpiredException extends RuntimeException {

    public StoredFileExpiredException(String downloadToken) {
        super("File expired for download token: " + downloadToken);
    }
}
