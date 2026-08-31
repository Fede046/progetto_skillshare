package it.unibo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class UtenteDatabaseTest {

    @BeforeAll
    static void setUpDatabase() {
        // Database in memoria per i test: non tocca il file progetto_sweng.db
        DatabaseCore.enableTestMode();
    }

    @AfterAll
    static void tearDownDatabase() {
        DatabaseCore.disableTestMode();
    }

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
        UtenteDTO loginOK = UtenteDatabase.verificaCredenziali(emailUnivoca, passwordValida);
        assertNotNull(loginOK, "Il login con credenziali corrette deve avere successo");

        // --- TEST 2: Errore "User not found" con email inesistente ---
        Exception exceptionNotFound = assertThrows(IllegalArgumentException.class, () -> {
            UtenteDatabase.verificaCredenziali("email.inesistente@unibo.it", passwordValida);
        });
        assertEquals("User not found", exceptionNotFound.getMessage());

        // --- TEST 3: Errore "Wrong password" con password errata ---
        Exception exceptionWrongPwd = assertThrows(IllegalArgumentException.class, () -> {
            UtenteDatabase.verificaCredenziali(emailUnivoca, "PasswordErrata123!");
        });
        assertEquals("Wrong password", exceptionWrongPwd.getMessage());
    }
    @Test
    void testVerificaCredenzialiRestituisceUtente() {
        String emailUnivoca = "test.login_" + System.currentTimeMillis() + "@unibo.it";
        
        // 1. Registriamo un utente
        UtenteDTO utente = new UtenteDTO();
        utente.setNome("Giulia");
        utente.setCognome("Bianchi");
        utente.setEmail(emailUnivoca);
        utente.setPassword("@Password123");
        UtenteDatabase.registra(utente);

        // 2. Verifichiamo che il login restituisca l'UtenteDTO corretto
        UtenteDTO utenteLoggato = UtenteDatabase.verificaCredenziali(emailUnivoca, "@Password123");
        
        assertNotNull(utenteLoggato, "L'utente loggato non deve essere null");
        assertEquals("Giulia", utenteLoggato.getNome(), "Il nome dell'utente deve corrispondere");
        assertEquals(emailUnivoca, utenteLoggato.getEmail(), "L'email deve corrispondere");
    }
    @Test
    void testAggiornaProfiloSuccess() {
        String emailUnivoca = "update.test_" + System.currentTimeMillis() + "@unibo.it";
        UtenteDTO utente = new UtenteDTO(
                emailUnivoca,
                "@Password123",
                "Marco",
                "Verdi",
                "Bio iniziale",
                "https://old.url/pic.png",
                List.of("Java")
        );
        UtenteDatabase.registra(utente);

        // Prepariamo l'oggetto con i dati aggiornati
        UtenteDTO datiModificati = new UtenteDTO();
        datiModificati.setEmail(emailUnivoca);
        datiModificati.setBio("Nuova bio aggiornata");
        datiModificati.setPhotoUrl("https://new.url/avatar.jpg");
        datiModificati.setTagCompetenza(List.of("Java", "GWT", "Docker"));

        UtenteDTO risultato = UtenteDatabase.aggiornaProfilo(datiModificati);

        // Verifiche sui dati aggiornati
        assertNotNull(risultato);
        assertEquals("Nuova bio aggiornata", risultato.getBio());
        assertEquals("https://new.url/avatar.jpg", risultato.getPhotoUrl());
        assertEquals(3, risultato.getTagCompetenza().size());
        assertTrue(risultato.getTagCompetenza().contains("Docker"));

        // Verifica che i dati immutabili e persistiti non siano stati compromessi
        UtenteDTO ricaricatoDalDb = UtenteDatabase.getProfilo(emailUnivoca);
        assertEquals("Marco", ricaricatoDalDb.getNome());
        assertEquals("Verdi", ricaricatoDalDb.getCognome());
        assertEquals("Nuova bio aggiornata", ricaricatoDalDb.getBio());
    }

    @Test
    void testAggiornaProfiloUtenteInesistente() {
        UtenteDTO dati = new UtenteDTO();
        dati.setEmail("inesistente_" + System.currentTimeMillis() + "@unibo.it");

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            UtenteDatabase.aggiornaProfilo(dati);
        });
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void testAggiornaProfiloDatiNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            UtenteDatabase.aggiornaProfilo(null);
        });

        UtenteDTO senzaEmail = new UtenteDTO();
        assertThrows(IllegalArgumentException.class, () -> {
            UtenteDatabase.aggiornaProfilo(senzaEmail);
        });
    }
}