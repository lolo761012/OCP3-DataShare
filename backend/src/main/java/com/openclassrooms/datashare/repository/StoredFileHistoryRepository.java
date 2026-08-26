package com.openclassrooms.datashare.repository;

import com.openclassrooms.datashare.entities.StoredFileHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StoredFileHistoryRepository
        extends JpaRepository<StoredFileHistory, Long> {

    List<StoredFileHistory> findAllByOwner_Id(Long ownerId);
    boolean existsByDownloadToken(String downloadToken);
}