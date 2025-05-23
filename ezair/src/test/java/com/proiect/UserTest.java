package com.proiect;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    private static final String NUME = "Ion";
    private static final String PRENUME = "Popescu";
    private static final String DATA_NASTERII = "01/01/1990";
    private static final String EMAIL = "ion.popescu@example.com";
    private static final String NUME_UTILIZATOR = "ionpopescu";
    private static final String PAROLA = "parola123";
    private static final String ID = "12345";
    private static final String TELEFON = "0712345678";
    
    private User user;
    private User userAdmin;

    @BeforeEach
    void setUp() {
        // Create a regular user
        user = new User(NUME, PRENUME, DATA_NASTERII, EMAIL, NUME_UTILIZATOR, PAROLA, ID, TELEFON);
        
        // Create an admin user
        userAdmin = new User(NUME, PRENUME, DATA_NASTERII, EMAIL, NUME_UTILIZATOR, PAROLA, true, ID, TELEFON);
    }

    @Test
    void testConstructorNonAdmin() {
        assertEquals(NUME, user.getNume(), "Numele nu corespunde");
        assertEquals(PRENUME, user.getPrenume(), "Prenumele nu corespunde");
        assertEquals(DATA_NASTERII, user.getDataNasterii(), "Data nașterii nu corespunde");
        assertEquals(EMAIL, user.getEmail(), "Email-ul nu corespunde");
        assertEquals(NUME_UTILIZATOR, user.getNumeUtilizator(), "Numele de utilizator nu corespunde");
        assertEquals(PAROLA, user.getParola(), "Parola nu corespunde");
        assertEquals(ID, user.getId(), "ID-ul nu corespunde");
        assertEquals(TELEFON, user.getTelefon(), "Telefonul nu corespunde");
        assertFalse(user.getEsteAdmin(), "Utilizatorul nu ar trebui să fie admin");
    }

    @Test
    void testConstructorAdmin() {
        assertEquals(NUME, userAdmin.getNume(), "Numele nu corespunde");
        assertEquals(PRENUME, userAdmin.getPrenume(), "Prenumele nu corespunde");
        assertEquals(DATA_NASTERII, userAdmin.getDataNasterii(), "Data nașterii nu corespunde");
        assertEquals(EMAIL, userAdmin.getEmail(), "Email-ul nu corespunde");
        assertEquals(NUME_UTILIZATOR, userAdmin.getNumeUtilizator(), "Numele de utilizator nu corespunde");
        assertEquals(PAROLA, userAdmin.getParola(), "Parola nu corespunde");
        assertEquals(ID, userAdmin.getId(), "ID-ul nu corespunde");
        assertEquals(TELEFON, userAdmin.getTelefon(), "Telefonul nu corespunde");
        assertTrue(userAdmin.getEsteAdmin(), "Utilizatorul ar trebui să fie admin");
    }

    @Test
    void testConstructorNullValues() {
        User userNullValues = new User(null, null, null, null, null, null, null, null);
        
        assertNull(userNullValues.getNume(), "Numele ar trebui să fie null");
        assertNull(userNullValues.getPrenume(), "Prenumele ar trebui să fie null");
        assertNull(userNullValues.getDataNasterii(), "Data nașterii ar trebui să fie null");
        assertNull(userNullValues.getEmail(), "Email-ul ar trebui să fie null");
        assertNull(userNullValues.getNumeUtilizator(), "Numele de utilizator ar trebui să fie null");
        assertNull(userNullValues.getParola(), "Parola ar trebui să fie null");
        assertNull(userNullValues.getId(), "ID-ul ar trebui să fie null");
        assertNull(userNullValues.getTelefon(), "Telefonul ar trebui să fie null");
        assertFalse(userNullValues.getEsteAdmin(), "Un utilizator nou nu ar trebui să fie admin");
    }
}
