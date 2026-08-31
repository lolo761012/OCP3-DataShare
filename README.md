# DataShare


Application de partage temporaire et sécurisé de fichiers réalisée dans le cadre du projet 3 de la formation \*\*OpenClassrooms Expert DevOps\*\*.


## Stack technique


- \*\*Frontend\*\* : Angular 22
- \*\*Backend\*\* : Java 21 / Spring Boot 4.1
- \*\*Base de données\*\* : PostgreSQL 16
- \*\*Build backend\*\* : Maven Wrapper
- \*\*Conteneurisation BDD\*\* : Docker Compose
- \*\*Stockage des fichiers\*\* : système de fichiers local
- \*\*Authentification\*\* : JWT


## Prérequis


Les outils suivants doivent être installés :


- Java 21
- Node.js 24
- npm
- Docker Desktop
- Git
- PowerShell


Maven global n'est pas nécessaire pour le backend : le projet utilise le \*\*Maven Wrapper\*\* (`mvnw.cmd`).


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
Le script génère une clé aléatoire de 256 bits et renseigne automatiquement
la variable JWT_SECRET dans .env.

Le backend téléchargera automatiquement les dépendances Maven nécessaires lors du premier lancement.

### Configuration JWT

Le backend utilise un secret JWT local stocké dans `.env`.
Ce fichier n'est pas versionné.

Générer ou renouveler le secret :

```powershell
.\scripts\generate-jwt-secret.ps1
```

Le script génère une clé aléatoire de 256 bits et renseigne `JWT_SECRET`
sans afficher la valeur.

Le backend charge `.env` via `spring.config.import`.

La durée de validité du JWT est de 1 heure par défaut et peut être
surchargée avec `JWT_EXPIRATION_MS`.
```

Variables locales :

```text
DATASHARE_DB_NAME
DATASHARE_DB_USER
DATASHARE_DB_PASSWORD
DATASHARE_STORAGE_PATH
JWT_SECRET
JWT_EXPIRATION_MS
```

## Démarrage en développement


### Méthode recommandée


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

## Configuration locale


Le backend utilise les variables d'environnement suivantes, avec des valeurs locales par défaut :


```text
DATASHARE\_DB\_NAME
DATASHARE\_DB\_USER
DATASHARE\_DB\_PASSWORD
DATASHARE\_STORAGE\_PATH
```


Les secrets applicatifs ne doivent pas être versionnés dans Git.


Les fichiers `.env` sont ignorés par le dépôt.


## Stockage local


Les fichiers téléversés sont destinés à être stockés dans :


```text
storage/files/
```


Le dossier est conservé dans Git grâce à `.gitkeep`, tandis que son contenu est ignoré.


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


En développement, Angular utilise un proxy vers le backend Spring Boot.


Le proxy permet notamment d'appeler :


```text
/api/\*\*
/actuator/\*\*
```

depuis le frontend sans configuration CORS spécifique pour le développement local.


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


La documentation de conception est disponible dans le dossier `docs/`, notamment :

- architecture de la solution ;
- modèle de données ;
- contrat d'API


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