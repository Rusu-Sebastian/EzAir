package com.proiect;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class PaginaPrincipalaAdminController {
    private static final Logger jurnal = Logger.getLogger(PaginaPrincipalaAdminController.class.getName());
    private static final String TITLU_EROARE = "Eroare";
    private static final String TITLU_CONFIRMARE = "Confirmare";
    private static final String CHEIE_ID_UTILIZATOR = "userId";
    private static final String CHEIE_ADMIN = "esteAdmin";
    private static final String PAGINA_LOGIN = "login";
    private static final String PAGINA_ZBORURI_ADMIN = "paginaZboruriAdmin";
    private App app;
    
    @FXML
    @SuppressWarnings("unused") // Used by FXML
    public void initialize() {
        app = App.getInstance();
        verificaPermisiuniAdmin();
    }
    
    @FXML
    @SuppressWarnings("unused") // Used by FXML
    public void gestioneazaZboruri() {
        if (!esteUtilizatorAdmin()) {
            jurnal.severe("Încercare de acces neautorizat la panoul de administrare zboruri");
            Platform.runLater(() -> {
                afiseazaEroare("Nu aveți permisiunile necesare pentru accesarea acestei pagini");
                try {
                    app.setRoot(PAGINA_LOGIN);
                } catch (IOException e) {
                    jurnal.log(Level.SEVERE, "Eroare la redirecționare către login", e);
                }
            });
            return;
        }

        try {
            app.setRoot(PAGINA_ZBORURI_ADMIN);
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, "Eroare la navigare către panoul de zboruri", e);
            afiseazaEroare("Nu s-a putut deschide panoul de zboruri");
        }
    }

    @FXML
    @SuppressWarnings("unused") // Used by FXML
    public void gestioneazaUtilizatori() {
        try {
            app.setRoot("paginaUseriAdmin");
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, "Eroare la navigarea către pagina de gestionare utilizatori", e);
            afiseazaEroare("Nu s-a putut deschide pagina de gestionare a utilizatorilor");
        }
    }
    
    @FXML
    @SuppressWarnings("unused") // Used by FXML
    public void deconectare() {
        Alert confirmare = new Alert(Alert.AlertType.CONFIRMATION);
        confirmare.setTitle(TITLU_CONFIRMARE);
        confirmare.setHeaderText("Confirmare deconectare");
        confirmare.setContentText("Sigur doriți să vă deconectați?");
        
        confirmare.showAndWait().ifPresent(raspuns -> {
            if (raspuns == ButtonType.OK) {
                App.getDateUtilizator().clear();
                try {
                    app.setRoot(PAGINA_LOGIN);
                } catch (IOException e) {
                    jurnal.log(Level.SEVERE, "Eroare la deconectare", e);
                    afiseazaEroare("Nu s-a putut realiza deconectarea");
                }
            }
        });
    }
    
    private void verificaPermisiuniAdmin() {
        if (!esteUtilizatorAdmin()) {
            jurnal.severe("Încercare de acces neautorizat la panoul de administrare");
            Platform.runLater(() -> {
                afiseazaEroare("Nu aveți permisiunile necesare pentru accesarea acestei pagini");
                try {
                    app.setRoot(PAGINA_LOGIN);
                } catch (IOException e) {
                    jurnal.log(Level.SEVERE, "Eroare la redirecționare către login", e);
                }
            });
        }
    }
    
    private boolean esteUtilizatorAdmin() {
        String idUtilizator = (String) App.getDateUtilizator().get(CHEIE_ID_UTILIZATOR);
        String esteAdmin = (String) App.getDateUtilizator().get(CHEIE_ADMIN);
        return idUtilizator != null && "true".equals(esteAdmin);
    }
    
    private void afiseazaEroare(String mesaj) {
        Platform.runLater(() -> {
            Alert eroare = new Alert(Alert.AlertType.ERROR);
            eroare.setTitle(TITLU_EROARE);
            eroare.setHeaderText("A apărut o eroare");
            eroare.setContentText(mesaj);
            eroare.showAndWait();
        });
    }
}
