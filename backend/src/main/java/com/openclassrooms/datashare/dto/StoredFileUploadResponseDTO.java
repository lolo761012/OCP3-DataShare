package com.openclassrooms.datashare.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoredFileUploadResponseDTO {

    private Long id;
    private String fileName;
    private Long size;
    private String downloadToken;
    private LocalDateTime expiresAt;
}