# PERF

## Test k6

Endpoint testé : `POST /api/files`

- 5 utilisateurs simultanés
- durée : 10 s
- 2623 requêtes
- 0 erreur
- temps moyen : 18,82 ms
- 95 % des requêtes : moins de 34,87 ms
- débit : environ 262 requêtes/s

### Analyse

Le test a traité 2623 uploads en 10 secondes sans erreur.

95 % des requêtes ont répondu en moins de 34,87 ms et le débit a atteint environ 262 requêtes par seconde.

Dans les conditions de ce test local, l'endpoint d'upload supporte donc la charge testée sans erreur ni ralentissement important.

## Métriques backend

Spring Boot Actuator :

- 2623 requêtes comptées
- temps cumulé : 47,62 s
- temps maximum : 500 ms

## Logs

Un log est écrit après chaque upload :

```text
file_upload id=8463 size=26 owner=anonymous
```

## Frontend

La taille initiale du frontend Angular est de 340,81 kB, sous la limite d'avertissement fixée à 500 kB.

## Performance navigateur

Test Lighthouse sur la page `/myspace` en local :

- Performance : 79
- Accessibilité : 100
- Bonnes pratiques : 100
- SEO : 90

### Analyse et optimisations possibles

Le premier test, réalisé avec la configuration Angular de développement, obtenait un score de performance de 59.

Un second test a été réalisé avec Angular en configuration de production
(`ng serve --configuration production`). Le score de performance est passé à 79.

Un test sur une application réellement déployée permettrait d'obtenir une mesure encore plus représentative des conditions d'utilisation réelles.

Si nécessaire, les éléments chargés par chaque page et les appels au backend pourraient ensuite être analysés pour identifier ce qui ralentit l'affichage.