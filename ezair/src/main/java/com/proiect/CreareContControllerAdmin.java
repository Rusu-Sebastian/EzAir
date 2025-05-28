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
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

public class CreareContControllerAdmin {

    private static final Logger jurnal = Logger.getLogger(CreareContControllerAdmin.class.getName());
    private static final String TITLU_ALERTA = "Atenție";
    private static final String PAGINA_USERI_ADMIN = "paginaUseriAdmin";
    private static final String CAMP_ZI_NASTERE = "#ziuaNasterii";
    private static final String CAMP_LUNA_NASTERE = "#lunaNasterii";
    private static final String CAMP_AN_NASTERE = "#anNasterii";
    private static final String CAMP_NUME_UTILIZATOR = "#numeUtilizator";
    private static final String CHEIE_ID_UTILIZATOR = "userId";
    private static final String CHEIE_ADMIN = "esteAdmin";
    private App app;

    @FXML
    @SuppressWarnings("unused") // Used by FXML
    public void initialize() {
        app = App.getInstance();
    }

    @FXML
    @SuppressWarnings("unused") // Used by FXML
    private void backCreareContInceput() throws IOException {
        jurnal.info("Navigare înapoi la pagina de utilizatori.");
        
        // Păstrăm credențialele de admin înainte de a naviga înapoi
        String userId = App.getDateUtilizator().get(CHEIE_ID_UTILIZATOR);
        String esteAdmin = App.getDateUtilizator().get(CHEIE_ADMIN);
        
        // Curățăm datele vechi dar păstrăm datele de autentificare
        App.getDateUtilizator().clear();
        
        // Restaurăm credențialele
        if (userId != null) {
            App.getDateUtilizator().put(CHEIE_ID_UTILIZATOR, userId);
        }
        if (esteAdmin != null) {
            App.getDateUtilizator().put(CHEIE_ADMIN, esteAdmin);
        }
        
        App.setRoot(PAGINA_USERI_ADMIN);
    }

    @FXML
    private void backCreareContFinal() throws IOException {
        jurnal.info("Navigare înapoi la pagina creareContInceput.");
        App.setRoot("creareContInceputAdmin");
    }

    @FXML
    private void cancelCreareCont() throws IOException {
        jurnal.info("Anulare creare cont și navigare înapoi la pagina de utilizatori.");
        
        // Păstrăm credențialele de admin înainte de a naviga înapoi
        String userId = App.getDateUtilizator().get(CHEIE_ID_UTILIZATOR);
        String esteAdmin = App.getDateUtilizator().get(CHEIE_ADMIN);
        
        // Curățăm datele vechi dar păstrăm datele de autentificare
        App.getDateUtilizator().clear();
        
        // Restaurăm credențialele
        if (userId != null) {
            App.getDateUtilizator().put(CHEIE_ID_UTILIZATOR, userId);
        }
        if (esteAdmin != null) {
            App.getDateUtilizator().put(CHEIE_ADMIN, esteAdmin);
        }
        
        App.setRoot(PAGINA_USERI_ADMIN);
    }

    @FXML
    private void creareContInceput() throws IOException {
        try {
            String nume = ((TextField) App.scena.lookup("#nume")).getText();
            String prenume = ((TextField) App.scena.lookup("#prenume")).getText();

            if (!valideazaCampuriObligatorii(nume, prenume)) {
                return;
            }

            if (!valideazaFormatDataNastere()) {
                return;
            }

            String dataNasterii = creeazaDataNasterii();
            if (dataNasterii == null) {
                return;
            }

            salveazaDateUtilizator(nume, prenume, dataNasterii);
            jurnal.info("Datele inițiale ale utilizatorului au fost salvate în sistem.");
            App.setRoot("creareContFinalAdmin");
        } catch (Exception e) {
            jurnal.log(Level.SEVERE, "A apărut o eroare în prima etapă de creare a contului.", e);
        }
    }

    private boolean valideazaCampuriObligatorii(String nume, String prenume) {
        if (nume.isEmpty() || prenume.isEmpty() ||
            ((TextField) App.scena.lookup(CAMP_ZI_NASTERE)).getText().isEmpty() ||
            ((TextField) App.scena.lookup(CAMP_LUNA_NASTERE)).getText().isEmpty() ||
            ((TextField) App.scena.lookup(CAMP_AN_NASTERE)).getText().isEmpty()) {
            jurnal.warning("Toate câmpurile sunt obligatorii.");
            return false;
        }
        return true;
    }

    private boolean valideazaFormatDataNastere() {
        if (((TextField) App.scena.lookup(CAMP_ZI_NASTERE)).getText().length() > 2 ||
            ((TextField) App.scena.lookup(CAMP_LUNA_NASTERE)).getText().length() > 2 ||
            ((TextField) App.scena.lookup(CAMP_AN_NASTERE)).getText().length() > 4) {
            jurnal.warning("Format dată naștere invalid.");
            return false;
        }
        return true;
    }

    private String creeazaDataNasterii() {
        try {
            int ziNastere = Integer.parseInt(((TextField) App.scena.lookup(CAMP_ZI_NASTERE)).getText());
            int lunaNastere = Integer.parseInt(((TextField) App.scena.lookup(CAMP_LUNA_NASTERE)).getText());
            int anNastere = Integer.parseInt(((TextField) App.scena.lookup(CAMP_AN_NASTERE)).getText());
            int anCurent = java.time.Year.now().getValue();

            if (ziNastere > 31 || lunaNastere > 12 || anNastere < 1900 || anNastere > anCurent) {
                jurnal.warning("Dată naștere invalidă.");
                return null;
            }

            return String.format("%d/%d/%d", ziNastere, lunaNastere, anNastere);
        } catch (NumberFormatException e) {
            jurnal.warning("Format numeric invalid pentru data nașterii.");
            return null;
        }
    }

    private void salveazaDateUtilizator(String nume, String prenume, String dataNasterii) {
        creeazaUtilizatorPartial(nume, prenume, dataNasterii);
        App.getDateUtilizator().put("nume", nume);
        App.getDateUtilizator().put("prenume", prenume);
        App.getDateUtilizator().put("dataNasterii", dataNasterii);
    }

    private User creeazaUtilizatorPartial(String nume, String prenume, String dataNasterii) {
        return new User(nume, prenume, dataNasterii, null, null, null, false, null, null);
    }

    @FXML
    private void creareContFinal() throws IOException {
        try {
            TextField campNumeUtilizator = (TextField) App.scena.lookup(CAMP_NUME_UTILIZATOR);
            javafx.scene.control.PasswordField campParola = (javafx.scene.control.PasswordField) App.scena.lookup("#parola");
            javafx.scene.control.PasswordField campConfirmareParola = (javafx.scene.control.PasswordField) App.scena.lookup("#confirmareParola");
            TextField campEmail = (TextField) App.scena.lookup("#email");
            CheckBox campAdmin = (CheckBox) App.scena.lookup("#checkAdmin");

            if (!valideazaCampuriFinale(campNumeUtilizator, campParola, campConfirmareParola, campEmail)) {
                return;
            }

            User utilizatorNou = creeazaUtilizatorFinal(
                campNumeUtilizator.getText(),
                campParola.getText(),
                campEmail.getText(),
                campAdmin.isSelected()
            );

            trimiteCreareUtilizator(utilizatorNou);
        } catch (IOException | URISyntaxException e) {
            jurnal.log(Level.SEVERE, "Nu s-a putut stabili conexiunea cu serverul.", e);
            App.setRoot("eroareConexiune");
        }
    }

    private boolean valideazaCampuriFinale(TextField campNumeUtilizator, javafx.scene.control.PasswordField campParola, 
                                         javafx.scene.control.PasswordField campConfirmareParola, TextField campEmail) {
        String numeUtilizator = campNumeUtilizator.getText();
        String parola = campParola.getText();
        String confirmareParola = campConfirmareParola.getText();
        String email = campEmail.getText();

        if (numeUtilizator.isEmpty() || parola.isEmpty() || confirmareParola.isEmpty() || email.isEmpty()) {
            afiseazaAlerta("Câmpuri incomplete", "Toate câmpurile sunt obligatorii.");
            return false;
        }

        if (parola.length() < 8) {
            afiseazaAlerta("Parolă prea scurtă", "Parola trebuie să aibă cel puțin 8 caractere.");
            return false;
        }

        if (!parola.equals(confirmareParola)) {
            afiseazaAlerta("Parolele nu coincid", "Te rog să introduci aceeași parolă în ambele câmpuri.");
            return false;
        }

        if (!email.contains("@")) {
            afiseazaAlerta("Email invalid", "Te rog să introduci un email valid.");
            return false;
        }

        return true;
    }

    private void afiseazaAlerta(String header, String content) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(TITLU_ALERTA);
        alerta.setHeaderText(header);
        alerta.setContentText(content);
        alerta.showAndWait();
    }

    private User creeazaUtilizatorFinal(String numeUtilizator, String parola, String email, boolean esteAdmin) {
        return new User(
            (String) App.getDateUtilizator().get("nume"),
            (String) App.getDateUtilizator().get("prenume"),
            (String) App.getDateUtilizator().get("dataNasterii"),
            email,
            numeUtilizator,
            parola,
            esteAdmin,
            null,
            null
        );
    }

    private void trimiteCreareUtilizator(User utilizatorNou) throws IOException, URISyntaxException {
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
            jurnal.info("Cont nou creat și adăugat cu succes.");
            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Succes");
            alerta.setHeaderText("Cont creat cu succes");
            alerta.setContentText("Contul a fost creat cu succes și a fost adăugat la baza de date.");
            alerta.showAndWait();
            
            // Păstrăm credențialele de admin înainte de a naviga înapoi
            String userId = App.getDateUtilizator().get(CHEIE_ID_UTILIZATOR);
            String esteAdmin = App.getDateUtilizator().get(CHEIE_ADMIN);
            
            // Curățăm datele vechi dar păstrăm datele de autentificare
            App.getDateUtilizator().clear();
            
            // Restaurăm credențialele
            if (userId != null) {
                App.getDateUtilizator().put(CHEIE_ID_UTILIZATOR, userId);
            }
            if (esteAdmin != null) {
                App.getDateUtilizator().put(CHEIE_ADMIN, esteAdmin);
            }
            
            App.setRoot(PAGINA_USERI_ADMIN);
        } else if (jurnal.isLoggable(Level.WARNING)) {
            jurnal.warning(String.format("Nu s-a putut crea contul. Cod răspuns server: %d", codRaspuns));
            afiseazaAlerta("Eroare de server", "Nu s-a putut crea contul. Te rugăm să încerci din nou mai târziu.");
        }
    }
}
