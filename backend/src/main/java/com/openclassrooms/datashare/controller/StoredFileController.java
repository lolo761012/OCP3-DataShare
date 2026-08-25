package com.openclassrooms.datashare.controller;

import com.openclassrooms.datashare.dto.StoredFileDTO;
import com.openclassrooms.datashare.dto.StoredFileListDTO;
import com.openclassrooms.datashare.dto.StoredFileUploadResponseDTO;
import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.service.StoredFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class StoredFileController {
    private final StoredFileService storedFileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StoredFileUploadResponseDTO> uploadStoredFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "expirationDays", required = false) Integer expirationDays,
            @RequestParam(value = "password", required = false) String password,
            Authentication authentication) {

        User owner = resolveOwner(authentication);

        StoredFileUploadResponseDTO response = storedFileService.uploadStoredFile(
                file, expirationDays, password, owner
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping({"", "/"})
    public ResponseEntity<List<StoredFileListDTO>> listStoredFiles(
        Authentication authentication){

            User owner = resolveOwner(authentication);

        return ResponseEntity.ok(storedFileService.getFilesForUser(owner.getId()));
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

    private User resolveOwner(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        return principal instanceof User user ? user : null;
    }
}