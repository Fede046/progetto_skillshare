package it.unibo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;

public class RichiestaScambioServiceImplTest {

    private static final String CREATORE = "mario.rossi@unibo.it";
    private static final String RICHIEDENTE = "luigi.verdi@unibo.it";

    private DB dbTest;
    private AnnuncioDatabase annuncioDatabase;
    private RichiestaScambioDatabase richiesteDatabase;
    private AnnuncioDTO annuncioCreato;
    private RichiestaScambioServiceImpl service;

    // DB in memoria iniettato nel servizio: il singleton DatabaseCore non viene toccato,
    // così gli altri test della suite (UtenteDatabase, ...) non vengono disturbati
    @BeforeEach
    void setUp() {
        dbTest = DBMaker.memoryDB().make();
        annuncioDatabase = new AnnuncioDatabase(dbTest);
        richiesteDatabase = new RichiestaScambioDatabase(dbTest);

        AnnuncioDTO annuncio = new AnnuncioDTO();
        annuncio.setIdUtente(CREATORE);
        annuncio.setTitolo("Ripetizioni di Java");
        annuncio.setDescrizione("Lezioni base e avanzate su Java e GWT");
        annuncio.setCompetenzaOfferta("Programmazione Java");
        annuncio.setDisponibilita("Lunedì e mercoledì pomeriggio");
        annuncio.setControprestazione("Lezioni di inglese");
        annuncioCreato = annuncioDatabase.pubblica(annuncio);

        service = new RichiestaScambioServiceImpl(annuncioDatabase, richiesteDatabase);
    }

    @AfterEach
    void tearDown() {
        if (dbTest != null && !dbTest.isClosed()) {
            dbTest.close();
        }
    }

    @Test
    void testInviaRichiestaValidaSalvataComePENDING() {
        RichiestaScambioDTO salvata = service.inviaRichiestaScambio(
                annuncioCreato.getId(), RICHIEDENTE, "Vorrei scambiare con te");

        // Il servizio risolve il creatore dall'annuncio e il Database forza PENDING
        assertNotNull(salvata.getId(), "L'id deve essere generato");
        assertEquals(annuncioCreato.getId(), salvata.getIdAnnuncio());
        assertEquals(RICHIEDENTE, salvata.getIdRichiedente());
        assertEquals(CREATORE, salvata.getIdCreatoreAnnuncio(), "Il creatore deve essere risolto dall'annuncio");
        assertEquals(StatoRichiesta.PENDING, salvata.getStato(), "Lo stato deve essere PENDING");
        assertTrue(salvata.getDataCreazione() > 0, "La dataCreazione deve essere valorizzata");

        // La richiesta deve essere realmente persistita nel database
        RichiestaScambioDTO ricaricata = richiesteDatabase.getRichiesteCollection().get(salvata.getId());
        assertNotNull(ricaricata, "La richiesta deve essere recuperabile dal database");

        // E visibile sia al creatore (ricevuta) sia al richiedente (inviata)
        List<RichiestaScambioDTO> ricevute = richiesteDatabase.richiesteRicevuteDaCreatore(CREATORE);
        assertEquals(1, ricevute.size(), "Il creatore deve vedere la richiesta ricevuta");
        assertEquals(salvata.getId(), ricevute.get(0).getId());

        List<RichiestaScambioDTO> inviate = richiesteDatabase.richiesteInviateDaRichiedente(RICHIEDENTE);
        assertEquals(1, inviate.size(), "Il richiedente deve vedere la richiesta inviata");
        assertEquals(salvata.getId(), inviate.get(0).getId());
    }

    @Test
    void testInviaRichiestaSulProprioAnnuncioRifiutata() {
        // Il creatore non puo' inviare una richiesta sul proprio annuncio
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            service.inviaRichiestaScambio(annuncioCreato.getId(), CREATORE, "Per me stesso");
        });
        assertEquals("Non puoi inviare una richiesta di scambio sul tuo stesso annuncio", ex.getMessage());

        // Nulla deve essere salvato
        assertTrue(richiesteDatabase.getRichiesteCollection().isEmpty(), "Non deve salvare nulla");
    }

    @Test
    void testInviaRichiestaAnnuncioInesistente() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            service.inviaRichiestaScambio("id-inesistente", RICHIEDENTE, "Ciao");
        });
        assertEquals("Annuncio non trovato", ex.getMessage());
        assertTrue(richiesteDatabase.getRichiesteCollection().isEmpty(), "Non deve salvare nulla");
    }

    @Test
    void testInviaRichiestaSenzaIdAnnuncio() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            service.inviaRichiestaScambio(null, RICHIEDENTE, "Ciao");
        });
        assertEquals("Dati non validi", ex.getMessage());
    }

    @Test
    void testInviaRichiestaSenzaIdRichiedente() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            service.inviaRichiestaScambio(annuncioCreato.getId(), "   ", "Ciao");
        });
        assertEquals("Dati non validi", ex.getMessage());
    }

    @Test
    void testMessaggioNullAccettato() {
        RichiestaScambioDTO salvata = service.inviaRichiestaScambio(
                annuncioCreato.getId(), RICHIEDENTE, null);

        assertNotNull(salvata.getId());
        assertEquals(StatoRichiesta.PENDING, salvata.getStato());
        assertNull(salvata.getMessaggio(), "Il messaggio opzionale può essere null");
    }

    // --- Delega di liste, accetta e rifiuta ---

    @Test
    void testRichiesteRicevuteDaCreatoreViaServizio() {
        RichiestaScambioDTO salvata = service.inviaRichiestaScambio(
                annuncioCreato.getId(), RICHIEDENTE, "Vorrei scambiare con te");

        List<RichiestaScambioDTO> ricevute = service.richiesteRicevuteDaCreatore(CREATORE);

        assertEquals(1, ricevute.size(), "Il creatore deve vedere la richiesta ricevuta");
        assertEquals(salvata.getId(), ricevute.get(0).getId());

        // Il creatore non ha inviato nessuna richiesta: la delega non confonde le due letture
        assertTrue(service.richiesteInviateDaRichiedente(CREATORE).isEmpty(),
                "Il creatore non deve apparire tra i richiedenti");
    }

    @Test
    void testRichiesteInviateDaRichiedenteViaServizio() {
        RichiestaScambioDTO salvata = service.inviaRichiestaScambio(
                annuncioCreato.getId(), RICHIEDENTE, "Vorrei scambiare con te");

        List<RichiestaScambioDTO> inviate = service.richiesteInviateDaRichiedente(RICHIEDENTE);

        assertEquals(1, inviate.size(), "Il richiedente deve vedere la richiesta inviata");
        assertEquals(salvata.getId(), inviate.get(0).getId());

        // Il richiedente non ha ricevuto nessuna richiesta: la delega non confonde le due letture
        assertTrue(service.richiesteRicevuteDaCreatore(RICHIEDENTE).isEmpty(),
                "Il richiedente non deve apparire tra i creatori");
    }

    @Test
    void testAccettaViaServizioDelegaAlDatabase() {
        RichiestaScambioDTO salvata = service.inviaRichiestaScambio(
                annuncioCreato.getId(), RICHIEDENTE, "Vorrei scambiare con te");

        RichiestaScambioDTO accettata = service.accetta(salvata.getId(), CREATORE);

        assertEquals(StatoRichiesta.ACCEPTED, accettata.getStato(), "accetta() deve delegare ad ACCEPTED");
    }

    @Test
    void testRifiutaViaServizioDelegaAlDatabase() {
        RichiestaScambioDTO salvata = service.inviaRichiestaScambio(
                annuncioCreato.getId(), RICHIEDENTE, "Vorrei scambiare con te");

        RichiestaScambioDTO rifiutata = service.rifiuta(salvata.getId(), CREATORE);

        assertEquals(StatoRichiesta.REJECTED, rifiutata.getStato(), "rifiuta() deve delegare a REJECTED");
    }

    @Test
    void testAccettaViaServizioPropagaEccezioneNonCreatore() {
        RichiestaScambioDTO salvata = service.inviaRichiestaScambio(
                annuncioCreato.getId(), RICHIEDENTE, "Vorrei scambiare con te");

        // L'eccezione di ownership del Database deve risalire fino al chiamante del servizio
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            service.accetta(salvata.getId(), "anna.bianchi@unibo.it");
        });
        assertEquals("Non autorizzato: solo il creatore dell'annuncio può accettare la richiesta", ex.getMessage());
    }

    @Test
    void testRifiutaViaServizioPropagaEccezioneNonCreatore() {
        RichiestaScambioDTO salvata = service.inviaRichiestaScambio(
                annuncioCreato.getId(), RICHIEDENTE, "Vorrei scambiare con te");

        // L'eccezione di ownership del Database deve risalire fino al chiamante del servizio
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            service.rifiuta(salvata.getId(), "anna.bianchi@unibo.it");
        });
        assertEquals("Non autorizzato: solo il creatore dell'annuncio può rifiutare la richiesta", ex.getMessage());
    }
}

