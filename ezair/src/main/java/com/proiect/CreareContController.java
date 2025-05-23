package com.proiect;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class CreareContController {

    private static final Logger jurnal = Logger.getLogger(CreareContController.class.getName());
    private static final String TITLU_ALERTA = "Atenție";
    private static final String PAGINA_LOGIN = "login";
    private static final String CAMP_ZI_NASTERE = "#ziuaNasterii";
    private static final String CAMP_LUNA_NASTERE = "#lunaNasterii";
    private static final String CAMP_AN_NASTERE = "#anNasterii";
    private static final String CAMP_NUME_UTILIZATOR = "#numeUtilizator";

    @FXML
    private void inapoiLaCreareContInceput() throws IOException {
        jurnal.info("Navigare înapoi la pagina de conectare.");
        App.setRoot(PAGINA_LOGIN);
    }

    @FXML
    private void inapoiLaCreareContFinal() throws IOException {
        jurnal.info("Navigare înapoi la pagina de creare cont început.");
        App.setRoot("creareContInceput");
    }

    @FXML
    private void anuleazaCreareCont() throws IOException {
        jurnal.info("Anulare creare cont și navigare înapoi la pagina de conectare.");
        App.setRoot(PAGINA_LOGIN);
    }

    @FXML
    private void proceseazaDatePersonale() throws IOException {
        try {
            String nume = ((javafx.scene.control.TextField) App.scena.lookup("#nume")).getText();
            String prenume = ((javafx.scene.control.TextField) App.scena.lookup("#prenume")).getText();

            if (nume.isEmpty() || prenume.isEmpty() ||
                ((javafx.scene.control.TextField) App.scena.lookup(CAMP_ZI_NASTERE)).getText().isEmpty() ||
                ((javafx.scene.control.TextField) App.scena.lookup(CAMP_LUNA_NASTERE)).getText().isEmpty() ||
                ((javafx.scene.control.TextField) App.scena.lookup(CAMP_AN_NASTERE)).getText().isEmpty()) {
                jurnal.warning("Toate câmpurile sunt obligatorii.");
                return;
            }

            if (((javafx.scene.control.TextField) App.scena.lookup(CAMP_ZI_NASTERE)).getText().length() > 2 ||
                ((javafx.scene.control.TextField) App.scena.lookup(CAMP_LUNA_NASTERE)).getText().length() > 2 ||
                ((javafx.scene.control.TextField) App.scena.lookup(CAMP_AN_NASTERE)).getText().length() > 4) {
                jurnal.warning("Format dată naștere invalid.");
                return;
            }

            int ziNastere = Integer.parseInt(((javafx.scene.control.TextField) App.scena.lookup(CAMP_ZI_NASTERE)).getText());
            int lunaNastere = Integer.parseInt(((javafx.scene.control.TextField) App.scena.lookup(CAMP_LUNA_NASTERE)).getText());
            int anNastere = Integer.parseInt(((javafx.scene.control.TextField) App.scena.lookup(CAMP_AN_NASTERE)).getText());
            int anCurent = java.time.Year.now().getValue();

            if (ziNastere > 31 || lunaNastere > 12 || anNastere < 1900 || anNastere > anCurent) {
                jurnal.warning("Dată naștere invalidă.");
                return;
            }

            String dataNasterii = ziNastere + "/" + lunaNastere + "/" + anNastere;
            creeazaUtilizatorPartial(nume, prenume, dataNasterii);
            App.setRoot("creareContFinal");
            App.getDateUtilizator().put("nume", nume);
            App.getDateUtilizator().put("prenume", prenume);
            App.getDateUtilizator().put("dataNasterii", dataNasterii);
            jurnal.info("Date parțiale utilizator salvate cu succes.");
        } catch (Exception e) {
            jurnal.log(Level.SEVERE, "Eroare în timpul creării contului - pasul 1.", e);
        }
    }

    private User creeazaUtilizatorPartial(String nume, String prenume, String dataNasterii) {
        return new User(nume, prenume, dataNasterii, null, null, null, null, null);
    }

    @FXML
    private void finalizeazaCreareCont() throws IOException {
        try {
            String numeUtilizator = ((javafx.scene.control.TextField) App.scena.lookup(CAMP_NUME_UTILIZATOR)).getText();
            String parola = ((javafx.scene.control.TextField) App.scena.lookup("#parola")).getText();
            String confirmareParola = ((javafx.scene.control.TextField) App.scena.lookup("#confirmareParola")).getText();
            String email = ((javafx.scene.control.TextField) App.scena.lookup("#email")).getText();

            if (numeUtilizator.isEmpty() || parola.isEmpty() || confirmareParola.isEmpty() || email.isEmpty()) {
                Alert alerta = new Alert(Alert.AlertType.WARNING);
                alerta.setTitle(TITLU_ALERTA);
                alerta.setHeaderText("Câmpuri incomplete");
                alerta.setContentText("Toate câmpurile sunt obligatorii.");
                alerta.showAndWait();
                return;
            }

            if (parola.length() < 8) {
                Alert alerta = new Alert(Alert.AlertType.WARNING);
                alerta.setTitle(TITLU_ALERTA);
                alerta.setHeaderText("Parolă prea scurtă");
                alerta.setContentText("Parola trebuie să aibă cel puțin 8 caractere.");
                alerta.showAndWait();
                return;
            }

            if (!parola.equals(confirmareParola)) {
                Alert alerta = new Alert(Alert.AlertType.WARNING);
                alerta.setTitle(TITLU_ALERTA);
                alerta.setHeaderText("Parolele nu coincid");
                alerta.setContentText("Te rog să introduci aceeași parolă în ambele câmpuri.");
                alerta.showAndWait();
                return;
            }

            if (!email.contains("@")) {
                Alert alerta = new Alert(Alert.AlertType.WARNING);
                alerta.setTitle(TITLU_ALERTA);
                alerta.setHeaderText("Email invalid");
                alerta.setContentText("Te rog să introduci un email valid.");
                alerta.showAndWait();
                return;
            }

            User utilizatorNou = new User(
                (String) App.getDateUtilizator().get("nume"),
                (String) App.getDateUtilizator().get("prenume"),
                (String) App.getDateUtilizator().get("dataNasterii"),
                email,
                numeUtilizator,
                parola,
                false, // Implicit, utilizatorul nu este admin
                null, // ID-ul va fi generat de server
                null
            );

            String url = "http://localhost:3000/users/creareCont";
            HttpURLConnection conexiuneHttp = (HttpURLConnection) new URI(url).toURL().openConnection();
            conexiuneHttp.setRequestMethod("POST");
            conexiuneHttp.setRequestProperty("Content-Type", "application/json");
            conexiuneHttp.setDoOutput(true);

            String jsonCerere = String.format(
                "{\"nume\": \"%s\", \"prenume\": \"%s\", \"dataNasterii\": \"%s\", \"email\": \"%s\", \"numeUtilizator\": \"%s\", \"parola\": \"%s\", \"esteAdmin\": %b}",
                utilizatorNou.getNume(), utilizatorNou.getPrenume(), utilizatorNou.getDataNasterii(),
                utilizatorNou.getEmail(), utilizatorNou.getNumeUtilizator(), utilizatorNou.getParola(), utilizatorNou.esteAdmin()
            );

            try (OutputStream os = conexiuneHttp.getOutputStream()) {
                byte[] date = jsonCerere.getBytes(StandardCharsets.UTF_8);
                os.write(date, 0, date.length);
            }

            int codRaspuns = conexiuneHttp.getResponseCode();
            if (codRaspuns == HttpURLConnection.HTTP_OK) {
                jurnal.info("Cont creat cu succes.");
                App.setRoot(PAGINA_LOGIN);
            } else if (jurnal.isLoggable(Level.WARNING)) {
                jurnal.warning(String.format("Eroare la crearea contului. Cod răspuns: %d", codRaspuns));
            }
        } catch (IOException | URISyntaxException e) {
            jurnal.log(Level.SEVERE, "Eroare de conexiune la server.", e);
            App.setRoot("eroareConexiune");
        }
    }
}
