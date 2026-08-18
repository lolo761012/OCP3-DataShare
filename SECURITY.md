# SECURITY

## Objectif

Documenter les contrôles de sécurité, les scans et les décisions prises pour DataShare.

## État

Document initialisé pendant la phase 2. Les résultats des contrôles seront ajoutés au fur et à mesure.

Contrôles déjà mis en œuvre :
- mots de passe utilisateurs hashés avec BCrypt ;
- authentification Spring Security stateless ;
- login via `AuthenticationManager` / `DaoAuthenticationProvider` ;
- chargement des utilisateurs via `CustomUserDetailService` ;
- JWT signé avec secret local fort ;
- expiration JWT configurable ;
- Bearer invalide ou expiré refusé en 401 JSON ;
- secrets hors du dépôt Git.

## Principes prévus

- authentification JWT ;
- mots de passe utilisateurs hashés et salés ;
- mot de passe de téléchargement hashé ;
- secrets hors du dépôt Git ;
- validation côté client et serveur ;
- contrôle de la taille des fichiers ;
- politique de types de fichiers interdits ;
- tokens de téléchargement non prédictibles ;
- contrôle des droits d'accès aux fichiers ;
- gestion explicite des erreurs.

## Upload anonyme

`POST /api/files` accepte une authentification optionnelle :

- aucun header `Authorization` : la requête reste anonyme ;
- Bearer JWT valide : l'utilisateur est placé dans le `SecurityContext` ;
- Bearer présent mais invalide / expiré : réponse 401 JSON.

Un Bearer invalide n'est jamais transformé silencieusement en upload anonyme.

## Secrets

Les secrets applicatifs ne doivent pas être versionnés.

Le projet utilisera des variables d'environnement, notamment pour `JWT_SECRET`.

Le secret local est généré avec :

```powershell
.\scripts\generate-jwt-secret.ps1
```

Le backend charge `.env` via :

```yaml
spring:
  config:
    import: "optional:file:../.env[.properties]"
```

La durée JWT par défaut est de 1 heure.

## Scans de dépendances

À exécuter et documenter avant livraison :

### Frontend

```powershell
cd frontend
npm audit
```

### Backend

Le contrôle des dépendances Maven sera ajouté lors de la phase sécurité.

## Résultats et décisions

À compléter après chaque scan :

- date ;
- outil utilisé ;
- vulnérabilités trouvées ;
- niveau de criticité ;
- décision prise ;
- action réalisée ou justification.
