package it.unibo;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void testGetMessaggiSenzaSessioneLanciaEccezione() {
        // Poiché siamo in un test unitario senza un contesto HTTP attivo
        // (RemoteServiceServlet),
        // getThreadLocalRequest() restituirà null, scatenando l'eccezione di utente non
        // autenticato.
        assertThrows(IllegalArgumentException.class, () -> {
            chatService.getMessaggi(idRichiestaAccettata);
        });
    }
}