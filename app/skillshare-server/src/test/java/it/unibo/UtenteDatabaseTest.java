package it.unibo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UtenteDatabaseTest {

    @Test
    void testRegistrazioneEControlloDuplicati() {
        // email unica usando il timestamp attuale per evitare conflitti nei test
        String emailUnivoca = "mario.rossi_" + System.currentTimeMillis() + "@unibo.it";

        // 1. Creiamo un DTO con dati validi
        UtenteDTO utente = new UtenteDTO();
        utente.setNome("Mario");
        utente.setCognome("Rossi");
        utente.setEmail(emailUnivoca);
        utente.setPassword("@Password123");

        // 2. Registriamo l'utente
        boolean esito = UtenteDatabase.registra(utente);
        assertTrue(esito, "La registrazione dovrebbe andare a buon fine");

        // 3. Verifichiamo il blocco dei duplicati
        UtenteDTO utenteDuplicato = new UtenteDTO();
        utenteDuplicato.setNome("Luigi");
        utenteDuplicato.setCognome("Verdi");
        utenteDuplicato.setEmail(emailUnivoca);
        utenteDuplicato.setPassword("@Password123");

        boolean esitoDuplicato = UtenteDatabase.registra(utenteDuplicato);
        assertFalse(esitoDuplicato, "La registrazione con email già esistente deve fallire");
    }

    @Test
    void testLoginRiuscitoEErroreCredenziali() {
        // 1.email unica e una password valida
        String emailUnivoca = "login.test_" + System.currentTimeMillis() + "@unibo.it";
        String passwordValida = "@Password123";

        // Registriamo prima l'utente per poter fare il login
        UtenteDTO utente = new UtenteDTO();
        utente.setNome("Anna");
        utente.setCognome("Neri");
        utente.setEmail(emailUnivoca);
        utente.setPassword(passwordValida);
        UtenteDatabase.registra(utente);

        // --- TEST 1: Login riuscito con credenziali corrette ---
        boolean loginOK = UtenteDatabase.verificaCredenziali(emailUnivoca, passwordValida);
        assertTrue(loginOK, "Il login con credenziali corrette deve avere successo");

        // --- TEST 2: Errore "User not found" con email inesistente ---
        Exception exceptionNotFound = assertThrows(IllegalArgumentException.class, () -> {
            UtenteDatabase.verificaCredenziali("email.inesistente@unibo.it", passwordValida);
        });
        assertEquals("Utente non trovato", exceptionNotFound.getMessage());

        // --- TEST 3: Errore "Wrong password" con password errata ---
        Exception exceptionWrongPwd = assertThrows(IllegalArgumentException.class, () -> {
            UtenteDatabase.verificaCredenziali(emailUnivoca, "PasswordErrata123!");
        });
        assertEquals("Password errata", exceptionWrongPwd.getMessage());
    }
}