package com.proiect;

import java.io.IOException;
import javafx.fxml.FXML;

public class LogInController {

    @FXML
    private void logIn() throws IOException {
        String username = ((javafx.scene.control.TextField) App.scene.lookup("#username")).getText();
        String password = ((javafx.scene.control.PasswordField) App.scene.lookup("#parola")).getText();

        if(username.equals("") || password.equals("")) {
            System.out.println("Completati toate campurile!");
            return;
        }else {
            if(functii.verificareDateCont(username, password)==false) {
                System.out.println("Datele introduse sunt gresite!");
                return;
            }
            else{
                App.setRoot("paginaPrincipala");
            }
        }
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);
    }

    @FXML
    private void paginaCreazaCont() throws IOException {
        App.setRoot("creareCont");
    }
    
    
}
 