package com.openclassrooms.datashare.scheduler;

import com.openclassrooms.datashare.service.StoredFileService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StoredFileCleanupScheduler {

    private final StoredFileService storedFileService;

    public StoredFileCleanupScheduler(StoredFileService storedFileService) {
        this.storedFileService = storedFileService;
    }

    @Scheduled(
            initialDelay = 5000,
            fixedDelay = 3600000
    )
    public void cleanupExpiredFiles() {
        storedFileService.purgeExpiredFiles();
    }
}