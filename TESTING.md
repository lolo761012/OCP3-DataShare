# TESTING

## Objectif

Définir et suivre la stratégie de tests de DataShare.

## État

Document initialisé pendant la phase 2. Les résultats seront complétés au fur et à mesure du développement.

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

## Critères d'acceptation

Les critères seront détaillés pour chaque User Story avant validation.

## Couverture

Objectif indicatif : **70 % minimum**.

Une capture du rapport de couverture sera ajoutée avant livraison.

## Exécution

Les commandes exactes seront ajoutées dès la mise en place des tests backend et frontend.
