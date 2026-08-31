# SECURITY

## Mesures de sécurité

- mots de passe hashés avec BCrypt ;
- authentification JWT ;
- secret JWT conservé hors de Git ;
- contrôle des droits d'accès aux fichiers ;
- mot de passe de téléchargement hashé ;
- contrôle de la taille et des extensions des fichiers.

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
