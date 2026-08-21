package com.openclassrooms.datashare.controller;

import com.openclassrooms.datashare.dto.StoredFileDTO;
import com.openclassrooms.datashare.service.StoredFileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

        import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class StoredFileController {
    private final StoredFileService storedFileService;

    @PostMapping
    public ResponseEntity<String>  uploadStoredFile(@Valid @RequestBody StoredFileDTO storedFileDTO) {
        StoredFileDTO createdStoredFile = storedFileService.uploadStoredFile (storedFileDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Fichier uploadé avec id: " + createdStoredFile.getId());
    }

    @GetMapping({"", "/"})
    public ResponseEntity<List<StoredFileDTO>> listStoredFiles() {
        return ResponseEntity.ok(storedFileService.listStoredFiles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoredFileDTO> getStoredFile(@PathVariable Long id) {
        return ResponseEntity.ok(storedFileService.getStoredFileByToken(id));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteStoredFile(@PathVariable Long id) {

        storedFileService.deleteStoredFile(id);

        return ResponseEntity.ok(
                "Le fichier avec l'id " + id + " a été effacé avec succès");
    }
}
