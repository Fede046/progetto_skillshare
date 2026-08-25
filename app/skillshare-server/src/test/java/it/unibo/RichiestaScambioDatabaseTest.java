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

public class RichiestaScambioDatabaseTest {

    private DB dbTest;
    private RichiestaScambioDatabase richiestaDatabase;

    // DB in memoria per non sporcare il database reale
    @BeforeEach
    void setUp() {
        dbTest = DBMaker.memoryDB().make();
        richiestaDatabase = new RichiestaScambioDatabase(dbTest);
    }

    @AfterEach
    void tearDown() {
        if (dbTest != null && !dbTest.isClosed()) {
            dbTest.close();
        }
    }

    // Richiesta con tutti i campi validi, da modificare nei singoli test
    private RichiestaScambioDTO creaRichiestaValida() {
        RichiestaScambioDTO richiesta = new RichiestaScambioDTO();
        richiesta.setIdAnnuncio("annuncio-1");
        richiesta.setIdRichiedente("luigi.verdi@unibo.it");
        richiesta.setIdCreatoreAnnuncio("mario.rossi@unibo.it");
        richiesta.setMessaggio("Vorrei scambiare con te");
        return richiesta;
    }

    @Test
    void testSalvaRichiestaConStatoPENDING() {
        RichiestaScambioDTO salvata = richiestaDatabase.salva(creaRichiestaValida());

        assertNotNull(salvata.getId(), "L'id deve essere generato");
        assertEquals(StatoRichiesta.PENDING, salvata.getStato(), "Lo stato deve essere PENDING");
        assertTrue(salvata.getDataCreazione() > 0, "La dataCreazione deve essere valorizzata");

        // La richiesta deve essere rileggibile dal database
        RichiestaScambioDTO ricaricata = richiestaDatabase.getRichiesteCollection().get(salvata.getId());
        assertNotNull(ricaricata);
        assertEquals("annuncio-1", ricaricata.getIdAnnuncio());
        assertEquals("luigi.verdi@unibo.it", ricaricata.getIdRichiedente());
        assertEquals("mario.rossi@unibo.it", ricaricata.getIdCreatoreAnnuncio());
        assertEquals("Vorrei scambiare con te", ricaricata.getMessaggio());
        assertEquals(StatoRichiesta.PENDING, ricaricata.getStato());
    }

    @Test
    void testSalvaRichiestaSenzaMessaggio() {
        RichiestaScambioDTO richiesta = creaRichiestaValida();
        richiesta.setMessaggio(null);

        RichiestaScambioDTO salvata = richiestaDatabase.salva(richiesta);

        assertNotNull(salvata.getId(), "L'id deve essere generato");
        assertEquals(StatoRichiesta.PENDING, salvata.getStato(), "Lo stato deve essere PENDING");
        // Il messaggio è opzionale: null è un percorso valido
        assertNull(salvata.getMessaggio(), "Il messaggio opzionale può essere null");
    }

    @Test
    void testSalvaIgnoraStatoInIngresso() {
        RichiestaScambioDTO richiesta = creaRichiestaValida();
        richiesta.setStato(StatoRichiesta.ACCEPTED); // stato in ingresso da ignorare

        RichiestaScambioDTO salvata = richiestaDatabase.salva(richiesta);

        assertEquals(StatoRichiesta.PENDING, salvata.getStato(), "salva() deve forzare PENDING");
    }

    @Test
    void testSalvaSenzaIdAnnuncio() {
        RichiestaScambioDTO richiesta = creaRichiestaValida();
        richiesta.setIdAnnuncio(null);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            richiestaDatabase.salva(richiesta);
        });
        assertEquals("Il campo 'id annuncio' è obbligatorio", ex.getMessage());
        assertTrue(richiestaDatabase.getRichiesteCollection().isEmpty(), "Non deve salvare nulla");
    }

    @Test
    void testSalvaSenzaIdRichiedente() {
        RichiestaScambioDTO richiesta = creaRichiestaValida();
        richiesta.setIdRichiedente("   ");

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            richiestaDatabase.salva(richiesta);
        });
        assertEquals("Il campo 'id richiedente' è obbligatorio", ex.getMessage());
        assertTrue(richiestaDatabase.getRichiesteCollection().isEmpty(), "Non deve salvare nulla");
    }

    @Test
    void testSalvaSenzaIdCreatore() {
        RichiestaScambioDTO richiesta = creaRichiestaValida();
        richiesta.setIdCreatoreAnnuncio(null);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            richiestaDatabase.salva(richiesta);
        });
        assertEquals("Il campo 'id creatore annuncio' è obbligatorio", ex.getMessage());
        assertTrue(richiestaDatabase.getRichiesteCollection().isEmpty(), "Non deve salvare nulla");
    }

    @Test
    void testRichiesteRicevuteDaCreatore() {
        RichiestaScambioDTO richiesta = creaRichiestaValida();
        richiestaDatabase.salva(richiesta);

        List<RichiestaScambioDTO> ricevute = richiestaDatabase.richiesteRicevuteDaCreatore("mario.rossi@unibo.it");

        assertEquals(1, ricevute.size(), "Il creatore deve ricevere la richiesta");
        assertEquals(richiesta.getId(), ricevute.get(0).getId());
        assertEquals(StatoRichiesta.PENDING, ricevute.get(0).getStato());
    }

    @Test
    void testRichiesteInviateDaRichiedente() {
        RichiestaScambioDTO richiesta = creaRichiestaValida();
        richiestaDatabase.salva(richiesta);

        List<RichiestaScambioDTO> inviate = richiestaDatabase.richiesteInviateDaRichiedente("luigi.verdi@unibo.it");

        assertEquals(1, inviate.size(), "Il richiedente deve vedere la sua richiesta");
        assertEquals(richiesta.getId(), inviate.get(0).getId());
        assertEquals(StatoRichiesta.PENDING, inviate.get(0).getStato());
    }

    @Test
    void testRichiesteRicevuteFiltratePerCreatore() {
        // Mario riceve la richiesta di Luigi
        RichiestaScambioDTO aMario = creaRichiestaValida();
        aMario.setIdCreatoreAnnuncio("mario.rossi@unibo.it");
        richiestaDatabase.salva(aMario);

        // Anna riceve la richiesta di Luigi sul suo annuncio
        RichiestaScambioDTO adAnna = creaRichiestaValida();
        adAnna.setIdCreatoreAnnuncio("anna.bianchi@unibo.it");
        richiestaDatabase.salva(adAnna);

        List<RichiestaScambioDTO> ricevuteDaMario = richiestaDatabase.richiesteRicevuteDaCreatore("mario.rossi@unibo.it");

        assertEquals(1, ricevuteDaMario.size(), "Mario deve vedere solo le sue richieste");
        assertEquals("mario.rossi@unibo.it", ricevuteDaMario.get(0).getIdCreatoreAnnuncio());
    }

    @Test
    void testRichiesteInviateFiltratePerRichiedente() {
        RichiestaScambioDTO diLuigi = creaRichiestaValida();
        diLuigi.setIdRichiedente("luigi.verdi@unibo.it");
        richiestaDatabase.salva(diLuigi);

        RichiestaScambioDTO diPaola = creaRichiestaValida();
        diPaola.setIdRichiedente("paola.neri@unibo.it");
        richiestaDatabase.salva(diPaola);

        List<RichiestaScambioDTO> inviateDaLuigi = richiestaDatabase.richiesteInviateDaRichiedente("luigi.verdi@unibo.it");

        assertEquals(1, inviateDaLuigi.size(), "Luigi deve vedere solo le sue richieste");
        assertEquals("luigi.verdi@unibo.it", inviateDaLuigi.get(0).getIdRichiedente());
    }

    @Test
    void testCreatoreSenzaRichiesteRestituisceListaVuota() {
        RichiestaScambioDTO richiesta = creaRichiestaValida();
        richiestaDatabase.salva(richiesta);

        List<RichiestaScambioDTO> risultato = richiestaDatabase.richiesteRicevuteDaCreatore("nessuno@unibo.it");

        assertNotNull(risultato, "Deve restituire una lista, non null");
        assertTrue(risultato.isEmpty(), "Un creatore senza richieste ha lista vuota");
    }

    @Test
    void testRichiedenteSenzaRichiesteRestituisceListaVuota() {
        RichiestaScambioDTO richiesta = creaRichiestaValida();
        richiestaDatabase.salva(richiesta);

        List<RichiestaScambioDTO> risultato = richiestaDatabase.richiesteInviateDaRichiedente("nessuno@unibo.it");

        assertNotNull(risultato, "Deve restituire una lista, non null");
        assertTrue(risultato.isEmpty(), "Un richiedente senza richieste ha lista vuota");
    }

    @Test
    void testIdNullOVuotoRestituisceListaVuota() {
        richiestaDatabase.salva(creaRichiestaValida());

        assertTrue(richiestaDatabase.richiesteRicevuteDaCreatore(null).isEmpty());
        assertTrue(richiestaDatabase.richiesteRicevuteDaCreatore("   ").isEmpty());
        assertTrue(richiestaDatabase.richiesteInviateDaRichiedente(null).isEmpty());
        assertTrue(richiestaDatabase.richiesteInviateDaRichiedente("   ").isEmpty());
    }
}
