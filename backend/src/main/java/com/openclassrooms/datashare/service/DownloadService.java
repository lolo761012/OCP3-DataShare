package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.dto.DownloadInfoDTO;
import com.openclassrooms.datashare.entities.StoredFile;
import com.openclassrooms.datashare.exception.FileStorageException;
import com.openclassrooms.datashare.exception.InvalidDownloadPasswordException;
import com.openclassrooms.datashare.exception.StoredFileExpiredException;
import com.openclassrooms.datashare.handler.StoredFileNotFoundException;
import com.openclassrooms.datashare.mapper.DownloadInfoMapper;
import com.openclassrooms.datashare.repository.StoredFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DownloadService {

    private final StoredFileRepository storedFileRepository;
    private final DownloadInfoMapper downloadInfoMapper;
    private final PasswordEncoder passwordEncoder;

    public DownloadInfoDTO getStoredFileByToken(String downloadToken) {
        StoredFile storedFile = getStoredFileEntityByToken(downloadToken);
        return downloadInfoMapper.toDto(storedFile);
    }

    public Resource downloadFile(String downloadToken, String password) {
        StoredFile storedFile = getStoredFileEntityByToken(downloadToken);

        if (storedFile.getPasswordHash() != null) {
            if (password == null ||
                    !passwordEncoder.matches(password, storedFile.getPasswordHash())) {
                throw new InvalidDownloadPasswordException();
            }
        }
        Path path = Paths.get(storedFile.getStoragePath()).normalize();
        Resource resource = new FileSystemResource(path);

        if (!resource.exists() || !resource.isReadable()) {
            throw new FileStorageException(
                    "Stored file is missing or unreadable",
                    null
            );
        }

        return resource;
    }


    private StoredFile getStoredFileEntityByToken(String downloadToken) {
        StoredFile storedFile = storedFileRepository
                .findByDownloadToken(downloadToken)
                .orElseThrow(() ->
                        new StoredFileNotFoundException(downloadToken));

        if (storedFile.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new StoredFileExpiredException(downloadToken);
        }

        return storedFile;
    }


}

