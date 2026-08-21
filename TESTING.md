# TESTING

## Objectif

Définir et suivre la stratégie de tests de DataShare.

## État actuel

### Backend — US03 / US04
- JUnit / Mockito / Spring Boot Test / Testcontainers
- 29 tests
- Résultat : 29 succès, 0 échec

Exécution :
```powershell
cd backend
.\mvnw.cmd test
```

### Frontend — US03 / US04
- Vitest / Angular TestBed / HttpTestingController
- AuthService : 6 tests
- AuthApiService : 6 tests
- AuthInterceptor : 7 tests
- RegisterComponent : 16 tests
- LoginComponent : 14 tests
- App : 1 test
- Total : 50 tests, 50 succès

Exécution :
```powershell
cd frontend
npm test -- --watch=false
```

## Tests unitaires

À couvrir au minimum pour les fonctionnalités obligatoires du MVP :

- création de compte ;
- authentification ;
- upload ;
- téléchargement ;
- historique ;
- suppression ;
- validations métier et erreurs.

## Tests end-to-end

Prévoir au moins 2 à 3 scénarios critiques avec Cypress ou équivalent.

Scénarios prévus :

1. inscription → connexion → upload → récupération du lien ;
2. téléchargement via lien valide ;
3. consultation de l'historique → suppression d'un fichier.

## Critères déjà validés
- inscription valide ;
- validations email / mot de passe ;
- email déjà utilisé → 409 ;
- login valide → JWT ;
- mauvais identifiants → 401 ;
- JWT stocké côté frontend ;
- Bearer ajouté uniquement aux API internes protégées.

## Couverture

Objectif indicatif : **70 % minimum**.

Une capture du rapport de couverture sera ajoutée avant livraison.
