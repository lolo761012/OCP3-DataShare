package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.dto.StoredFileDTO;
import com.openclassrooms.datashare.dto.StoredFileListDTO;
import com.openclassrooms.datashare.dto.StoredFileUploadResponseDTO;
import com.openclassrooms.datashare.entities.StoredFile;
import com.openclassrooms.datashare.entities.StoredFileHistory;
import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.exception.FileStorageException;
import com.openclassrooms.datashare.exception.FileTooLargeException;
import com.openclassrooms.datashare.exception.InvalidOwnerException;
import com.openclassrooms.datashare.handler.StoredFileNotFoundException;
import com.openclassrooms.datashare.mapper.StoredFileDtoMapper;
import com.openclassrooms.datashare.repository.StoredFileHistoryRepository;
import com.openclassrooms.datashare.repository.StoredFileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class StoredFileService  {

    private static final long MAX_FILE_SIZE_BYTES = 1024L * 1024 * 1024; // 1 Go
    private static final int MIN_EXPIRATION_DAYS = 1;
    private static final int MAX_EXPIRATION_DAYS = 7;
    private static final int DEFAULT_EXPIRATION_DAYS = 7;
    private static final int MIN_DOWNLOAD_PASSWORD_LENGTH = 6;

    private static final Set<String> FORBIDDEN_EXTENSIONS = Set.of(
            "exe", "bat", "cmd", "com", "msi", "ps1", "vbs", "scr"
    );

    private final StoredFileRepository storedFileRepository;
    private final StoredFileDtoMapper storedFileDtoMapper;
    private final PasswordEncoder passwordEncoder;
    private final StoredFileHistoryRepository storedFileHistoryRepository;
    private final StoredFilePurgeService storedFilePurgeService;

    @Value("${datashare.storage.path}")
    private String storagePath;

    public StoredFileUploadResponseDTO uploadStoredFile(
            MultipartFile file,
            Integer expirationDays,
            String password,
            User owner) {

        validateFile(file);
        validateExtension(file.getOriginalFilename());
        int resolvedExpirationDays = resolveExpirationDays(expirationDays);
        validatePassword(password);

        String downloadToken = UUID.randomUUID().toString();
        Path targetPath = resolveTargetPath(downloadToken);

        writeToDisk(file, targetPath);

        StoredFile storedFile = new StoredFile();
        storedFile.setOwner(owner);
        storedFile.setFileName(file.getOriginalFilename());
        storedFile.setSize(file.getSize());
        storedFile.setExpiresAt(LocalDateTime.now().plusDays(resolvedExpirationDays));
        storedFile.setDownloadToken(downloadToken);
        storedFile.setStoragePath(targetPath.toString());
        storedFile.setContentType(resolveContentType(file));

        if (StringUtils.hasText(password)) {
            storedFile.setPasswordHash(passwordEncoder.encode(password));
        }

        StoredFile saved;
        try {
            saved = storedFileRepository.saveAndFlush(storedFile);
        } catch (RuntimeException persistenceException) {
            deleteQuietly(targetPath);
            throw new FileStorageException(
                    "Unable to persist uploaded file metadata", persistenceException);
        }
        log.info("file_upload id={} size={} owner={}",
                saved.getId(),
                saved.getSize(),
                owner != null ? owner.getId() : "anonymous");

        return new StoredFileUploadResponseDTO(
                saved.getId(),
                saved.getFileName(),
                saved.getSize(),
                saved.getDownloadToken(),
                saved.getExpiresAt()
        );
    }

    public List<StoredFileListDTO> getFilesForUser(long userId) {
        List<StoredFile> activeFiles =
                storedFileRepository.findAllByOwner_Id(userId);

        List<StoredFileHistory> expiredFiles =
                storedFileHistoryRepository.findAllByOwner_Id(userId);

        List<StoredFileListDTO> result = new ArrayList<>();
        for (StoredFile file : activeFiles) {
            StoredFileListDTO dto = new StoredFileListDTO();

            dto.setId(file.getId());
            dto.setFileName(file.getFileName());
            dto.setSize(file.getSize());
            dto.setUploadedAt(file.getUploadedAt());
            dto.setExpiresAt(file.getExpiresAt());
            dto.setStatus("VALID");
            dto.setDownloadToken(file.getDownloadToken());

            result.add(dto);
        }
        for (StoredFileHistory file : expiredFiles) {
            StoredFileListDTO dto = new StoredFileListDTO();

            dto.setFileName(file.getFileName());
            dto.setSize(file.getSize());
            dto.setUploadedAt(file.getUploadedAt());
            dto.setExpiresAt(file.getExpiresAt());
            dto.setStatus("EXPIRED");

            result.add(dto);
        }
        result.sort(
                Comparator.comparing(StoredFileListDTO::getUploadedAt).reversed()
        );
        return result;
    }

    public StoredFileDTO getStoredFileByToken(long id) {
        StoredFile storedFile = storedFileRepository.findById(id)
                .orElseThrow(() ->
                        new StoredFileNotFoundException(id));
        return storedFileDtoMapper.toDto(storedFile);
    }

    public void deleteStoredFile(Long fileId, Long ownerId) {
        StoredFile storedFile = storedFileRepository.findById(fileId)
                .orElseThrow(() ->
                        new StoredFileNotFoundException(fileId));

        if (storedFile.getOwner() == null
                || !storedFile.getOwner().getId().equals(ownerId)) {
            throw new InvalidOwnerException("You are not allowed to delete this file");
        }

        try {
            Files.deleteIfExists(Path.of(storedFile.getStoragePath()));
        } catch (IOException e) {
            throw new FileStorageException("Unable to delete stored file", e);
        }


        storedFileRepository.delete(storedFile);


    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file must not be empty");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new FileTooLargeException("Uploaded file exceeds the 1 GB limit");
        }
        if (!StringUtils.hasText(file.getOriginalFilename())) {
            throw new IllegalArgumentException("Uploaded file name must not be empty");
        }
    }



    private void validateExtension(String originalFilename) {
        String extension = extractExtension(originalFilename);
        if (extension != null && FORBIDDEN_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("File type is not allowed");
        }
    }

    private String extractExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return null;
        }
        int lastDot = originalFilename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == originalFilename.length() - 1) {
            return null;
        }
        return originalFilename.substring(lastDot + 1);
    }

    private int resolveExpirationDays(Integer expirationDays) {
        if (expirationDays == null) {
            return DEFAULT_EXPIRATION_DAYS;
        }
        if (expirationDays < MIN_EXPIRATION_DAYS || expirationDays > MAX_EXPIRATION_DAYS) {
            throw new IllegalArgumentException(
                    "expirationDays must be between " + MIN_EXPIRATION_DAYS
                            + " and " + MAX_EXPIRATION_DAYS);
        }
        return expirationDays;
    }

    private void validatePassword(String password) {
        if (StringUtils.hasText(password) && password.length() < MIN_DOWNLOAD_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "password must be at least " + MIN_DOWNLOAD_PASSWORD_LENGTH + " characters long");
        }
    }

    private Path resolveTargetPath(String downloadToken) {
        return Paths.get(storagePath, downloadToken).normalize();
    }

    private String resolveContentType(MultipartFile file) {
        String contentType = file.getContentType();
        return StringUtils.hasText(contentType) ? contentType : "application/octet-stream";
    }

    private void writeToDisk(MultipartFile file, Path targetPath) {
        try {
            Files.createDirectories(targetPath.getParent());
            file.transferTo(targetPath);
        } catch (IOException storageException) {
            deleteQuietly(targetPath);
            throw new FileStorageException(
                    "Unable to store uploaded file",
                    storageException
            );
        }
    }


    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException cleanupException) {
            log.error("Failed to delete orphaned file {} after persistence failure", path, cleanupException);
        }
    }


    public void purgeExpiredFiles() {
        List<StoredFile> expiredFiles =
                storedFileRepository.findAllByExpiresAtBefore(LocalDateTime.now());

        for (StoredFile file : expiredFiles) {
            try {
                storedFilePurgeService.purgeExpiredFile(file.getId());
            } catch (RuntimeException e) {
                log.error("Unable to purge expired file id={}", file.getId(), e);
            }
        }
    }
}