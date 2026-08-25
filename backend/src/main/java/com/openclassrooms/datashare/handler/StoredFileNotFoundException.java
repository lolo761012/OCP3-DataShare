package com.openclassrooms.datashare.handler;

public class StoredFileNotFoundException extends RuntimeException {

    public StoredFileNotFoundException(Long id) {
        super("Le fichier avec l'id " + id + " est introuvable");
    }

    public StoredFileNotFoundException(String downloadToken) {
        super("File not found for download token: " + downloadToken);
    }
}