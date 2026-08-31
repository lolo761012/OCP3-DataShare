# MAINTENANCE

## Démarrage / arrêt

L'application peut être lancée séparément.

Backend :

    cd backend
    .\mvnw.cmd spring-boot:run

Frontend :

    cd frontend
    npm start

PostgreSQL :

    docker compose up -d

Pour arrêter PostgreSQL :

    docker compose down

Le volume PostgreSQL est conservé après un `docker compose down`.

## Script de développement

Le script `scripts/dev.ps1` permet de gérer l'ensemble de l'environnement DataShare.

Depuis la racine du projet :

    .\scripts\dev.ps1 start

Démarre PostgreSQL, le backend et le frontend.

    .\scripts\dev.ps1 status

Affiche l'état de PostgreSQL, du backend et du frontend.

    .\scripts\dev.ps1 stop

Arrête le frontend, le backend et PostgreSQL.

## PostgreSQL

Vérifier l'état de la base :

    docker compose ps

Se connecter à PostgreSQL :

    docker exec -it datashare-postgres psql -U datashare -d datashare

Commandes utiles dans PostgreSQL :

    \dt

Affiche les tables.

    \d stored_file

Affiche la structure de la table `stored_file`.

    SELECT * FROM stored_file;

Affiche les fichiers enregistrés en base.

    \q

Quitte PostgreSQL.

## Mise à jour des dépendances

Frontend :

    npm outdated

La commande affiche les dépendances pour lesquelles une version plus récente existe.

Pour le backend, les versions sont définies dans `pom.xml`.

Après une mise à jour, les tests doivent être relancés.

Fréquence :

- vérifier les dépendances avant une livraison ;
- vérifier les vulnérabilités après une mise à jour importante.

Risques principaux :

- incompatibilité de versions ;
- régression ;
- changement de configuration ;
- vulnérabilité.

## Logs et diagnostic

Les logs du backend sont affichés dans le terminal Spring Boot.

Les erreurs frontend peuvent être consultées dans la console du navigateur.

L'état de PostgreSQL peut être vérifié avec :

    docker compose ps

## Purge des fichiers expirés

La purge est automatique :

- premier contrôle 5 secondes après le démarrage ;
- nouveau contrôle toutes les heures ;
- le fichier physique est supprimé ;
- un historique minimal est conservé pour pouvoir être affiché dans l'espace Mes Fichiers.

## Fichiers orphelins

Lors d'un upload, le fichier est d'abord écrit sur le disque puis ses informations sont enregistrées dans PostgreSQL.

Si l'enregistrement en base échoue, le fichier physique est supprimé.

Si cette suppression échoue également, l'erreur est enregistrée dans les logs du backend.

Un contrôle de maintenance pourra comparer `storage/files` avec les fichiers référencés en base afin d'identifier d'éventuels fichiers orphelins.