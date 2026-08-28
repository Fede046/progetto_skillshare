package it.unibo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;

public class ReputazioneServiceImplTest {

    private static final String CREATORE = "mario.rossi@unibo.it";
    private static final String RICHIEDENTE = "luigi.verdi@unibo.it";
    private static final String ALTRO = "altro.utente@unibo.it";

    private DB dbTest;
    private RecensioneDatabase recensioneDatabase;
    private RichiestaScambioDatabase richiesteDatabase;
    private ReputazioneServiceImpl service;

    // DB in memoria iniettato nel servizio: il singleton DatabaseCore non viene toccato,
    // così gli altri test della suite non vengono disturbati
    @BeforeEach
    void setUp() {
        dbTest = DBMaker.memoryDB().make();
        recensioneDatabase = new RecensioneDatabase(dbTest);
        richiesteDatabase = new RichiestaScambioDatabase(dbTest);
        service = new ReputazioneServiceImpl(recensioneDatabase);
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

        RichiestaScambioDTO salvata = richiesteDatabase.salva(richiesta);
        richiesteDatabase.accetta(salvata.getId(), CREATORE);
        return richiesteDatabase.completa(salvata.getId(), RICHIEDENTE);
    }

    /**
     * Pubblica una recensione verso il destinatario indicato, su un nuovo
     * scambio completato: cosi' ogni chiamata evita il blocco sui duplicati.
     */
    private RecensioneDTO recensisce(String idAnnuncio, String idDestinatario, int voto) {
        RichiestaScambioDTO scambio = scambioCompletato(idAnnuncio);

        RecensioneDTO recensione = new RecensioneDTO();
        recensione.setIdRichiestaScambio(scambio.getId());
        recensione.setIdAutore(RICHIEDENTE);
        recensione.setIdDestinatario(idDestinatario);
        recensione.setVoto(voto);
        recensione.setCommento("Scambio andato benissimo");

        return recensioneDatabase.lascia(recensione);
    }

    // --- ratingMedio ---

    @Test
    void testRatingMedioDelegaARecensioneDatabase() {
        recensisce("annuncio-1", CREATORE, 5);
        recensisce("annuncio-2", CREATORE, 4);

        Double media = service.ratingMedio(CREATORE);

        assertNotNull(media, "Con recensioni ricevute la media deve essere valorizzata");
        assertEquals(4.5, media, 0.0001, "(5 + 4) / 2 = 4.5");
        // Stesso risultato del database: la servlet non rielabora nulla
        assertEquals(recensioneDatabase.ratingMedio(CREATORE), media);
    }

    @Test
    void testRatingMedioSenzaRecensioniRestituisceNull() {
        recensisce("annuncio-1", CREATORE, 5);

        // null, non 0.0: i voti vanno da 1 a 5, quindi 0.0 sarebbe ambiguo
        assertNull(service.ratingMedio("nessuno@unibo.it"),
                "Un utente senza recensioni non ha un rating");
    }

    @Test
    void testRatingMedioIgnoraRecensioniVersoAltriUtenti() {
        recensisce("annuncio-1", CREATORE, 5);
        recensisce("annuncio-2", ALTRO, 1);

        assertEquals(5.0, service.ratingMedio(CREATORE), 0.0001,
                "La recensione verso un altro utente non entra nel calcolo");
    }

    // --- recensioniRicevute ---

    @Test
    void testRecensioniRicevuteDelegaARecensioneDatabase() {
        RecensioneDTO piuVecchia = recensisce("annuncio-1", CREATORE, 5);
        RecensioneDTO piuRecente = recensisce("annuncio-2", CREATORE, 4);
        recensisce("annuncio-3", ALTRO, 3);

        // lascia() usa System.currentTimeMillis(): forziamo le date per rendere
        // l'ordinamento verificabile a prescindere dalla velocita' del test
        piuVecchia.setDataCreazione(1000L);
        piuRecente.setDataCreazione(2000L);
        recensioneDatabase.getRecensioniCollection().put(piuVecchia.getId(), piuVecchia);
        recensioneDatabase.getRecensioniCollection().put(piuRecente.getId(), piuRecente);

        List<RecensioneDTO> ricevute = service.recensioniRicevute(CREATORE);

        assertEquals(2, ricevute.size(), "Deve restituire solo le recensioni ricevute dal creatore");
        // L'ordinamento decrescente arriva da RecensioneDatabase
        assertEquals(piuRecente.getId(), ricevute.get(0).getId(), "La piu' recente va per prima");
        assertEquals(piuVecchia.getId(), ricevute.get(1).getId());
    }

    @Test
    void testRecensioniRicevuteSenzaRecensioniRestituisceListaVuota() {
        recensisce("annuncio-1", CREATORE, 5);

        List<RecensioneDTO> ricevute = service.recensioniRicevute("nessuno@unibo.it");

        assertNotNull(ricevute, "Deve restituire una lista, non null");
        assertTrue(ricevute.isEmpty(), "Un utente senza recensioni ricevute ha lista vuota");
    }
}
