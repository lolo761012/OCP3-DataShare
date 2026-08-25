package com.openclassrooms.datashare.controller;

import com.openclassrooms.datashare.dto.DownloadInfoDTO;
import com.openclassrooms.datashare.dto.DownloadRequestDTO;
import com.openclassrooms.datashare.service.DownloadService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;


@RestController
@RequestMapping("/api/downloads")
public class DownloadController {

    private final DownloadService downloadService;

    public DownloadController(DownloadService downloadService) {
        this.downloadService = downloadService;
    }

    @GetMapping("/{downloadToken}")
    public ResponseEntity<DownloadInfoDTO> getStoredFileInfo(@PathVariable String downloadToken) {
        return ResponseEntity.ok(downloadService.getStoredFileByToken(downloadToken));
    }

    @PostMapping("/{downloadToken}/file")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String downloadToken,
            @RequestBody(required = false) DownloadRequestDTO request) {

        String password = request != null ? request.getPassword() : null;

        DownloadInfoDTO info =
                downloadService.getStoredFileByToken(downloadToken);

        Resource resource =
                downloadService.downloadFile(downloadToken, password);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(info.getContentType()))
                .contentLength(info.getSize())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(
                                        info.getFileName(),
                                        StandardCharsets.UTF_8
                                )
                                .build()
                                .toString()
                )
                .body(resource);
    }
}
