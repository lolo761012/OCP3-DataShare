describe('DataShare download flow', () => {
  it('downloads a file from a valid link', () => {
    const fileName = `e2e-download-${Date.now()}.txt`;
    const fileContent = 'DataShare E2E download test';

    cy.visit('/upload');

    cy.get('button[aria-label="Sélectionner un fichier"]')
      .should('be.visible')
      .click();

    cy.get('input[type="file"]').selectFile(
      {
        contents: Cypress.Buffer.from(fileContent),
        fileName,
        mimeType: 'text/plain',
      },
      { force: true }
    );

    cy.contains(fileName).should('be.visible');

    cy.contains('button', 'Téléverser').click();

    cy.get('.download-link')
      .should('be.visible')
      .invoke('text')
      .then((downloadLink) => {
        cy.visit(downloadLink.trim());
      });

    cy.contains(fileName).should('be.visible');

    cy.contains('button', 'Télécharger')
      .should('be.visible')
      .click();

    cy.readFile(`cypress/downloads/${fileName}`, {
      timeout: 10000,
    }).should('eq', fileContent);
  });
});