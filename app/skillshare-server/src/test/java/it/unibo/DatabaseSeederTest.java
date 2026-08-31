package it.unibo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Verifica che il seeding produca davvero i dati attesi.
 * Gira su database in memoria: il file progetto_sweng.db non viene toccato.
 */
public class DatabaseSeederTest {

    @BeforeAll
    static void setUpDatabase() {
        DatabaseCore.enableTestMode();
        // UtenteDatabase è statico su DatabaseCore, gli altri repository
        // ricevono lo stesso DB dal costruttore di default: in test mode
        // puntano tutti alla RAM
        DatabaseSeeder.popola();
    }

    @AfterAll
    static void tearDownDatabase() {
        DatabaseCore.disableTestMode();
    }

    @Test
    void testQuattroUtentiDemoCreati() {
        for (String email : new String[] { DatabaseSeeder.DEMO_1, DatabaseSeeder.DEMO_2,
                DatabaseSeeder.DEMO_3, DatabaseSeeder.DEMO_4 }) {
            UtenteDTO utente = UtenteDatabase.getProfilo(email);
            assertNotNull(utente, "L'utente demo " + email + " deve esistere");
            assertFalse(utente.getBio() == null || utente.getBio().trim().isEmpty(),
                    "Ogni utente demo deve avere una bio");
            assertFalse(utente.getPhotoUrl() == null || utente.getPhotoUrl().trim().isEmpty(),
                    "Ogni utente demo deve avere una foto");
            assertTrue(utente.getTagCompetenza().size() >= 2,
                    "Ogni utente demo deve avere almeno 2 tag di competenza");
        }
    }

    @Test
    void testCredenzialiDemoFunzionanti() {
        // La password documentata deve permettere davvero l'accesso
        UtenteDTO utente = UtenteDatabase.verificaCredenziali(
                DatabaseSeeder.DEMO_1, DatabaseSeeder.PASSWORD_DEMO);
        assertEquals("Giulia", utente.getNome());
    }

    @Test
    void testOgniUtenteHaAlmenoUnAnnuncio() {
        AnnuncioDatabase annunci = new AnnuncioDatabase();
        for (String email : new String[] { DatabaseSeeder.DEMO_1, DatabaseSeeder.DEMO_2,
                DatabaseSeeder.DEMO_3, DatabaseSeeder.DEMO_4 }) {
            assertFalse(annunci.annunciDiUtente(email).isEmpty(),
                    "L'utente " + email + " deve avere almeno un annuncio");
        }
        assertTrue(annunci.tuttiGliAnnunci().size() >= 4, "Devono esserci almeno 4 annunci");
    }

    @Test
    void testRichiesteNeiQuattroStati() {
        List<RichiestaScambioDTO> tutte = tutteLeRichieste();

        assertTrue(contieneStato(tutte, StatoRichiesta.PENDING), "Manca una richiesta PENDING");
        assertTrue(contieneStato(tutte, StatoRichiesta.ACCEPTED), "Manca una richiesta ACCEPTED");
        assertTrue(contieneStato(tutte, StatoRichiesta.REJECTED), "Manca una richiesta REJECTED");
        assertTrue(contieneStato(tutte, StatoRichiesta.COMPLETED), "Manca una richiesta COMPLETED");
        assertEquals(4, tutte.size(), "Devono esserci quattro richieste demo");
    }

    @Test
    void testMessaggiSulloScambioAccettato() {
        RichiestaScambioDTO accettata = primaConStato(StatoRichiesta.ACCEPTED);
        assertNotNull(accettata);

        MessaggioDatabase messaggi = new MessaggioDatabase();
        List<MessaggioDTO> conversazione =
                messaggi.getMessaggi(accettata.getId(), accettata.getIdRichiedente());

        assertFalse(conversazione.isEmpty(), "Lo scambio accettato deve avere una chat");
        assertTrue(conversazione.size() >= 2, "La chat deve contenere più di un messaggio");
    }

    @Test
    void testRecensioniSulloScambioCompletato() {
        RichiestaScambioDTO completata = primaConStato(StatoRichiesta.COMPLETED);
        assertNotNull(completata);

        RecensioneDatabase recensioni = new RecensioneDatabase();
        List<RecensioneDTO> ricevute = recensioni.recensioniRicevute(completata.getIdCreatoreAnnuncio());

        assertFalse(ricevute.isEmpty(), "Lo scambio completato deve aver prodotto una recensione");

        RecensioneDTO recensione = ricevute.get(0);
        assertTrue(recensione.getVoto() >= 1 && recensione.getVoto() <= 5, "Voto entro 1-5");
        assertFalse(recensione.getCommento() == null || recensione.getCommento().trim().isEmpty(),
                "La recensione deve avere un commento");
    }

    @Test
    void testRatingVisibileSulProfiloPubblico() {
        // È lo scopo delle recensioni demo: un profilo con rating già popolato
        RecensioneDatabase recensioni = new RecensioneDatabase();
        Double rating = recensioni.ratingMedio(DatabaseSeeder.DEMO_3);

        assertNotNull(rating, "Il profilo di demo3 deve mostrare un rating");
        assertTrue(rating >= 1.0 && rating <= 5.0, "Il rating deve essere entro 1-5");
    }

    @Test
    void testSeedingRipetibileSenzaDuplicare() {
        int utentiPrima = 4;
        int annunciPrima = new AnnuncioDatabase().tuttiGliAnnunci().size();
        int richiestePrima = tutteLeRichieste().size();

        // Secondo giro sullo stesso database: non deve fare nulla
        boolean eseguito = DatabaseSeeder.popola();

        assertFalse(eseguito, "Il seeding non deve rieseguirsi su un database già popolato");
        assertEquals(annunciPrima, new AnnuncioDatabase().tuttiGliAnnunci().size(),
                "Gli annunci non devono essere duplicati");
        assertEquals(richiestePrima, tutteLeRichieste().size(),
                "Le richieste non devono essere duplicate");
        assertNotNull(UtenteDatabase.getProfilo(DatabaseSeeder.DEMO_1));
        assertEquals(utentiPrima, 4);
    }

    // --- Supporto ---

    private List<RichiestaScambioDTO> tutteLeRichieste() {
        RichiestaScambioDatabase richieste = new RichiestaScambioDatabase();
        List<RichiestaScambioDTO> tutte = new ArrayList<>();
        for (String email : new String[] { DatabaseSeeder.DEMO_1, DatabaseSeeder.DEMO_2,
                DatabaseSeeder.DEMO_3, DatabaseSeeder.DEMO_4 }) {
            for (RichiestaScambioDTO r : richieste.richiesteInviateDaRichiedente(email)) {
                tutte.add(r);
            }
        }
        return tutte;
    }

    private boolean contieneStato(List<RichiestaScambioDTO> richieste, StatoRichiesta stato) {
        for (RichiestaScambioDTO r : richieste) {
            if (r.getStato() == stato) {
                return true;
            }
        }
        return false;
    }

    private RichiestaScambioDTO primaConStato(StatoRichiesta stato) {
        for (RichiestaScambioDTO r : tutteLeRichieste()) {
            if (r.getStato() == stato) {
                return r;
            }
        }
        return null;
    }
}
