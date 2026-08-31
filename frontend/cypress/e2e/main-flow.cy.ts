describe('DataShare main flow', () => {
  it('registers, logs in and uploads a file', () => {
    const email = `e2e-${Date.now()}@example.com`;
    const password = 'azerty123';

    cy.visit('/register');

    cy.get('#email').type(email);
    cy.get('#password').type(password);
    cy.get('#confirmPassword').type(password);

    cy.contains('button', 'Créer mon compte').click();

    cy.contains('Compte créé avec succès').should('be.visible');

    cy.contains("J'ai déjà un compte").click();

    cy.get('#email').type(email);
    cy.get('#password').type(password);

    cy.contains('button', 'Connexion').click();

    cy.url().should('include', '/myspace');

    cy.get('app-header').contains('a', 'Ajouter des fichiers').click();
    cy.url().should('include', '/upload');

    cy.get('button[aria-label="Sélectionner un fichier"]').should('be.visible').click();

    cy.get('input[type="file"]').selectFile(
    {
        contents: Cypress.Buffer.from('DataShare E2E test file'),
        fileName: 'e2e-test.txt',
        mimeType: 'text/plain',
    },
    { force: true }
    );

    cy.contains('e2e-test.txt').should('be.visible');

    cy.contains('button', 'Téléverser').click();

    cy.contains('Félicitations').should('be.visible');
    cy.get('.download-link')
      .should('be.visible')
      .and('contain', '/downloads/');
  });
});