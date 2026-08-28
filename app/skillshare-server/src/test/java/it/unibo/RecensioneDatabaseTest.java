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

public class RecensioneDatabaseTest {

    private static final String RICHIEDENTE = "luigi.verdi@unibo.it";
    private static final String CREATORE = "mario.rossi@unibo.it";

    private DB dbTest;
    private RecensioneDatabase recensioneDatabase;
    private RichiestaScambioDatabase richiestaDatabase;

    // Un solo DB in memoria per entrambe le collection: RecensioneDatabase
    // deve poter leggere le richieste di scambio
    @BeforeEach
    void setUp() {
        dbTest = DBMaker.memoryDB().make();
        recensioneDatabase = new RecensioneDatabase(dbTest);
        richiestaDatabase = new RichiestaScambioDatabase(dbTest);
    }

    @AfterEach
    void tearDown() {
        if (dbTest != null && !dbTest.isClosed()) {
            dbTest.close();
        }
    }

    /**
     * Crea una richiesta sull'annuncio indicato e la porta fino a COMPLETED.
     */
    private RichiestaScambioDTO scambioCompletato(String idAnnuncio) {
        RichiestaScambioDTO richiesta = new RichiestaScambioDTO();
        richiesta.setIdAnnuncio(idAnnuncio);
        richiesta.setIdRichiedente(RICHIEDENTE);
        richiesta.setIdCreatoreAnnuncio(CREATORE);
        richiesta.setMessaggio("Vorrei scambiare con te");

        RichiestaScambioDTO salvata = richiestaDatabase.salva(richiesta);
        richiestaDatabase.accetta(salvata.getId(), CREATORE);
        return richiestaDatabase.completa(salvata.getId(), RICHIEDENTE);
    }

    // Recensione valida del richiedente verso il creatore dell'annuncio
    private RecensioneDTO creaRecensione(String idRichiestaScambio, String idAutore, int voto) {
        RecensioneDTO recensione = new RecensioneDTO();
        recensione.setIdRichiestaScambio(idRichiestaScambio);
        recensione.setIdAutore(idAutore);
        recensione.setIdDestinatario(CREATORE);
        recensione.setVoto(voto);
        recensione.setCommento("Scambio andato benissimo");
        return recensione;
    }

    @Test
    void testLasciaRecensioneValidaSuScambioCompletato() {
        RichiestaScambioDTO scambio = scambioCompletato("annuncio-1");

        // idAnnuncio volutamente sbagliato: deve essere sovrascritto dal server
        RecensioneDTO recensione = creaRecensione(scambio.getId(), RICHIEDENTE, 5);
        recensione.setIdAnnuncio("annuncio-falsificato");

        RecensioneDTO salvata = recensioneDatabase.lascia(recensione);

        assertNotNull(salvata.getId(), "L'id deve essere generato");
        assertTrue(salvata.getDataCreazione() > 0, "La dataCreazione deve essere valorizzata");
        assertEquals("annuncio-1", salvata.getIdAnnuncio(),
                "L'idAnnuncio deve essere preso dalla richiesta collegata, non dal DTO in ingresso");

        // La recensione deve essere rileggibile dal database
        RecensioneDTO ricaricata = recensioneDatabase.getRecensioniCollection().get(salvata.getId());
        assertNotNull(ricaricata);
        assertEquals(scambio.getId(), ricaricata.getIdRichiestaScambio());
        assertEquals(RICHIEDENTE, ricaricata.getIdAutore());
        assertEquals(CREATORE, ricaricata.getIdDestinatario());
        assertEquals(5, ricaricata.getVoto());
        assertEquals("Scambio andato benissimo", ricaricata.getCommento());
    }

    @Test
    void testLasciaSuScambioNonCompletatoRifiutato() {
        // Accettata ma mai completata
        RichiestaScambioDTO richiesta = new RichiestaScambioDTO();
        richiesta.setIdAnnuncio("annuncio-1");
        richiesta.setIdRichiedente(RICHIEDENTE);
        richiesta.setIdCreatoreAnnuncio(CREATORE);
        RichiestaScambioDTO salvata = richiestaDatabase.salva(richiesta);
        richiestaDatabase.accetta(salvata.getId(), CREATORE);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            recensioneDatabase.lascia(creaRecensione(salvata.getId(), RICHIEDENTE, 4));
        });
        assertEquals("Non è possibile recensire uno scambio non ancora completato", ex.getMessage());
        assertTrue(recensioneDatabase.getRecensioniCollection().isEmpty(), "Non deve salvare nulla");
    }

    @Test
    void testLasciaConVotoFuoriIntervalloRifiutato() {
        RichiestaScambioDTO scambio = scambioCompletato("annuncio-1");

        Exception troppoBasso = assertThrows(IllegalArgumentException.class, () -> {
            recensioneDatabase.lascia(creaRecensione(scambio.getId(), RICHIEDENTE, 0));
        });
        assertEquals("Il voto deve essere compreso tra 1 e 5", troppoBasso.getMessage());

        Exception troppoAlto = assertThrows(IllegalArgumentException.class, () -> {
            recensioneDatabase.lascia(creaRecensione(scambio.getId(), RICHIEDENTE, 6));
        });
        assertEquals("Il voto deve essere compreso tra 1 e 5", troppoAlto.getMessage());

        assertTrue(recensioneDatabase.getRecensioniCollection().isEmpty(), "Non deve salvare nulla");
    }

    @Test
    void testDoppiaRecensioneDelloStessoAutoreRifiutata() {
        RichiestaScambioDTO scambio = scambioCompletato("annuncio-1");
        recensioneDatabase.lascia(creaRecensione(scambio.getId(), RICHIEDENTE, 5));

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            recensioneDatabase.lascia(creaRecensione(scambio.getId(), RICHIEDENTE, 3));
        });
        assertEquals("Hai già recensito questo scambio", ex.getMessage());

        assertEquals(1, recensioneDatabase.getRecensioniCollection().size(),
                "Deve restare la sola prima recensione");

        // L'altro partecipante puo' comunque recensire lo stesso scambio
        RecensioneDTO controparte = creaRecensione(scambio.getId(), CREATORE, 4);
        controparte.setIdDestinatario(RICHIEDENTE);
        assertNotNull(recensioneDatabase.lascia(controparte));
    }

    @Test
    void testRecensioniPerAnnuncioSoloSueEOrdinatePerDataDecrescente() {
        RichiestaScambioDTO primo = scambioCompletato("annuncio-1");
        RichiestaScambioDTO secondo = scambioCompletato("annuncio-1");
        RichiestaScambioDTO altroAnnuncio = scambioCompletato("annuncio-2");

        RecensioneDTO piuVecchia = recensioneDatabase.lascia(creaRecensione(primo.getId(), RICHIEDENTE, 5));
        RecensioneDTO piuRecente = recensioneDatabase.lascia(creaRecensione(secondo.getId(), RICHIEDENTE, 4));
        recensioneDatabase.lascia(creaRecensione(altroAnnuncio.getId(), RICHIEDENTE, 3));

        // lascia() usa System.currentTimeMillis(): forziamo le date per rendere
        // l'ordinamento verificabile a prescindere dalla velocita' del test
        piuVecchia.setDataCreazione(1000L);
        piuRecente.setDataCreazione(2000L);
        recensioneDatabase.getRecensioniCollection().put(piuVecchia.getId(), piuVecchia);
        recensioneDatabase.getRecensioniCollection().put(piuRecente.getId(), piuRecente);

        List<RecensioneDTO> risultato = recensioneDatabase.recensioniPerAnnuncio("annuncio-1");

        assertEquals(2, risultato.size(), "Deve restituire solo le recensioni di annuncio-1");
        assertEquals(piuRecente.getId(), risultato.get(0).getId(), "La piu' recente va per prima");
        assertEquals(piuVecchia.getId(), risultato.get(1).getId());
    }

    @Test
    void testRecensioniPerAnnuncioSenzaRecensioniRestituisceListaVuota() {
        RichiestaScambioDTO scambio = scambioCompletato("annuncio-1");
        recensioneDatabase.lascia(creaRecensione(scambio.getId(), RICHIEDENTE, 5));

        List<RecensioneDTO> risultato = recensioneDatabase.recensioniPerAnnuncio("annuncio-senza-recensioni");

        assertNotNull(risultato, "Deve restituire una lista, non null");
        assertTrue(risultato.isEmpty(), "Un annuncio senza recensioni ha lista vuota");
    }

    // --- rating pubblico e storico ricevuto (US-14) ---

    /**
     * Pubblica una recensione verso il destinatario indicato, su un nuovo
     * scambio completato: cosi' ogni chiamata evita il blocco sui duplicati.
     */
    private RecensioneDTO recensisce(String idAnnuncio, String idDestinatario, int voto) {
        RichiestaScambioDTO scambio = scambioCompletato(idAnnuncio);
        RecensioneDTO recensione = creaRecensione(scambio.getId(), RICHIEDENTE, voto);
        recensione.setIdDestinatario(idDestinatario);
        return recensioneDatabase.lascia(recensione);
    }

    @Test
    void testRatingMedioConPiuRecensioni() {
        recensisce("annuncio-1", CREATORE, 5);
        recensisce("annuncio-2", CREATORE, 4);
        recensisce("annuncio-3", CREATORE, 3);

        // Una recensione verso un altro utente non deve entrare nel calcolo
        recensisce("annuncio-4", "altro.utente@unibo.it", 1);

        Double media = recensioneDatabase.ratingMedio(CREATORE);

        assertNotNull(media, "Con recensioni ricevute la media deve essere valorizzata");
        assertEquals(4.0, media, 0.0001, "(5 + 4 + 3) / 3 = 4.0");
    }

    @Test
    void testRatingMedioNonInteroCalcolatoCorrettamente() {
        recensisce("annuncio-1", CREATORE, 5);
        recensisce("annuncio-2", CREATORE, 4);

        assertEquals(4.5, recensioneDatabase.ratingMedio(CREATORE), 0.0001, "(5 + 4) / 2 = 4.5");
    }

    @Test
    void testRatingMedioSenzaRecensioniRestituisceNull() {
        recensisce("annuncio-1", CREATORE, 5);

        Double media = recensioneDatabase.ratingMedio("nessuno@unibo.it");

        // null, non 0.0: i voti vanno da 1 a 5, quindi 0.0 sarebbe ambiguo
        assertNull(media, "Un utente senza recensioni non ha un rating");
    }

    @Test
    void testRecensioniRicevuteSoloSueEOrdinatePerDataDecrescente() {
        RecensioneDTO piuVecchia = recensisce("annuncio-1", CREATORE, 5);
        RecensioneDTO piuRecente = recensisce("annuncio-2", CREATORE, 4);
        recensisce("annuncio-3", "altro.utente@unibo.it", 3);

        // lascia() usa System.currentTimeMillis(): forziamo le date per rendere
        // l'ordinamento verificabile a prescindere dalla velocita' del test
        piuVecchia.setDataCreazione(1000L);
        piuRecente.setDataCreazione(2000L);
        recensioneDatabase.getRecensioniCollection().put(piuVecchia.getId(), piuVecchia);
        recensioneDatabase.getRecensioniCollection().put(piuRecente.getId(), piuRecente);

        List<RecensioneDTO> ricevute = recensioneDatabase.recensioniRicevute(CREATORE);

        assertEquals(2, ricevute.size(), "Deve restituire solo le recensioni ricevute dal creatore");
        assertEquals(piuRecente.getId(), ricevute.get(0).getId(), "La piu' recente va per prima");
        assertEquals(piuVecchia.getId(), ricevute.get(1).getId());
    }

    @Test
    void testRecensioniRicevuteSenzaRecensioniRestituisceListaVuota() {
        recensisce("annuncio-1", CREATORE, 5);

        List<RecensioneDTO> ricevute = recensioneDatabase.recensioniRicevute("nessuno@unibo.it");

        assertNotNull(ricevute, "Deve restituire una lista, non null");
        assertTrue(ricevute.isEmpty(), "Un utente senza recensioni ricevute ha lista vuota");
    }
}
