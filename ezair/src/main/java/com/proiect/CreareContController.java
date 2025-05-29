package com.proiect;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import com.proiect.config.ApiEndpoints;
import com.proiect.util.HttpUtil;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class CreareContController {
    private static final Logger jurnal = Logger.getLogger(CreareContController.class.getName());
    private static final String PAGINA_LOGIN = "login";
    private static final String TITLU_ALERTA = "Atenție";
    private static final String TITLU_EROARE = "Eroare";
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final String CHEIE_NUME = "nume";
    private static final String CHEIE_PRENUME = "prenume";
    private static final String CHEIE_DATA_NASTERII = "dataNasterii";
    private static final String MESAJ_EROARE_NAVIGARE = "Eroare de navigare";

    @FXML private TextField email;
    @FXML private TextField numeUtilizator;
    @FXML private TextField parola;
    @FXML private TextField confirmareParola;
    
    // Câmpuri pentru prima etapă (creareContInceput.fxml) - match FXML fx:id names
    @FXML private TextField nume;
    @FXML private TextField prenume;
    @FXML private TextField ziuaNasterii;
    @FXML private TextField lunaNasterii;
    @FXML private TextField anNasterii;

    @SuppressWarnings("unused")
    @FXML
    private void finalizeazaCreareCont() {
        String emailText = email.getText();
        String numeUtilizatorText = numeUtilizator.getText();
        String parolaText = parola.getText();
        String confirmareParolaText = confirmareParola.getText();

        if (emailText.isEmpty() || numeUtilizatorText.isEmpty() || parolaText.isEmpty() || confirmareParolaText.isEmpty()) {
            afiseazaAlerta(Alert.AlertType.WARNING,
                          TITLU_ALERTA,
                          "Câmpuri incomplete",
                          "Te rog să completezi toate câmpurile.");
            return;
        }

        if (!parolaText.equals(confirmareParolaText)) {
            afiseazaAlerta(Alert.AlertType.WARNING,
                          TITLU_ALERTA,
                          "Parole diferite",
                          "Parolele introduse nu coincid.");
            return;
        }

        if (!emailText.matches(EMAIL_PATTERN)) {
            afiseazaAlerta(Alert.AlertType.WARNING,
                          TITLU_ALERTA,
                          "Email invalid",
                          "Te rog să introduci un email valid.");
            return;
        }

        try {
            trimiteDateCreareCont(emailText, numeUtilizatorText, parolaText);
        } catch (IOException | URISyntaxException e) {
            jurnal.log(Level.SEVERE, "Eroare la crearea contului", e);
            afiseazaAlerta(Alert.AlertType.ERROR,
                          TITLU_EROARE,
                          "Nu s-a putut crea contul",
                          "A apărut o eroare la comunicarea cu serverul. Te rog să încerci din nou.");
        }
    }

    private void trimiteDateCreareCont(String email, String numeUtilizator, String parola) 
            throws IOException, URISyntaxException {
        JSONObject dateUtilizator = new JSONObject();
        dateUtilizator.put(CHEIE_NUME, App.getDateUtilizator().get(CHEIE_NUME));
        dateUtilizator.put(CHEIE_PRENUME, App.getDateUtilizator().get(CHEIE_PRENUME));
        dateUtilizator.put(CHEIE_DATA_NASTERII, App.getDateUtilizator().get(CHEIE_DATA_NASTERII));
        dateUtilizator.put("email", email);
        dateUtilizator.put("numeUtilizator", numeUtilizator);
        dateUtilizator.put("parola", parola);
        dateUtilizator.put("esteAdmin", false);

        HttpURLConnection conexiune = HttpUtil.createJsonConnection(ApiEndpoints.CREATE_ACCOUNT, "POST");
        conexiune.setDoOutput(true);

        try {
            byte[] date = dateUtilizator.toString().getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = conexiune.getOutputStream()) {
                os.write(date);
            }

            int codRaspuns = conexiune.getResponseCode();
            if (codRaspuns == HttpURLConnection.HTTP_OK) {
                jurnal.info("Cont creat cu succes.");
                afiseazaAlerta(Alert.AlertType.INFORMATION,
                             "Succes",
                             "Cont creat cu succes",
                             "Contul tău a fost creat cu succes. Te poți conecta acum.");
                App.setRoot(PAGINA_LOGIN);
            } else {
                if (jurnal.isLoggable(Level.WARNING)) {
                    jurnal.warning(String.format("Eroare la crearea contului. Cod răspuns: %d", codRaspuns));
                }
                afiseazaAlerta(Alert.AlertType.ERROR,
                             TITLU_EROARE,
                             "Eroare la crearea contului",
                             "Te rog să încerci din nou mai târziu.");
            }
        } finally {
            conexiune.disconnect();
        }
    }

    private void afiseazaAlerta(Alert.AlertType tip, String titlu, String antet, String continut) {
        Alert alerta = new Alert(tip);
        alerta.setTitle(titlu);
        alerta.setHeaderText(antet);
        alerta.setContentText(continut);
        alerta.showAndWait();
    }

    @SuppressWarnings("unused")
    @FXML
    private void backCreareContInceput() {
        try {
            App.setRoot(PAGINA_LOGIN);
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, "Eroare la navigarea înapoi la login", e);
            afiseazaAlerta(Alert.AlertType.ERROR,
                          TITLU_EROARE,
                          MESAJ_EROARE_NAVIGARE,
                          "Nu s-a putut naviga înapoi la pagina de login.");
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private void creareContInceput() {
        String numeText = nume.getText();
        String prenumeText = prenume.getText();
        String ziua = ziuaNasterii.getText();
        String luna = lunaNasterii.getText();
        String anul = anNasterii.getText();

        if (numeText.isEmpty() || prenumeText.isEmpty() || ziua.isEmpty() || luna.isEmpty() || anul.isEmpty()) {
            afiseazaAlerta(Alert.AlertType.WARNING,
                          TITLU_ALERTA,
                          "Câmpuri incomplete",
                          "Te rog să completezi toate câmpurile.");
            return;
        }

        // Validare dată nașterii
        try {
            int zi = Integer.parseInt(ziua);
            int lunaInt = Integer.parseInt(luna);
            int an = Integer.parseInt(anul);
            
            if (zi < 1 || zi > 31 || lunaInt < 1 || lunaInt > 12 || an < 1900 || an > 2010) {
                afiseazaAlerta(Alert.AlertType.WARNING,
                              TITLU_ALERTA,
                              "Dată invalidă",
                              "Te rog să introduci o dată de naștere validă.");
                return;
            }
        } catch (NumberFormatException e) {
            afiseazaAlerta(Alert.AlertType.WARNING,
                          TITLU_ALERTA,
                          "Format invalid",
                          "Te rog să introduci numere valide pentru dată.");
            return;
        }

        // Stochează datele pentru următoarea etapă
        App.getDateUtilizator().put(CHEIE_NUME, numeText);
        App.getDateUtilizator().put(CHEIE_PRENUME, prenumeText);
        App.getDateUtilizator().put(CHEIE_DATA_NASTERII, ziua + "/" + luna + "/" + anul);

        try {
            App.setRoot("creareContFinal");
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, "Eroare la navigarea către următoarea etapă", e);
            afiseazaAlerta(Alert.AlertType.ERROR,
                          TITLU_EROARE,
                          MESAJ_EROARE_NAVIGARE,
                          "Nu s-a putut naviga către următoarea etapă.");
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private void cancelCreareCont() {
        try {
            App.setRoot(PAGINA_LOGIN);
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, "Eroare la navigarea înapoi la login", e);
            afiseazaAlerta(Alert.AlertType.ERROR,
                          TITLU_EROARE,
                          MESAJ_EROARE_NAVIGARE,
                          "Nu s-a putut naviga înapoi la pagina de login.");
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private void backCreareContFinal() {
        try {
            App.setRoot("creareContInceput");
        } catch (IOException e) {
            jurnal.log(Level.SEVERE, "Eroare la navigarea înapoi la prima etapă", e);
            afiseazaAlerta(Alert.AlertType.ERROR,
                          TITLU_EROARE,
                          MESAJ_EROARE_NAVIGARE,
                          "Nu s-a putut naviga înapoi la prima etapă.");
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private void creareContFinal() {
        // Delegate to the existing method
        finalizeazaCreareCont();
    }
}
