package com.openclassrooms.datashare.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DownloadInfoDTO {

    private String fileName;
    private String contentType;
    private Long size;
    private LocalDateTime expiresAt;
    private Boolean passwordProtected;

}
