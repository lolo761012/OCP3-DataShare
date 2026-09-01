# DataShare


Application de partage temporaire et sécurisé de fichiers réalisée dans le cadre du projet 3 de la formation \*\*OpenClassrooms Expert DevOps\*\*.


## Stack technique


- **Frontend** : Angular 22
- **Backend** : Java 21 / Spring Boot 4.1
- **Base de données** : PostgreSQL 16
- **Base de données locale** : Docker Compose
- **Stockage des fichiers** : système de fichiers local
- **Authentification** : JWT


## Prérequis


Les outils suivants doivent être installés :


- Java 21
- Node.js 24
- npm
- Docker Desktop
- Git
- PowerShell


Le projet utilise le Maven Wrapper (`mvnw.cmd`), configuré avec Maven 3.9.16.


## Installation locale - Windows / PowerShell


Cette procédure de développement local a été validée sous Windows 11 avec PowerShell.


Cloner le dépôt puis se placer à sa racine :


```powershell
git clone https://github.com/lolo761012/OCP3-DataShare.git
cd OCP3-DataShare
```


Installer les dépendances du frontend :


```powershell
cd frontend
npm install
cd ..
```
Le backend téléchargera automatiquement les dépendances Maven nécessaires lors du premier lancement.

### Configuration JWT

Le backend utilise un secret JWT local stocké dans `.env`.
Ce fichier n'est pas versionné.

Générer ou renouveler le secret :

```powershell
.\scripts\generate-jwt-secret.ps1
```

Le script génère une clé aléatoire de 256 bits et renseigne automatiquement `JWT_SECRET` dans le fichier `.env`, sans afficher le secret dans le terminal.

Le backend charge `.env` via `spring.config.import`.

La durée de validité du JWT est de 1 heure par défaut.
Elle peut être modifiée avec la variable `JWT_EXPIRATION_MS`, exprimée en millisecondes.


## Démarrage en développement


### Démarrage par script


Depuis la racine du projet :


```powershell
.\\scripts\\dev.ps1 start
```

Le script démarre :

1\. PostgreSQL avec Docker Compose ;
2\. le backend Spring Boot ;
3\. le frontend Angular.


Lorsque les composants sont disponibles, le script affiche :


```text
PostgreSQL: UP
Backend: UP
Frontend: UP
DataShare is ready.
```


## Vérification de l'état

```powershell
.\\scripts\\dev.ps1 status

```


Le script vérifie notamment :

- l'état de Docker ;
- le conteneur PostgreSQL ;
- le port PostgreSQL `5432` ;
- le backend sur le port `8080` ;
- le frontend sur le port `4200` ;
- le endpoint de santé Spring Boot ;
- l'accès HTTP au frontend.


## Arrêt


```powershell
.\\scripts\\dev.ps1 stop
```


Le script arrête le frontend, le backend et le conteneur PostgreSQL.


Le volume Docker PostgreSQL est conservé : les données de la base ne sont donc pas supprimées par un arrêt normal.


## Démarrage manuel


En cas de besoin, chaque composant peut être lancé séparément.


### PostgreSQL


Depuis la racine :


```powershell
docker compose up -d
```


### Backend


Dans un second terminal :


```powershell
cd backend
.\\mvnw.cmd spring-boot:run
```


### Frontend


Dans un troisième terminal :


```powershell
cd frontend
npm start
```


### Arrêt manuel


Arrêter le frontend et le backend avec `Ctrl+C` dans leurs terminaux respectifs, puis depuis la racine :


```powershell
docker compose down
```


## URLs de développement


- Frontend : http://localhost:4200
- Backend : http://localhost:8080
- Health backend : http://localhost:8080/actuator/health
- PostgreSQL : `localhost:5432`

## Connexion à PostgreSQL

La base PostgreSQL de développement tourne dans le conteneur Docker `datashare-postgres`.

Depuis PowerShell :

```powershell
docker exec -it datashare-postgres psql -U datashare -d datashare
```


## API d'authentification

- `POST /api/auth/register` → 201 en cas de succès
- `POST /api/auth/login` → 200 + `{ "token": "..." }`
- mauvais identifiants → 401 JSON

Les routes protégées utilisent :

`Authorization: Bearer <jwt>`

`POST /api/files` accepte un JWT optionnel :
- pas de Bearer → anonyme ;
- Bearer valide → utilisateur authentifié ;
- Bearer invalide → 401.


## Utilisation

Une fois l'application démarrée, ouvrir :

http://localhost:4200

L'utilisateur peut :

- créer un compte et se connecter ;
- envoyer un fichier avec ou sans compte ;
- choisir une durée d'expiration de 1 à 7 jours ;
- protéger éventuellement le téléchargement par mot de passe ;
- partager le lien de téléchargement généré ;
- consulter et supprimer ses fichiers depuis son espace personnel.

### Variables de configuration

```text
DATASHARE_DB_NAME
DATASHARE_DB_USER
DATASHARE_DB_PASSWORD
DATASHARE_STORAGE_PATH
JWT_SECRET
JWT_EXPIRATION_MS
```

## Stockage local


Les fichiers téléversés sont destinés à être stockés dans :


```text
storage/files/
```


Le dossier est conservé dans Git grâce à `.gitkeep`, tandis que son contenu est ignoré.

Les secrets applicatifs ne sont pas versionnés dans Git.

Les fichiers `.env` sont ignorés par le dépôt.



## Structure principale


```text
DataShare/
├── backend/            # API Spring Boot
├── frontend/           # Application Angular
├── docs/               # Documentation technique et conception
├── scripts/            # Scripts PowerShell du projet
├── storage/
│   └── files/          # Fichiers téléversés en local
├── docker-compose.yml  # PostgreSQL local
├── TESTING.md
├── SECURITY.md
├── PERF.md
├── MAINTENANCE.md
└── README.md
```

## Communication frontend / backend

En développement, le frontend Angular fonctionne sur le port `4200` et le backend Spring Boot sur le port `8080`.

Angular utilise un proxy local : les requêtes commençant par `/api` ou `/actuator` sont automatiquement transmises au backend.

Par exemple :

```text
/api/files → http://localhost:8080/api/files
```


## Tests

Les tests du projet sont documentés dans [`TESTING.md`](TESTING.md).

État actuel :
- 46 tests backend réussis ;
- 92 tests frontend réussis ;
- 3 scénarios E2E Cypress réussis ;
- couverture backend : 72,57 % ;
- couverture frontend : 74,29 %.

Les contrôles de sécurité, de performance et de maintenance sont documentés dans :
- [`SECURITY.md`](SECURITY.md)
- [`PERF.md`](PERF.md)
- [`MAINTENANCE.md`](MAINTENANCE.md)


## Documentation

La documentation de conception est disponible dans le dossier `docs/` :

- [Architecture de la solution](docs/architecture/OCP3_ARCHITECTURE.md)
- [Modèle de données](docs/data-model/OCP3_MCD.md)
- [Contrat d'API](docs/api/OCP3_API_CONTRACT.md)

## État du projet

Le MVP DataShare est fonctionnel.

Fonctionnalités principales :
- création de compte ;
- connexion JWT ;
- upload de fichier authentifié ou anonyme ;
- génération d'un lien de téléchargement ;
- téléchargement de fichier ;
- consultation des fichiers d'un utilisateur ;
- suppression de fichier ;
- expiration et purge automatique des fichiers.

### Codes HTTP principaux

- `200 OK` : requête réussie ;
- `201 Created` : ressource créée avec succès ;
- `204 No Content` : suppression réussie ;
- `400 Bad Request` : données envoyées invalides ;
- `401 Unauthorized` : authentification absente ou invalide ;
- `403 Forbidden` : accès refusé ;
- `404 Not Found` : ressource ou lien introuvable ;
- `410 Gone` : lien de téléchargement expiré ;
- `413 Payload Too Large` : fichier supérieur à la taille maximale autorisée.
