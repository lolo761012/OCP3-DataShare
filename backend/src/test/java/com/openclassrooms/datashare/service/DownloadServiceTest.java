package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.dto.DownloadInfoDTO;
import com.openclassrooms.datashare.entities.StoredFile;
import com.openclassrooms.datashare.exception.FileStorageException;
import com.openclassrooms.datashare.exception.InvalidDownloadPasswordException;
import com.openclassrooms.datashare.exception.StoredFileExpiredException;
import com.openclassrooms.datashare.handler.StoredFileNotFoundException;
import com.openclassrooms.datashare.mapper.DownloadInfoMapper;
import com.openclassrooms.datashare.repository.StoredFileHistoryRepository;
import com.openclassrooms.datashare.repository.StoredFileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.file.Files;
import java.nio.file.Path;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.springframework.core.io.Resource;


@ExtendWith(MockitoExtension.class)

public class DownloadServiceTest {

    @Mock
    private DownloadInfoMapper downloadInfoMapper;

    @Mock
    private StoredFileRepository storedFileRepository;

    @Mock
    private StoredFileHistoryRepository storedFileHistoryRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DownloadService downloadService;

    @TempDir
    Path tempDir;

    @Test
    void should_return_file_detail() {

        // GIVEN
        // données
        // comportement des mocks
        StoredFile storedFile = new StoredFile();
        storedFile.setExpiresAt(LocalDateTime.now().plusDays(1));
        DownloadInfoDTO expectedDto = new DownloadInfoDTO();
        String token = "valid-token";


        when(storedFileRepository.findByDownloadToken(token))
                .thenReturn(Optional.of(storedFile));

        when(downloadInfoMapper.toDto(storedFile))
                .thenReturn(expectedDto);


        // WHEN
        DownloadInfoDTO result =  downloadService.getStoredFileByToken(token);

        // THEN
        assertThat(result).isSameAs(expectedDto);
        verify(storedFileRepository).findByDownloadToken(token);
        verify(downloadInfoMapper).toDto(storedFile);
    }

    @Test
    void should_return_expired_token() {

        // GIVEN
        // données
        // comportement des mocks
        StoredFile storedFile = new StoredFile();
        storedFile.setExpiresAt(LocalDateTime.now().minusDays(1));
        String token = "expired-token";


        when(storedFileRepository.findByDownloadToken(token))
                .thenReturn(Optional.of(storedFile));




        // WHEN / THEN
        assertThatThrownBy(() ->
                downloadService.getStoredFileByToken(token)
        ).isInstanceOf(StoredFileExpiredException.class);
    }

    @Test
    void should_return_expired_token_from_history() {

        // GIVEN
        String token = "expired-history-token";

        when(storedFileRepository.findByDownloadToken(token))
                .thenReturn(Optional.empty());

        when(storedFileHistoryRepository.existsByDownloadToken(token))
                .thenReturn(true);

        // WHEN / THEN
        assertThatThrownBy(() ->
                downloadService.getStoredFileByToken(token)
        ).isInstanceOf(StoredFileExpiredException.class);
    }

    @Test
    void should_return_unknown_token() {

        // GIVEN
        String token = "wrong-token";

        when(storedFileRepository.findByDownloadToken(token))
                .thenReturn(Optional.empty());

        when(storedFileHistoryRepository.existsByDownloadToken(token))
                .thenReturn(false);

        // WHEN / THEN
        assertThatThrownBy(() ->
                downloadService.getStoredFileByToken(token)
        ).isInstanceOf(StoredFileNotFoundException.class);
    }

    @Test
    void should_download_file_without_password() throws Exception {

        // GIVEN
        String token = "valid-token";

        Path tempFile = tempDir.resolve("test.txt");
        Files.writeString(tempFile, "test");

        StoredFile storedFile = new StoredFile();
        storedFile.setExpiresAt(LocalDateTime.now().plusDays(1));
        storedFile.setStoragePath(tempFile.toString());
        storedFile.setPasswordHash(null);

        when(storedFileRepository.findByDownloadToken(token))
                .thenReturn(Optional.of(storedFile));

        // WHEN
        Resource result =
                downloadService.downloadFile(token, null);

        // THEN
        assertThat(result.exists()).isTrue();
        assertThat(result.isReadable()).isTrue();
    }

    @Test
    void should_download_file_with_correct_password() throws Exception {

        // GIVEN
        String token = "valid-token";
        String password = "secret123";
        String passwordHash = "hashed-password";

        Path tempFile = tempDir.resolve("test.txt");
        Files.writeString(tempFile, "test");

        StoredFile storedFile = new StoredFile();
        storedFile.setExpiresAt(LocalDateTime.now().plusDays(1));
        storedFile.setStoragePath(tempFile.toString());
        storedFile.setPasswordHash(passwordHash);

        when(storedFileRepository.findByDownloadToken(token))
                .thenReturn(Optional.of(storedFile));

        when(passwordEncoder.matches(password, passwordHash))
                .thenReturn(true);

        // WHEN
        Resource result =
                downloadService.downloadFile(token, password);

        // THEN
        assertThat(result.exists()).isTrue();
        assertThat(result.isReadable()).isTrue();
    }

    @Test
    void should_reject_missing_password() {

        // GIVEN
        String token = "valid-token";

        StoredFile storedFile = new StoredFile();
        storedFile.setExpiresAt(LocalDateTime.now().plusDays(1));
        storedFile.setPasswordHash("hashed-password");

        when(storedFileRepository.findByDownloadToken(token))
                .thenReturn(Optional.of(storedFile));

        // WHEN / THEN
        assertThatThrownBy(() ->
                downloadService.downloadFile(token, null)
        ).isInstanceOf(InvalidDownloadPasswordException.class);
    }


    @Test
    void should_reject_wrong_password() {

        // GIVEN
        String token = "valid-token";
        String password = "wrong-password";
        String passwordHash = "hashed-password";

        StoredFile storedFile = new StoredFile();
        storedFile.setExpiresAt(LocalDateTime.now().plusDays(1));
        storedFile.setPasswordHash(passwordHash);

        when(storedFileRepository.findByDownloadToken(token))
                .thenReturn(Optional.of(storedFile));

        when(passwordEncoder.matches(password, passwordHash))
                .thenReturn(false);

        // WHEN / THEN
        assertThatThrownBy(() ->
                downloadService.downloadFile(token, password)
        ).isInstanceOf(InvalidDownloadPasswordException.class);
    }


    @Test
    void should_reject_missing_physical_file() {

        // GIVEN
        String token = "valid-token";

        Path missingFile = tempDir.resolve("missing.txt");

        StoredFile storedFile = new StoredFile();
        storedFile.setExpiresAt(LocalDateTime.now().plusDays(1));
        storedFile.setStoragePath(missingFile.toString());
        storedFile.setPasswordHash(null);

        when(storedFileRepository.findByDownloadToken(token))
                .thenReturn(Optional.of(storedFile));

        // WHEN / THEN
        assertThatThrownBy(() ->
                downloadService.downloadFile(token, null)
        ).isInstanceOf(FileStorageException.class);
    }
}