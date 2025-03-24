package com.proiect;

import java.io.IOException;
import javafx.fxml.FXML;

public class CreareContNumeController {

    // butonu de cancel care duce inapoi la pagina de pornire adica la login
    @FXML
    private void cancelCreareCont() throws IOException {
        App.setRoot("login");
    }
    



    // butonu de next care salveaza datele despre nume si email introduse si merge la pagina urmatoare care este cea ce preia data nasterii
     @FXML
    private void creareContNumeNext() throws IOException {
        String nume = ((javafx.scene.control.TextField) App.scene.lookup("#nume")).getText();
        String prenume = ((javafx.scene.control.TextField) App.scene.lookup("#prenume")).getText();
        String email = ((javafx.scene.control.TextField) App.scene.lookup("#email")).getText();


        if(nume.isEmpty() || prenume.isEmpty() || email.isEmpty()) {
            System.out.println("Toate campurile sunt obligatorii");
            return;
        }else {
            System.out.println("Nume: " + nume);
            System.out.println("Prenume: " + prenume);
            System.out.println("Email: " + email);
        }
    }
}