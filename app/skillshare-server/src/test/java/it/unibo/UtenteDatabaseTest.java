package it.unibo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UtenteDatabaseTest {

    @Test
    void testRegistrazioneEControlloDuplicati() {
        // 1. Creiamo un DTO con dati validi
        UtenteDTO utente = new UtenteDTO();
        utente.setNome("Mario");
        utente.setCognome("Rossi");
        utente.setEmail("mario.rossi@unibo.it");
        utente.setPassword("password123");

        // 2. Registriamo l'utente
        boolean esito = UtenteDatabase.registra(utente);
        assertTrue(esito, "La registrazione dovrebbe andare a buon fine");

        // 3. Verifichiamo il blocco dei duplicati
        UtenteDTO utenteDuplicato = new UtenteDTO();
        utenteDuplicato.setNome("Luigi");
        utenteDuplicato.setCognome("Verdi");
        utenteDuplicato.setEmail("mario.rossi@unibo.it"); // Stessa email
        utenteDuplicato.setPassword("altrapassword123");

        boolean esitoDuplicato = UtenteDatabase.registra(utenteDuplicato);
        assertFalse(esitoDuplicato, "La registrazione con email già esistente deve fallire");
    }
}