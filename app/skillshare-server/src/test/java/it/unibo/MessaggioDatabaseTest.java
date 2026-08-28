package it.unibo;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;

public class MessaggioDatabaseTest {

    private DB dbTest;
    private MessaggioDatabase messaggioDatabase;
    private RichiestaScambioDatabase richiestaDatabase;
    private String idRichiestaAccettata;
    private String idRichiestaPending;

    private static final String CREATORE = "mario.rossi@unibo.it";
    private static final String RICHIEDENTE = "luigi.verdi@unibo.it";
    private static final String INTRUSO = "intruso@unibo.it";

    @BeforeEach
    void setUp() {
        dbTest = DBMaker.memoryDB().make();
        messaggioDatabase = new MessaggioDatabase(dbTest);
        richiestaDatabase = new RichiestaScambioDatabase(dbTest);

        // Prepariamo una richiesta di scambio ACCEPTED per i test di invio messaggi
        RichiestaScambioDTO reqAccettata = new RichiestaScambioDTO();
        reqAccettata.setIdAnnuncio("annuncio-1");
        reqAccettata.setIdRichiedente(RICHIEDENTE);
        reqAccettata.setIdCreatoreAnnuncio(CREATORE);
        reqAccettata = richiestaDatabase.salva(reqAccettata);
        richiestaDatabase.accetta(reqAccettata.getId(), CREATORE);
        idRichiestaAccettata = reqAccettata.getId();

        // Prepariamo una richiesta di scambio PENDING (non ancora accettata)
        RichiestaScambioDTO reqPending = new RichiestaScambioDTO();
        reqPending.setIdAnnuncio("annuncio-2");
        reqPending.setIdRichiedente(RICHIEDENTE);
        reqPending.setIdCreatoreAnnuncio(CREATORE);
        reqPending = richiestaDatabase.salva(reqPending);
        idRichiestaPending = reqPending.getId();
    }

    @AfterEach
    void tearDown() {
        if (dbTest != null && !dbTest.isClosed()) {
            dbTest.close();
        }
    }

    @Test
    void testInviaMessaggioSuccessoEGetMessaggiOrdinati() {
        // Invio primo messaggio dal richiedente
        MessaggioDTO m1 = new MessaggioDTO();
        m1.setIdRichiestaScambio(idRichiestaAccettata);
        m1.setIdMittente(RICHIEDENTE);
        m1.setTesto("Ciao Mario, concordiamo per le ripetizioni?");
        messaggioDatabase.inviaMessaggio(m1);

        // Piccolo sleep per garantire timestamp differenti e verificabili
        // nell'ordinamento
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Invio secondo messaggio dal creatore
        MessaggioDTO m2 = new MessaggioDTO();
        m2.setIdRichiestaScambio(idRichiestaAccettata);
        m2.setIdMittente(CREATORE);
        m2.setTesto("Certamente Luigi! Per me va benissimo lunedì.");
        messaggioDatabase.inviaMessaggio(m2);

        // Verifica recupero messaggi da parte di un partecipante (ordinati per
        // timestamp ascendente)
        List<MessaggioDTO> messaggi = messaggioDatabase.getMessaggi(idRichiestaAccettata, RICHIEDENTE);

        assertEquals(2, messaggi.size(), "Dovrebbero esserci esattamente 2 messaggi");
        assertEquals("Ciao Mario, concordiamo per le ripetizioni?", ((MessaggioDTO) messaggi.get(0)).getTesto(),
                "Il primo messaggio deve essere il più vecchio");
        assertEquals("Certamente Luigi! Per me va benissimo lunedì.", ((MessaggioDTO) messaggi.get(1)).getTesto(),
                "Il secondo messaggio deve essere il più recente");
        assertTrue(((MessaggioDTO) messaggi.get(0)).getTimestamp() <= ((MessaggioDTO) messaggi.get(1)).getTimestamp(),
                "I timestamp devono essere in ordine ascendente");
    }

    @Test
    void testInviaMessaggioFallisceSeRichiestaNonAccettata() {
        // Tentativo di inviare un messaggio su una richiesta in stato PENDING
        MessaggioDTO m = new MessaggioDTO();
        m.setIdRichiestaScambio(idRichiestaPending);
        m.setIdMittente(RICHIEDENTE);
        m.setTesto("Ci sei?");

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            messaggioDatabase.inviaMessaggio(m);
        });
        assertEquals("Impossibile inviare messaggi se la richiesta non è stata accettata", ex.getMessage());
    }

    @Test
    void testInviaMessaggioFallisceSeMittenteNonPartecipante() {
        // Tentativo di inviare un messaggio da parte di un utente estraneo alla
        // richiesta
        MessaggioDTO m = new MessaggioDTO();
        m.setIdRichiestaScambio(idRichiestaAccettata);
        m.setIdMittente(INTRUSO);
        m.setTesto("Mi intrometto nella chat!");

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            messaggioDatabase.inviaMessaggio(m);
        });
        assertEquals("L'utente non è autorizzato a inviare messaggi per questa richiesta", ex.getMessage());
    }

    @Test
    void testGetMessaggiFallisceSeUtenteNonPartecipante() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            messaggioDatabase.getMessaggi(idRichiestaAccettata, INTRUSO);
        });
        assertEquals("Non autorizzato a visualizzare i messaggi di questa chat", ex.getMessage());
    }
}