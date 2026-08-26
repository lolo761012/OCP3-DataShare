package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.entities.StoredFile;
import com.openclassrooms.datashare.entities.StoredFileHistory;
import com.openclassrooms.datashare.exception.FileStorageException;
import com.openclassrooms.datashare.handler.StoredFileNotFoundException;
import com.openclassrooms.datashare.repository.StoredFileHistoryRepository;
import com.openclassrooms.datashare.repository.StoredFileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class StoredFilePurgeService {

    private final StoredFileRepository storedFileRepository;
    private final StoredFileHistoryRepository storedFileHistoryRepository;

    public StoredFilePurgeService(
            StoredFileRepository storedFileRepository,
            StoredFileHistoryRepository storedFileHistoryRepository) {
        this.storedFileRepository = storedFileRepository;
        this.storedFileHistoryRepository = storedFileHistoryRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void purgeExpiredFile(Long fileId) {

        StoredFile file = storedFileRepository.findById(fileId)
                .orElseThrow(() -> new StoredFileNotFoundException(fileId));

        StoredFileHistory history = new StoredFileHistory();
        history.setOwner(file.getOwner());
        history.setFileName(file.getFileName());
        history.setSize(file.getSize());
        history.setDownloadToken(file.getDownloadToken());
        history.setUploadedAt(file.getUploadedAt());
        history.setExpiresAt(file.getExpiresAt());

        storedFileHistoryRepository.saveAndFlush(history);

        try {
            Files.deleteIfExists(Path.of(file.getStoragePath()));
        } catch (IOException e) {
            throw new FileStorageException(
                    "Unable to delete stored file", e);
        }

        storedFileRepository.delete(file);
    }
}
