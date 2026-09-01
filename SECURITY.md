# SECURITY

## Mesures de sécurité

- mots de passe hashés avec BCrypt ;
- authentification JWT ;
- secret JWT conservé hors de Git ;
- contrôle des droits d'accès aux fichiers ;
- mot de passe de téléchargement hashé ;
- contrôle de la taille et des extensions des fichiers.

## Sécurité applicative

### Authentification et droits d'accès

L'authentification repose sur des JWT.

Après la connexion, le frontend ajoute le JWT aux requêtes qui nécessitent une authentification. Le backend vérifie alors ce token avant d'autoriser l'accès.

Spring Security ne conserve pas de session utilisateur sur le serveur (`STATELESS`) : chaque requête protégée doit donc présenter son JWT.

Les principales règles d'accès sont :

- `/api/auth/**` : public ;
- `/api/downloads/**` : public, avec un token de téléchargement valide ;
- `POST /api/files` : accessible avec ou sans compte afin de permettre l'upload anonyme ;
- `GET /api/files` : réservé aux utilisateurs authentifiés ;
- `DELETE /api/files/{id}` : réservé aux utilisateurs authentifiés et au propriétaire du fichier.

Lors d'une suppression, le backend vérifie que le fichier appartient bien à l'utilisateur connecté.

### Validation des données et injections

L'accès à PostgreSQL passe par Spring Data JPA plutôt que par des requêtes SQL construites manuellement.

Les données principales reçues par l'API sont aussi contrôlées côté backend : identifiants, fichier, durée d'expiration et mot de passe éventuel.

Ces choix limitent notamment le risque d'injection SQL.

### Affichage des données

Les données provenant de l'utilisateur, par exemple le nom d'un fichier, sont affichées par Angular comme du texte et non comme du code HTML ou JavaScript à exécuter.

### CSRF

La protection CSRF intégrée à Spring Security est désactivée car DataShare n'utilise pas de cookie de session pour authentifier l'utilisateur.

Pour les routes protégées, le frontend ajoute explicitement le JWT dans l'en-tête `Authorization`. Le navigateur ne l'envoie donc pas automatiquement comme il le ferait avec un cookie de connexion.

### Upload de fichiers

Le backend applique plusieurs contrôles :

- taille maximale de 1 Go ;
- durée d'expiration comprise entre 1 et 7 jours ;
- certaines extensions exécutables sont interdites ;
- validation du nom de fichier ;
- mot de passe de téléchargement facultatif avec au moins 6 caractères ;
- stockage du mot de passe sous forme de hash BCrypt ;
- génération d'un token de téléchargement unique.

## Scan de sécurité

### Frontend

Commande :

```powershell
npm audit
```

Résultat :

```text
found 0 vulnerabilities
```

Aucune vulnérabilité connue détectée.

### Backend

Scan réalisé avec OWASP Dependency-Check.

Premier scan :
- vulnérabilités détectées dans Log4j, PostgreSQL JDBC et Tomcat ;
- les versions concernées ont été mises à jour.

Après correction :
- 46 tests backend réussis ;
- nouveau scan OWASP Dependency-Check ;
- aucune vulnérabilité connue détectée ;
- BUILD SUCCESS.

Aucune vulnérabilité n'a été acceptée ou ignorée.

Commande :

```powershell
.\mvnw.cmd org.owasp:dependency-check-maven:13.0.0:check
```

## Conclusion

Les scans frontend et backend ont été réalisés.

- `npm audit` : 0 vulnérabilité ;
- OWASP Dependency-Check après correction : 0 vulnérabilité détectée.
