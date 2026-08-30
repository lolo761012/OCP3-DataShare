package com.openclassrooms.datashare.controller;

import com.openclassrooms.datashare.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;



import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;


@TestPropertySource(properties =
        "datashare.storage.path=target/test-storage")
public class StoredFileControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void upload_file_returns_201() throws Exception {

        // GIVEN
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "contenu du fichier".getBytes()
        );

        // WHEN / THEN
        mockMvc.perform(
                        multipart("/api/files")
                                .file(file)
                                .param("expirationDays", "1")
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("test.txt"))
                .andExpect(jsonPath("$.downloadToken").isNotEmpty());
    }
}
