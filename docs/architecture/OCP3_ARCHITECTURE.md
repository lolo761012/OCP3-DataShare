# DataShare - Schéma d'architecture

```mermaid
flowchart LR
    U[Utilisateur / Navigateur]
    F[Frontend Angular 22]
    B[Backend Spring Boot 3.5<br/>Java 21<br/>API REST]
    DB[(PostgreSQL<br/>Métadonnées et historique)]
    FS[(Stockage local<br/>Contenu des fichiers)]
    JWT[Spring Security<br/>JWT]
    S[Scheduler Spring<br/>@Scheduled - purge quotidienne]

    U -->|HTTPS| F
    F -->|REST JSON / multipart<br/>JWT selon endpoint| B

    B --> JWT
    B -->|JPA / SQL| DB
    B -->|Lecture / écriture| FS

    S -->|déclenche la purge| B
    B -->|Mise à jour expiration / historique| DB
    B -->|Suppression contenu expiré| FS
```

## Principes

- Angular communique uniquement avec l'API REST Spring Boot.
- PostgreSQL stocke les comptes, métadonnées et traces historiques nécessaires.
- Le contenu binaire des fichiers est stocké dans le système de fichiers local.
- JWT protège les endpoints nécessitant un utilisateur connecté.
- `POST /api/files` accepte soit un JWT valide (upload connecté), soit aucun JWT
  (upload anonyme). Un JWT fourni mais invalide est refusé.
- Le scheduler Spring lance la purge quotidienne des contenus expirés.
- Après expiration, le contenu physique est supprimé et seules les informations minimales
  nécessaires à l'historique et au message « lien expiré » sont conservées.
