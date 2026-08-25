package com.openclassrooms.datashare.repository;

import com.openclassrooms.datashare.entities.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoredFileRepository  extends JpaRepository<StoredFile, Long>  {

    Optional<StoredFile> findByDownloadToken(String downloadToken);
}
