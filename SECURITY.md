# SECURITY

## Objectif

Documenter les contrôles de sécurité, les scans et les décisions prises pour DataShare.

## État

Document initialisé pendant la phase 2. Les résultats des contrôles seront ajoutés au fur et à mesure.

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

## Secrets

Les secrets applicatifs ne doivent pas être versionnés.

Le projet utilisera des variables d'environnement, notamment pour `JWT_SECRET`.

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
