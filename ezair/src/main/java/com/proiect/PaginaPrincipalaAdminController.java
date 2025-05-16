package com.proiect;

import javafx.fxml.FXML;

public class PaginaPrincipalaAdminController {
    
    @FXML
    private void zboruriAdmin() throws Exception {
        App.setRoot("paginaZboruriAdmin");
    }

    @FXML
    private void useriAdmin() throws Exception {
        App.setRoot("paginaUseriAdmin");
    }
}
