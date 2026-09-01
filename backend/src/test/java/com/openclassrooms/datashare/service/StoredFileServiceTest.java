package com.openclassrooms.datashare.service;


import com.openclassrooms.datashare.dto.StoredFileListDTO;
import com.openclassrooms.datashare.dto.StoredFileUploadResponseDTO;
import com.openclassrooms.datashare.entities.StoredFile;
import com.openclassrooms.datashare.entities.StoredFileHistory;
import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.repository.StoredFileHistoryRepository;
import com.openclassrooms.datashare.repository.StoredFileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.web.multipart.MultipartFile;
import com.openclassrooms.datashare.exception.FileTooLargeException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoredFileServiceTest {

    @Value("${datashare.storage.path}")
    private String storagePath;

    @Mock
    private StoredFileRepository storedFileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private StoredFileHistoryRepository storedFileHistoryRepository;

    @Mock
    private StoredFilePurgeService storedFilePurgeService;

    @InjectMocks
    private StoredFileService storedFileService;

    @TempDir
    Path tempDir;


    @Test
    void should_upload_file() throws Exception {

        // GIVEN

        ReflectionTestUtils.setField(
                storedFileService,
                "storagePath",
                tempDir.toString()
        );

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "contenu du fichier".getBytes()
        );

        Integer expirationDays = 1;
        String password = null;
        User owner = new User();

        StoredFile savedStoredFile = new StoredFile();
        savedStoredFile.setId(1L);
        savedStoredFile.setFileName("test.txt");
        savedStoredFile.setSize(file.getSize());
        savedStoredFile.setDownloadToken("token-test");
        savedStoredFile.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(storedFileRepository.saveAndFlush(any(StoredFile.class)))
                .thenReturn(savedStoredFile);

        // WHEN
        StoredFileUploadResponseDTO result =
                storedFileService.uploadStoredFile(
                        file,
                        expirationDays,
                        password,
                        owner
                );

        // THEN
        assertThat(result).isNotNull();
        verify(storedFileRepository).saveAndFlush(any(StoredFile.class));
    }

    @Test
    void should_upload_file_with_password() throws Exception {

        // GIVEN

        ReflectionTestUtils.setField(
                storedFileService,
                "storagePath",
                tempDir.toString()
        );

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "contenu du fichier".getBytes()
        );

        Integer expirationDays = 1;
        String password = "azerty";
        String passwordHash = "hashed-password";
        User owner = new User();

        StoredFile savedStoredFile = new StoredFile();
        savedStoredFile.setId(1L);
        savedStoredFile.setFileName("test.txt");
        savedStoredFile.setSize(file.getSize());
        savedStoredFile.setDownloadToken("token-test");
        savedStoredFile.setPasswordHash(passwordHash);
        savedStoredFile.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(storedFileRepository.saveAndFlush(any(StoredFile.class)))
                .thenReturn(savedStoredFile);

        when(passwordEncoder.encode(password))
                .thenReturn("hashed-password");

        // WHEN
        StoredFileUploadResponseDTO result =
                storedFileService.uploadStoredFile(
                        file,
                        expirationDays,
                        password,
                        owner
                );

        // THEN
        assertThat(result).isNotNull();
        verify(passwordEncoder).encode(password);
        verify(storedFileRepository).saveAndFlush(any(StoredFile.class));
    }

    @Test
    void should_reject_forbidden_extension() {

        // GIVEN
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "file.exe",
                "application/octet-stream",
                "test".getBytes()
        );

        // WHEN / THEN
        assertThatThrownBy(() ->
                storedFileService.uploadStoredFile(
                        file,
                        1,
                        null,
                        new User()
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_return_active_and_expired_files() {

        // GIVEN
        long userId = 1L;

        StoredFile activeFile = new StoredFile();
        activeFile.setId(1L);
        activeFile.setFileName("active.txt");
        activeFile.setSize(100L);
        activeFile.setUploadedAt(LocalDateTime.now());
        activeFile.setExpiresAt(LocalDateTime.now().plusDays(1));
        activeFile.setDownloadToken("active-token");

        StoredFileHistory expiredFile = new StoredFileHistory();
        expiredFile.setFileName("expired.txt");
        expiredFile.setSize(200L);
        expiredFile.setUploadedAt(LocalDateTime.now().minusDays(2));
        expiredFile.setExpiresAt(LocalDateTime.now().minusDays(1));

        when(storedFileRepository.findAllByOwner_Id(userId))
                .thenReturn(List.of(activeFile));

        when(storedFileHistoryRepository.findAllByOwner_Id(userId))
                .thenReturn(List.of(expiredFile));

        // WHEN
        List<StoredFileListDTO> result =
                storedFileService.getFilesForUser(userId);

        // THEN
        assertThat(result).hasSize(2);

        assertThat(result.get(0).getFileName()).isEqualTo("active.txt");
        assertThat(result.get(0).getStatus()).isEqualTo("VALID");

        assertThat(result.get(1).getFileName()).isEqualTo("expired.txt");
        assertThat(result.get(1).getStatus()).isEqualTo("EXPIRED");
    }

    @Test
    void should_reject_file_too_large() {

        // GIVEN
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L * 1024 * 1024 + 1);

        // WHEN / THEN
        assertThatThrownBy(() ->
                storedFileService.uploadStoredFile(
                        file,
                        1,
                        null,
                        new User()
                )
        ).isInstanceOf(FileTooLargeException.class);
    }
}