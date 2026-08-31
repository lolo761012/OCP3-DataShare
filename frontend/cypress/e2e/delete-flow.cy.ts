describe('DataShare delete flow', () => {
  it('shows an uploaded file in My Space and deletes it', () => {
    const email = `e2e-delete-${Date.now()}@example.com`;
    const password = 'Password123!';
    const fileName = `e2e-delete-${Date.now()}.txt`;

    // Création du compte
    cy.visit('/register');

    cy.get('#email').type(email);
    cy.get('#password').type(password);
    cy.get('#confirmPassword').type(password);

    cy.contains('button', 'Créer mon compte').click();

    cy.contains('Compte créé avec succès')
      .should('be.visible');

    // Connexion
    cy.contains("J'ai déjà un compte").click();

    cy.get('#email').type(email);
    cy.get('#password').type(password);

    cy.contains('button', 'Connexion').click();

    cy.url().should('include', '/myspace');

    // Navigation vers l'upload via le header
    cy.get('app-header')
      .contains('a', 'Ajouter des fichiers')
      .click();

    cy.url().should('include', '/upload');

    // Sélection du fichier
    cy.get('input[type="file"]').selectFile(
      {
        contents: Cypress.Buffer.from('DataShare delete E2E test'),
        fileName,
        mimeType: 'text/plain',
      },
      { force: true }
    );

    cy.contains(fileName).should('be.visible');

    cy.contains('button', 'Téléverser').click();

    cy.contains('Félicitations').should('be.visible');

    // Retour à Mon espace via le vrai header
    cy.get('app-header')
      .contains('a', 'Mon espace')
      .click();

    cy.url().should('include', '/myspace');

    // Le fichier doit être présent dans l'historique
    cy.contains('.file-row', fileName)
      .should('be.visible');

    // Accepter la fenêtre confirm() utilisée par l'application
    cy.on('window:confirm', () => true);

    // Supprimer précisément la ligne correspondant à notre fichier
    cy.contains('.file-row', fileName)
      .within(() => {
        cy.contains('button', 'Supprimer').click();
      });

    // Vérifier le résultat
    cy.contains('Fichier supprimé avec succès.')
      .should('be.visible');

    cy.contains('.file-row', fileName)
      .should('not.exist');
  });
});