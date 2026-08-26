package com.openclassrooms.datashare.repository;

import com.openclassrooms.datashare.entities.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StoredFileRepository  extends JpaRepository<StoredFile, Long>  {

    Optional<StoredFile> findByDownloadToken(String downloadToken);
    List<StoredFile> findAllByOwner_Id(Long ownerId);
    List<StoredFile> findAllByExpiresAtBefore(LocalDateTime dateTime);
}
