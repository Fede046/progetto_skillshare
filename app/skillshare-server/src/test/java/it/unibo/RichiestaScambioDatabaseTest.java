package it.unibo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
        // Mario riceve la richiesta di Luigi sul proprio annuncio
        RichiestaScambioDTO aMario = creaRichiestaValida();
        aMario.setIdCreatoreAnnuncio("mario.rossi@unibo.it");
        richiestaDatabase.salva(aMario);

        // Anna riceve la richiesta di Luigi su un annuncio suo: creatore diverso
        // implica annuncio diverso, altrimenti scatta il blocco sulle richieste
        // multiple per la stessa coppia utente+annuncio.
        RichiestaScambioDTO adAnna = creaRichiestaValida();
        adAnna.setIdAnnuncio("annuncio-2");
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

    // --- Accetta / Rifiuta ---

    @Test
    void testCreatoreAccettaRichiestaPENDING() {
        RichiestaScambioDTO salvata = richiestaDatabase.salva(creaRichiestaValida());

        RichiestaScambioDTO accettata = richiestaDatabase.accetta(salvata.getId(), "mario.rossi@unibo.it");

        assertEquals(StatoRichiesta.ACCEPTED, accettata.getStato(), "Lo stato deve essere ACCEPTED");

        // La richiesta deve essere aggiornata anche nella collection
        RichiestaScambioDTO ricaricata = richiestaDatabase.getRichiesteCollection().get(salvata.getId());
        assertNotNull(ricaricata);
        assertEquals(StatoRichiesta.ACCEPTED, ricaricata.getStato(), "Lo stato persistito deve essere ACCEPTED");

        // La lista delle richieste inviate dal richiedente deve riflettere il nuovo stato
        List<RichiestaScambioDTO> inviate = richiestaDatabase.richiesteInviateDaRichiedente("luigi.verdi@unibo.it");
        assertEquals(1, inviate.size(), "La richiesta deve restare nella lista inviate");
        assertEquals(StatoRichiesta.ACCEPTED, inviate.get(0).getStato(), "La lista inviate deve riflettere ACCEPTED");
    }

    @Test
    void testCreatoreRifiutaRichiestaPENDING() {
        RichiestaScambioDTO salvata = richiestaDatabase.salva(creaRichiestaValida());

        RichiestaScambioDTO rifiutata = richiestaDatabase.rifiuta(salvata.getId(), "mario.rossi@unibo.it");

        assertEquals(StatoRichiesta.REJECTED, rifiutata.getStato(), "Lo stato deve essere REJECTED");

        // La richiesta deve essere aggiornata anche nella collection
        RichiestaScambioDTO ricaricata = richiestaDatabase.getRichiesteCollection().get(salvata.getId());
        assertNotNull(ricaricata);
        assertEquals(StatoRichiesta.REJECTED, ricaricata.getStato(), "Lo stato persistito deve essere REJECTED");
    }

    @Test
    void testAccettaComeNonCreatoreRifiutato() {
        RichiestaScambioDTO salvata = richiestaDatabase.salva(creaRichiestaValida());

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            richiestaDatabase.accetta(salvata.getId(), "anna.bianchi@unibo.it");
        });
        assertEquals("Non autorizzato: solo il creatore dell'annuncio può accettare la richiesta", ex.getMessage());

        // Lo stato deve restare invariato (PENDING)
        RichiestaScambioDTO ricaricata = richiestaDatabase.getRichiesteCollection().get(salvata.getId());
        assertNotNull(ricaricata);
        assertEquals(StatoRichiesta.PENDING, ricaricata.getStato(), "Lo stato non deve cambiare");
    }

    @Test
    void testRifiutaComeNonCreatoreRifiutato() {
        RichiestaScambioDTO salvata = richiestaDatabase.salva(creaRichiestaValida());

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            richiestaDatabase.rifiuta(salvata.getId(), "anna.bianchi@unibo.it");
        });
        assertEquals("Non autorizzato: solo il creatore dell'annuncio può rifiutare la richiesta", ex.getMessage());

        // Lo stato deve restare invariato (PENDING)
        RichiestaScambioDTO ricaricata = richiestaDatabase.getRichiesteCollection().get(salvata.getId());
        assertNotNull(ricaricata);
        assertEquals(StatoRichiesta.PENDING, ricaricata.getStato(), "Lo stato non deve cambiare");
    }
    @Test
    void testAccettaRichiestaInesistente() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            richiestaDatabase.accetta("id-inesistente", "mario.rossi@unibo.it");
        });
        assertEquals("Richiesta non trovata", ex.getMessage());
    }

    @Test
    void testRifiutaRichiestaInesistente() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            richiestaDatabase.rifiuta("id-inesistente", "mario.rossi@unibo.it");
        });
        assertEquals("Richiesta non trovata", ex.getMessage());
    }

    @Test
    void testAccettaSenzaIdRichiesta() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            richiestaDatabase.accetta("   ", "mario.rossi@unibo.it");
        });
        assertEquals("Dati non validi", ex.getMessage());
    }

    @Test
    void testRifiutaSenzaIdCreatore() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            richiestaDatabase.rifiuta("id-qualsiasi", null);
        });
        assertEquals("Dati non validi", ex.getMessage());
    }

    // --- completa(): chiusura dello scambio (US-13) ---

    @Test
    void testRichiedenteCompletaRichiestaACCEPTED() {
        RichiestaScambioDTO salvata = richiestaDatabase.salva(creaRichiestaValida());
        richiestaDatabase.accetta(salvata.getId(), "mario.rossi@unibo.it");

        RichiestaScambioDTO completata = richiestaDatabase.completa(salvata.getId(), "luigi.verdi@unibo.it");

        assertEquals(StatoRichiesta.COMPLETED, completata.getStato(), "Lo stato deve passare a COMPLETED");

        // Anche il dato persistito deve risultare aggiornato
        RichiestaScambioDTO ricaricata = richiestaDatabase.getRichiesteCollection().get(salvata.getId());
        assertEquals(StatoRichiesta.COMPLETED, ricaricata.getStato());
    }

    @Test
    void testCreatoreAnnuncioCompletaRichiestaACCEPTED() {
        RichiestaScambioDTO salvata = richiestaDatabase.salva(creaRichiestaValida());
        richiestaDatabase.accetta(salvata.getId(), "mario.rossi@unibo.it");

        // Anche l'altro partecipante puo' chiudere lo scambio
        RichiestaScambioDTO completata = richiestaDatabase.completa(salvata.getId(), "mario.rossi@unibo.it");

        assertEquals(StatoRichiesta.COMPLETED, completata.getStato());
    }

    @Test
    void testCompletaComeEstraneoRifiutato() {
        RichiestaScambioDTO salvata = richiestaDatabase.salva(creaRichiestaValida());
        richiestaDatabase.accetta(salvata.getId(), "mario.rossi@unibo.it");

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            richiestaDatabase.completa(salvata.getId(), "estraneo@unibo.it");
        });
        assertEquals("Non sei autorizzato a completare questo scambio", ex.getMessage());

        // Lo stato non deve essere cambiato
        assertEquals(StatoRichiesta.ACCEPTED,
                richiestaDatabase.getRichiesteCollection().get(salvata.getId()).getStato());
    }

    @Test
    void testCompletaRichiestaPENDINGRifiutato() {
        // Salvata ma mai accettata: resta PENDING
        RichiestaScambioDTO salvata = richiestaDatabase.salva(creaRichiestaValida());

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            richiestaDatabase.completa(salvata.getId(), "luigi.verdi@unibo.it");
        });
        assertEquals("Impossibile completare uno scambio non ancora accettato", ex.getMessage());

        assertEquals(StatoRichiesta.PENDING,
                richiestaDatabase.getRichiesteCollection().get(salvata.getId()).getStato());
    }

    @Test
    void testCompletaRichiestaInesistente() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            richiestaDatabase.completa("id-inesistente", "luigi.verdi@unibo.it");
        });
        assertEquals("Richiesta non trovata", ex.getMessage());
    }

    // --- Richieste multiple sullo stesso annuncio (Issue #136) ---

    @Test
    void testSecondaRichiestaConPrimaPENDINGRifiutata() {
        richiestaDatabase.salva(creaRichiestaValida());

        // Stesso richiedente, stesso annuncio: la prima e' ancora PENDING
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            richiestaDatabase.salva(creaRichiestaValida());
        });
        assertEquals("Hai già una richiesta su questo annuncio non ancora completata", ex.getMessage());

        // La seconda non deve essere finita nel database
        assertEquals(1, richiestaDatabase.getRichiesteCollection().size(),
                "Deve restare salvata solo la prima richiesta");
    }

    @Test
    void testSecondaRichiestaConPrimaACCEPTEDRifiutata() {
        RichiestaScambioDTO prima = richiestaDatabase.salva(creaRichiestaValida());
        richiestaDatabase.accetta(prima.getId(), "mario.rossi@unibo.it");

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            richiestaDatabase.salva(creaRichiestaValida());
        });
        assertEquals("Hai già una richiesta su questo annuncio non ancora completata", ex.getMessage());
        assertEquals(1, richiestaDatabase.getRichiesteCollection().size());
    }

    @Test
    void testSecondaRichiestaConPrimaREJECTEDRifiutata() {
        RichiestaScambioDTO prima = richiestaDatabase.salva(creaRichiestaValida());
        richiestaDatabase.rifiuta(prima.getId(), "mario.rossi@unibo.it");

        // Anche il rifiuto blocca: non si puo' insistere sullo stesso annuncio
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            richiestaDatabase.salva(creaRichiestaValida());
        });
        assertEquals("Hai già una richiesta su questo annuncio non ancora completata", ex.getMessage());
        assertEquals(1, richiestaDatabase.getRichiesteCollection().size());
    }

    @Test
    void testNuovaRichiestaDopoCOMPLETEDPermessa() {
        RichiestaScambioDTO prima = richiestaDatabase.salva(creaRichiestaValida());
        richiestaDatabase.accetta(prima.getId(), "mario.rossi@unibo.it");
        richiestaDatabase.completa(prima.getId(), "luigi.verdi@unibo.it");

        // Lo scambio precedente si e' concluso: se ne puo' proporre uno nuovo
        RichiestaScambioDTO seconda = richiestaDatabase.salva(creaRichiestaValida());

        assertNotNull(seconda.getId(), "La seconda richiesta deve essere creata");
        assertNotEquals(prima.getId(), seconda.getId(), "Deve essere una richiesta distinta");
        assertEquals(StatoRichiesta.PENDING, seconda.getStato(), "La nuova richiesta riparte da PENDING");
        assertEquals(2, richiestaDatabase.getRichiesteCollection().size(),
                "Entrambe le richieste devono coesistere");
    }

    @Test
    void testRichiesteSuAnnunciDiversiNonSiBloccano() {
        // Prima richiesta su annuncio-1, lasciata PENDING
        richiestaDatabase.salva(creaRichiestaValida());

        // Stesso richiedente, annuncio diverso: il blocco e' per coppia utente+annuncio
        RichiestaScambioDTO suAltroAnnuncio = creaRichiestaValida();
        suAltroAnnuncio.setIdAnnuncio("annuncio-2");
        RichiestaScambioDTO salvata = richiestaDatabase.salva(suAltroAnnuncio);

        assertNotNull(salvata.getId(), "La richiesta su un altro annuncio deve essere creata");
        assertEquals("annuncio-2", salvata.getIdAnnuncio());
        assertEquals(StatoRichiesta.PENDING, salvata.getStato());

        // Il richiedente risulta avere due richieste aperte, una per annuncio
        List<RichiestaScambioDTO> inviate =
                richiestaDatabase.richiesteInviateDaRichiedente("luigi.verdi@unibo.it");
        assertEquals(2, inviate.size(), "Le richieste su annunci diversi convivono");
    }
}
