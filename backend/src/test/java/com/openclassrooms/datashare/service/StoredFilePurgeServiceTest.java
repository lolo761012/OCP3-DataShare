package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.entities.StoredFile;
import com.openclassrooms.datashare.entities.StoredFileHistory;
import com.openclassrooms.datashare.repository.StoredFileHistoryRepository;
import com.openclassrooms.datashare.repository.StoredFileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;


@ExtendWith(MockitoExtension.class)
public class StoredFilePurgeServiceTest {

    @Mock
    private StoredFileRepository storedFileRepository;

    @Mock
    private StoredFileHistoryRepository storedFileHistoryRepository;

    @InjectMocks
    private StoredFilePurgeService storedFilePurgeService;

    @TempDir
    Path tempDir;

    @Test
    void should_purge_expired_file() throws Exception {

        // GIVEN
        Long fileId = 1L;

        Path tempFile = tempDir.resolve("expired.txt");
        Files.writeString(tempFile, "test");

        StoredFile storedFile = new StoredFile();
        storedFile.setStoragePath(tempFile.toString());

        when(storedFileRepository.findById(fileId))
                .thenReturn(Optional.of(storedFile));

        // WHEN
        storedFilePurgeService.purgeExpiredFile(fileId);

        // THEN
        verify(storedFileHistoryRepository)
                .saveAndFlush(any(StoredFileHistory.class));

        verify(storedFileRepository)
                .delete(storedFile);

        assertThat(Files.exists(tempFile)).isFalse();
    }
}
