package com.openclassrooms.datashare.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StoredFileDTO {
    private Long id;
    private String fileName;
    private Long size;
    private LocalDateTime uploadedAt;
    private LocalDateTime expiresAt;
    private String status;
    private boolean passwordProtected;
    private String downloadToken;
}
