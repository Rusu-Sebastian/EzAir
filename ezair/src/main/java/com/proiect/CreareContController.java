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
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";

    private App app;

    @FXML private TextField campEmail;
    @FXML private TextField campNumeUtilizator;
    @FXML private TextField campParola;
    @FXML private TextField campConfirmareParola;

    public void initialize() {
        app = App.getInstance();
    }

    @SuppressWarnings("unused")
    @FXML
    private void finalizeazaCreareCont() {
        String email = campEmail.getText();
        String numeUtilizator = campNumeUtilizator.getText();
        String parola = campParola.getText();
        String confirmareParola = campConfirmareParola.getText();

        if (email.isEmpty() || numeUtilizator.isEmpty() || parola.isEmpty() || confirmareParola.isEmpty()) {
            afiseazaAlerta(Alert.AlertType.WARNING,
                          TITLU_ALERTA,
                          "Câmpuri incomplete",
                          "Te rog să completezi toate câmpurile.");
            return;
        }

        if (!parola.equals(confirmareParola)) {
            afiseazaAlerta(Alert.AlertType.WARNING,
                          TITLU_ALERTA,
                          "Parole diferite",
                          "Parolele introduse nu coincid.");
            return;
        }

        if (!email.matches(EMAIL_PATTERN)) {
            afiseazaAlerta(Alert.AlertType.WARNING,
                          TITLU_ALERTA,
                          "Email invalid",
                          "Te rog să introduci un email valid.");
            return;
        }

        try {
            trimiteDateCreareCont(email, numeUtilizator, parola);
        } catch (IOException | URISyntaxException e) {
            jurnal.log(Level.SEVERE, "Eroare la crearea contului", e);
            afiseazaAlerta(Alert.AlertType.ERROR,
                          "Eroare",
                          "Nu s-a putut crea contul",
                          "A apărut o eroare la comunicarea cu serverul. Te rog să încerci din nou.");
        }
    }

    private void trimiteDateCreareCont(String email, String numeUtilizator, String parola) 
            throws IOException, URISyntaxException {
        JSONObject dateUtilizator = new JSONObject();
        dateUtilizator.put("nume", App.getDateUtilizator().get("nume"));
        dateUtilizator.put("prenume", App.getDateUtilizator().get("prenume"));
        dateUtilizator.put("dataNasterii", App.getDateUtilizator().get("dataNasterii"));
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
                app.setRoot(PAGINA_LOGIN);
            } else {
                if (jurnal.isLoggable(Level.WARNING)) {
                    jurnal.warning(String.format("Eroare la crearea contului. Cod răspuns: %d", codRaspuns));
                }
                afiseazaAlerta(Alert.AlertType.ERROR,
                             "Eroare",
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
}
