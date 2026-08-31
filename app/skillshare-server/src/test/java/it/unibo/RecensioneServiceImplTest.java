package it.unibo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;

public class RecensioneServiceImplTest {

    // UtenteDatabase espone metodi statici legati al singleton DatabaseCore:
    // senza test mode le registrazioni finirebbero sul file progetto_sweng.db
    @BeforeAll
    static void setUpDatabase() {
        DatabaseCore.enableTestMode();
    }

    @AfterAll
    static void tearDownDatabase() {
        DatabaseCore.disableTestMode();
    }

    private static final String CREATORE = "mario.rossi@unibo.it";
    private static final String RICHIEDENTE = "luigi.verdi@unibo.it";

    private DB dbTest;
    private RecensioneDatabase recensioneDatabase;
    private RichiestaScambioDatabase richiesteDatabase;
    private RecensioneServiceImpl service;

    // DB in memoria iniettato nel servizio: il singleton DatabaseCore non viene toccato,
    // così gli altri test della suite non vengono disturbati
    @BeforeEach
    void setUp() {
        dbTest = DBMaker.memoryDB().make();
        recensioneDatabase = new RecensioneDatabase(dbTest);
        richiesteDatabase = new RichiestaScambioDatabase(dbTest);
        service = new RecensioneServiceImpl(recensioneDatabase);
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
    void testLasciaDelegaARecensioneDatabase() {
        RichiestaScambioDTO scambio = scambioCompletato("annuncio-1");

        RecensioneDTO salvata = service.lascia(creaRecensione(scambio.getId(), RICHIEDENTE, 5));

        // Id, idAnnuncio e dataCreazione li valorizza RecensioneDatabase: se ci sono, la delega ha funzionato
        assertNotNull(salvata.getId(), "L'id deve essere generato");
        assertTrue(salvata.getDataCreazione() > 0, "La dataCreazione deve essere valorizzata");
        assertEquals("annuncio-1", salvata.getIdAnnuncio(),
                "L'idAnnuncio deve essere ricavato dalla richiesta collegata");

        // La recensione deve essere finita davvero nella collection
        RecensioneDTO ricaricata = recensioneDatabase.getRecensioniCollection().get(salvata.getId());
        assertNotNull(ricaricata, "La recensione deve essere recuperabile dal database");
        assertEquals(RICHIEDENTE, ricaricata.getIdAutore());
        assertEquals(5, ricaricata.getVoto());
    }

    @Test
    void testLasciaIgnoraIdAnnuncioInviatoDalClient() {
        RichiestaScambioDTO scambio = scambioCompletato("annuncio-1");

        RecensioneDTO recensione = creaRecensione(scambio.getId(), RICHIEDENTE, 4);
        recensione.setIdAnnuncio("annuncio-falsificato");

        RecensioneDTO salvata = service.lascia(recensione);

        assertEquals("annuncio-1", salvata.getIdAnnuncio(),
                "Il server deve sovrascrivere l'idAnnuncio arrivato dal client");
    }

    @Test
    void testLasciaSuScambioNonCompletatoPropagaEccezione() {
        // Accettata ma mai completata
        RichiestaScambioDTO richiesta = new RichiestaScambioDTO();
        richiesta.setIdAnnuncio("annuncio-1");
        richiesta.setIdRichiedente(RICHIEDENTE);
        richiesta.setIdCreatoreAnnuncio(CREATORE);
        RichiestaScambioDTO salvata = richiesteDatabase.salva(richiesta);
        richiesteDatabase.accetta(salvata.getId(), CREATORE);

        // Il messaggio arriva da RecensioneDatabase: la servlet non valida in proprio
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            service.lascia(creaRecensione(salvata.getId(), RICHIEDENTE, 4));
        });
        assertEquals("Non è possibile recensire uno scambio non ancora completato", ex.getMessage());
    }

    @Test
    void testRecensioniPerAnnuncioDelegaARecensioneDatabase() {
        RichiestaScambioDTO primo = scambioCompletato("annuncio-1");
        RichiestaScambioDTO altro = scambioCompletato("annuncio-2");

        RecensioneDTO attesa = service.lascia(creaRecensione(primo.getId(), RICHIEDENTE, 5));
        service.lascia(creaRecensione(altro.getId(), RICHIEDENTE, 3));

        List<RecensioneDTO> risultato = service.recensioniPerAnnuncio("annuncio-1");

        assertEquals(1, risultato.size(), "Deve restituire solo le recensioni di annuncio-1");
        assertEquals(attesa.getId(), risultato.get(0).getId());
    }

    @Test
    void testRecensioniPerAnnuncioSenzaRecensioniRestituisceListaVuota() {
        List<RecensioneDTO> risultato = service.recensioniPerAnnuncio("annuncio-senza-recensioni");

        assertNotNull(risultato, "Deve restituire una lista, non null");
        assertTrue(risultato.isEmpty(), "Un annuncio senza recensioni ha lista vuota");
    }

    @Test
    void testRecensioniPerAnnuncioRisolveNomeAutore() {
        // Utente registrato: UtenteDatabase e' statico sul database reale,
        // quindi usiamo un'email univoca per non collidere con gli altri test
        String email = "recensore." + System.currentTimeMillis() + "@unibo.it";
        UtenteDatabase.registra(new UtenteDTO(email, "@Password123", "Anna", "Bianchi"));

        RichiestaScambioDTO scambio = scambioCompletato("annuncio-nome");
        service.lascia(creaRecensione(scambio.getId(), email, 5));

        List<RecensioneDTO> risultato = service.recensioniPerAnnuncio("annuncio-nome");

        assertEquals(1, risultato.size());
        assertEquals("Anna Bianchi", risultato.get(0).getNomeAutore(),
                "Il nome autore deve essere risolto via UtenteDatabase");
    }

    @Test
    void testRecensioniPerAnnuncioAutoreNonRegistratoUsaEmailComeFallback() {
        RichiestaScambioDTO scambio = scambioCompletato("annuncio-fantasma");
        String email = "fantasma." + System.currentTimeMillis() + "@unibo.it";
        service.lascia(creaRecensione(scambio.getId(), email, 3));

        List<RecensioneDTO> risultato = service.recensioniPerAnnuncio("annuncio-fantasma");

        assertEquals(email, risultato.get(0).getNomeAutore(),
                "Senza utente registrato resta visibile l'email");
    }
}
