# MAINTENANCE

## Objectif

Documenter les opérations courantes de maintenance de DataShare.

## État

Document initialisé pendant la phase 2. Il sera enrichi pendant le développement.

## Démarrage / arrêt

La procédure de développement est documentée dans le `README.md`.

## Mise à jour des dépendances

### Frontend

Contrôler régulièrement :

```powershell
cd frontend
npm outdated
```

Les mises à jour doivent être testées avant intégration.

### Backend

Les versions Maven / Spring Boot seront contrôlées avant toute mise à jour.

## Fréquence

Prévision :

- contrôle des dépendances : avant livraison et lors des opérations de maintenance ;
- contrôle des vulnérabilités : avant livraison et après mise à jour importante.

## Risques liés aux mises à jour

- incompatibilité de versions ;
- régression fonctionnelle ;
- changement de configuration ;
- vulnérabilité introduite ou non corrigée.

## Base PostgreSQL

Le développement local utilise Docker Compose.

```powershell
docker compose up -d
docker compose down
```

Le volume PostgreSQL est conservé lors d'un `docker compose down` normal.

## Logs et diagnostic

À compléter avec :

- localisation des logs backend ;
- diagnostic des erreurs frontend ;
- contrôle de l'état PostgreSQL ;
- procédures de sauvegarde / restauration si nécessaires.

## Scripts d'exploitation

Le script de développement est :

```powershell
.\scripts\dev.ps1 start
.\scripts\dev.ps1 status
.\scripts\dev.ps1 stop
```

Les scripts d'installation et de configuration de la base demandés dans les livrables seront ajoutés dans la phase dédiée.
