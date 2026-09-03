package it.unibo;


import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;


public class ChatServiceImplTest {


    private DB dbTest;
    private MessaggioDatabase messaggioDatabase;
    private RichiestaScambioDatabase richiestaDatabase;
    private ChatServiceImpl chatService;
    private String idRichiestaAccettata;


    private static final String CREATORE = "mario.rossi@unibo.it";
    private static final String RICHIEDENTE = "luigi.verdi@unibo.it";


    @BeforeEach
    void setUp() {
        dbTest = DBMaker.memoryDB().make();
        messaggioDatabase = new MessaggioDatabase(dbTest);
        richiestaDatabase = new RichiestaScambioDatabase(dbTest);


        // Inizializziamo il servizio iniettando il database di test
        chatService = new ChatServiceImpl(messaggioDatabase);


        // Prepariamo una richiesta di scambio ACCETTATA
        RichiestaScambioDTO req = new RichiestaScambioDTO();
        req.setIdAnnuncio("annuncio-1");
        req.setIdRichiedente(RICHIEDENTE);
        req.setIdCreatoreAnnuncio(CREATORE);
        req = richiestaDatabase.salva(req);
        richiestaDatabase.accetta(req.getId(), CREATORE);
        idRichiestaAccettata = req.getId();
    }


    @AfterEach
    void tearDown() {
        if (dbTest != null && !dbTest.isClosed()) {
            dbTest.close();
        }
    }


    @Test
    void testInviaMessaggioDelegatoCorrettamente() {
        MessaggioDTO msg = new MessaggioDTO();
        msg.setIdRichiestaScambio(idRichiestaAccettata);
        msg.setIdMittente(RICHIEDENTE);
        msg.setTesto("Test invio tramite service");


        MessaggioDTO salvato = chatService.inviaMessaggio(msg);


        assertNotNull(salvato.getId());
        assertEquals("Test invio tramite service", salvato.getTesto());
    }


    @Test
    void testInviaMessaggioRichiestaNullLanciaEccezione() {
        assertThrows(IllegalArgumentException.class, () -> {
            chatService.inviaMessaggio(null);
        });
    }


    @Test
    void testGetMessaggiDelegatoCorrettamente() {
        MessaggioDTO primo = new MessaggioDTO();
        primo.setIdRichiestaScambio(idRichiestaAccettata);
        primo.setIdMittente(RICHIEDENTE);
        primo.setTesto("Ciao, quando ci vediamo?");
        chatService.inviaMessaggio(primo);


        // Ritardo per garantire timestamp diversi
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }


        MessaggioDTO secondo = new MessaggioDTO();
        secondo.setIdRichiestaScambio(idRichiestaAccettata);
        secondo.setIdMittente(CREATORE);
        secondo.setTesto("Direi giovedì pomeriggio");
        chatService.inviaMessaggio(secondo);


        List<MessaggioDTO> messaggi = chatService.getMessaggi(idRichiestaAccettata, RICHIEDENTE);


        assertEquals(2, messaggi.size(), "Deve restituire i due messaggi della chat");
        assertEquals("Ciao, quando ci vediamo?", messaggi.get(0).getTesto(), "Il piu' vecchio va per primo");
    }


    @Test
    void testGetMessaggiComeEstraneoLanciaEccezione() {
        // L'autorizzazione arriva da MessaggioDatabase: la servlet non la rifa'
        assertThrows(IllegalArgumentException.class, () -> {
            chatService.getMessaggi(idRichiestaAccettata, "estraneo@unibo.it");
        });
    }


    @Test
    void testGetMessaggiChatSenzaMessaggiRestituisceListaVuota() {
        List<MessaggioDTO> messaggi = chatService.getMessaggi(idRichiestaAccettata, CREATORE);


        assertNotNull(messaggi, "Deve restituire una lista, non null");
        assertTrue(messaggi.isEmpty(), "Una chat senza messaggi ha lista vuota");
    }
}