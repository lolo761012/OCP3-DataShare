package com.openclassrooms.datashare.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_history")
public class StoredFileHistory {

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "fileName", nullable = false)
    private String fileName;

    @Column(name = "size")
    private Long size;

    @Column(name = "downloadToken", nullable = false, unique = true)
    private String downloadToken;

    @Column(name = "uploadedAt", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "expiresAt", nullable = false)
    private LocalDateTime expiresAt;
}
