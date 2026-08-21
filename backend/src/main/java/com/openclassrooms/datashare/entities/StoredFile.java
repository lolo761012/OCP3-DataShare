package com.openclassrooms.datashare.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "files")
public class StoredFile {
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = true)
    private User owner;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "fileName", nullable = false)
    private String fileName;

    @Column(name = "size")
    private Long size;

    @CreationTimestamp
    @Column(name = "uploadedAt")
    private LocalDateTime uploadedAt;

    @Column(name = "expiresAt")
    private LocalDateTime expiresAt;

    @Column(name = "downloadToken", nullable = false, unique = true)
    private String downloadToken;

    @Column(name = "storagePath", nullable = false)
    private String storagePath;

    private String passwordHash;

    @Column(name = "contentType", nullable = false)
    private String contentType;


}