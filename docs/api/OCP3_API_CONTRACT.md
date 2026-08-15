# DataShare - Contrat API

## Vue d'ensemble

| Méthode | Route | Auth | Usage |
|---|---|---|---|
| POST | `/api/auth/register` | Non | créer un compte |
| POST | `/api/auth/login` | Non | connexion / obtenir JWT |
| POST | `/api/files` | JWT optionnel | uploader un fichier connecté ou anonyme |
| GET | `/api/files` | JWT | historique de mes fichiers |
| DELETE | `/api/files/{id}` | JWT | supprimer mon fichier |
| GET | `/api/downloads/{token}` | Non | consulter les informations du transfert |
| POST | `/api/downloads/{token}/file` | Non | télécharger le fichier |

## Parallèle avec le projet 2

| P2 | P3 |
|---|---|
| POST étudiant | POST fichier |
| GET étudiants | GET fichiers |
| DELETE étudiant | DELETE fichier |
| GET détail étudiant | GET métadonnées fichier |
| — | POST téléchargement du contenu réel |

---

## 1. Création de compte

### POST `/api/auth/register`

Authentification : aucune

Requête :

```json
{
  "email": "user@example.com",
  "password": "Password123"
}
```

Réponse `201 Created` :

```json
{
  "id": 1,
  "email": "user@example.com"
}
```

Erreurs principales :

- `400 Bad Request` : email ou mot de passe invalide
- `409 Conflict` : email déjà utilisé

---

## 2. Connexion

### POST `/api/auth/login`

Authentification : aucune

Requête :

```json
{
  "email": "user@example.com",
  "password": "Password123"
}
```

Réponse `200 OK` :

```json
{
  "token": "eyJ..."
}
```

Erreurs principales :

- `400 Bad Request` : données invalides
- `401 Unauthorized` : identifiants incorrects

---

## 3. Upload d'un fichier

### POST `/api/files`

Authentification : JWT optionnel

Comportement :

- aucun en-tête `Authorization` : upload anonyme, le fichier n'est associé à aucun utilisateur ;
- JWT valide : upload connecté, le fichier est associé à l'utilisateur authentifié ;
- JWT fourni mais invalide ou expiré : la requête est refusée avec `401 Unauthorized`.

Content-Type :

```text
multipart/form-data
```

Paramètres :

| Nom | Type | Obligatoire | Description |
|---|---|---|---|
| `file` | fichier | Oui | fichier à transférer |
| `expirationDays` | entier | Non | durée de conservation entre 1 et 7 jours, 7 par défaut |
| `password` | chaîne | Non | mot de passe de téléchargement, minimum 6 caractères |

Réponse `201 Created` :

```json
{
  "id": 42,
  "fileName": "rapport.pdf",
  "size": 485120,
  "downloadToken": "a8f7b291...",
  "expiresAt": "2026-08-20T12:00:00"
}
```

Erreurs principales :

- `400 Bad Request` : fichier, expiration ou mot de passe invalide
- `401 Unauthorized` : JWT fourni mais invalide ou expiré
- `413 Payload Too Large` : fichier supérieur à 1 Go

---

## 4. Historique des fichiers

### GET `/api/files`

Authentification : JWT

Retourne uniquement les fichiers appartenant à l'utilisateur connecté.
Les uploads anonymes n'apparaissent jamais dans cet historique.

Réponse `200 OK` :

```json
[
  {
    "id": 42,
    "fileName": "rapport.pdf",
    "size": 485120,
    "uploadedAt": "2026-08-15T12:00:00",
    "expiresAt": "2026-08-20T12:00:00",
    "status": "VALID",
    "passwordProtected": true,
    "downloadToken": "a8f7b291..."
  }
]
```

Le statut `VALID` / `EXPIRED` est calculé à partir de `expiresAt`.
Les données `passwordProtected` et `downloadToken` permettent au front d'afficher
l'état de protection et le bouton d'accès prévu dans la maquette.

Erreurs principales :

- `401 Unauthorized` : JWT absent, invalide ou expiré

---

## 5. Suppression d'un fichier

### DELETE `/api/files/{id}`

Authentification : JWT

Paramètre de route :

| Nom | Type | Description |
|---|---|---|
| `id` | entier | identifiant du fichier |

Réponse :

```text
204 No Content
```

Erreurs principales :

- `401 Unauthorized` : JWT absent, invalide ou expiré
- `403 Forbidden` : fichier appartenant à un autre utilisateur
- `404 Not Found` : fichier inexistant

La suppression manuelle supprime le fichier physique et toutes ses métadonnées.

---

## 6. Consultation d'un transfert

### GET `/api/downloads/{token}`

Authentification : aucune

Paramètre de route :

| Nom | Type | Description |
|---|---|---|
| `token` | chaîne | token unique et non prédictible |

Réponse `200 OK` :

```json
{
  "fileName": "rapport.pdf",
  "contentType": "application/pdf",
  "size": 485120,
  "expiresAt": "2026-08-20T12:00:00",
  "passwordProtected": true
}
```

Erreurs principales :

- `404 Not Found` : token invalide
- `410 Gone` : transfert expiré

---

## 7. Téléchargement du fichier

### POST `/api/downloads/{token}/file`

Authentification : aucune

Paramètre de route :

| Nom | Type | Description |
|---|---|---|
| `token` | chaîne | token unique du transfert |

Content-Type : `application/json`

Corps de requête facultatif :

```json
{
  "password": "secret123"
}
```

Le champ `password` est requis uniquement lorsque le transfert est protégé.

Réponse `200 OK` :

```text
Contenu binaire du fichier
```

Le serveur fournit le type MIME et le nom du fichier dans les en-têtes HTTP appropriés.

Erreurs principales :

- `400 Bad Request` : mot de passe requis ou invalide
- `404 Not Found` : token invalide ou fichier absent
- `410 Gone` : transfert expiré
