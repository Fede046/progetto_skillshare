package it.unibo;

import java.util.concurrent.ConcurrentMap;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;

public class MessaggioDatabaseTest {

    private DB dbTest;
    private MessaggioDatabase messaggioDatabase;

    // DB in memoria per non sporcare il database reale durante i test
    @BeforeEach
    void setUp() {
        dbTest = DBMaker.memoryDB().make();
        messaggioDatabase = new MessaggioDatabase(dbTest);
    }

    @AfterEach
    void tearDown() {
        if (dbTest != null && !dbTest.isClosed()) {
            dbTest.close();
        }
    }

    @Test
    void testSalvaMessaggioValido() {
        // Arrange
        MessaggioDTO messaggio = new MessaggioDTO();
        messaggio.setIdRichiestaScambio("richiesta-123");
        messaggio.setIdMittente("mario.rossi@unibo.it");
        messaggio.setTesto("Ciao, ti scrivo per la richiesta di scambio!");

        // Act
        MessaggioDTO salvato = messaggioDatabase.salva(messaggio);

        // Assert
        assertNotNull(salvato.getId(), "L'id del messaggio deve essere generato");
        assertTrue(salvato.getTimestamp() > 0, "Il timestamp deve essere valorizzato");
        assertEquals("richiesta-123", salvato.getIdRichiestaScambio());
        assertEquals("mario.rossi@unibo.it", salvato.getIdMittente());
        assertEquals("Ciao, ti scrivo per la richiesta di scambio!", salvato.getTesto());

        // Verifica che sia effettivamente persistito nella collezione MapDB "messaggi"
        ConcurrentMap collection = messaggioDatabase.getMessaggiCollection();
        assertNotNull(collection.get(salvato.getId()), "Il messaggio deve essere recuperabile dalla collezione MapDB");
    }

    @Test
    void testCollezioneMessaggiInizializzataCorrettamente() {
        ConcurrentMap collection = messaggioDatabase.getMessaggiCollection();
        assertNotNull(collection, "La collezione 'messaggi' deve essere inizializzata e non nulla");
        assertTrue(collection.isEmpty(), "All'avvio la collezione deve essere vuota");
    }
}