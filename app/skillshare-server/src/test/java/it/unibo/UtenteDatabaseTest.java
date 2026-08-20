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
}