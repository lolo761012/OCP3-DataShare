package com.openclassrooms.datashare.controller;

import com.openclassrooms.datashare.entities.StoredFile;
import com.openclassrooms.datashare.repository.StoredFileRepository;
import com.openclassrooms.datashare.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class DownloadControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StoredFileRepository storedFileRepository;

    @TempDir
    Path tempDir;

    @Test
    void download_file_returns_200() throws Exception {

        // GIVEN
        String token = "download-token";

        Path tempFile = tempDir.resolve("test.txt");
        Files.writeString(tempFile, "contenu du fichier");

        StoredFile storedFile = new StoredFile();
        storedFile.setFileName("test.txt");
        storedFile.setSize(Files.size(tempFile));
        storedFile.setExpiresAt(LocalDateTime.now().plusDays(1));
        storedFile.setDownloadToken(token);
        storedFile.setStoragePath(tempFile.toString());
        storedFile.setPasswordHash(null);
        storedFile.setContentType("text/plain");

        storedFileRepository.saveAndFlush(storedFile);

        // WHEN / THEN
        mockMvc.perform(
                        post("/api/downloads/" + token + "/file")
                )
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        containsString("test.txt")
                ))
                .andExpect(content().bytes(
                        "contenu du fichier".getBytes()
                ));
    }
}
