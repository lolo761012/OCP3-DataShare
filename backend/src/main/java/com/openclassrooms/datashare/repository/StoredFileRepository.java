package com.openclassrooms.datashare.repository;

import com.openclassrooms.datashare.entities.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoredFileRepository  extends JpaRepository<StoredFile, Long>  {
}
