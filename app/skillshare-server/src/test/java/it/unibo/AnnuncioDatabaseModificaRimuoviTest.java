package it.unibo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;

public class AnnuncioDatabaseModificaRimuoviTest {

    private static final String PROPRIETARIO = "mario.rossi@unibo.it";
    private static final String NON_PROPRIETARIO = "luigi.verdi@unibo.it";

    private DB dbTest;
    private AnnuncioDatabase annuncioDatabase;

    // DB in memoria per non sporcare il database reale
    @BeforeEach
    void setUp() {
        dbTest = DBMaker.memoryDB().make();
        annuncioDatabase = new AnnuncioDatabase(dbTest);
    }

    @AfterEach
    void tearDown() {
        if (dbTest != null && !dbTest.isClosed()) {
            dbTest.close();
        }
    }

    // Annuncio con tutti i campi validi, da modificare nei singoli test
    private AnnuncioDTO creaAnnuncioValido() {
        AnnuncioDTO annuncio = new AnnuncioDTO();
        annuncio.setIdUtente(PROPRIETARIO);
        annuncio.setTitolo("Ripetizioni di Java");
        annuncio.setDescrizione("Lezioni base e avanzate su Java e GWT");
        annuncio.setCompetenzaOfferta("Programmazione Java");
        annuncio.setDisponibilita("Lunedì e mercoledì pomeriggio");
        annuncio.setControprestazione("Lezioni di inglese");
        return annuncio;
    }

    // Pubblica un annuncio valido e ne restituisce il DTO con id valorizzato
    private AnnuncioDTO pubblicaAnnuncio() {
        return annuncioDatabase.pubblica(creaAnnuncioValido());
    }

    @Test
    void testModificaDaProprietarioAggiornaAnnuncio() {
        AnnuncioDTO pubblicato = pubblicaAnnuncio();

        AnnuncioDTO aggiornato = creaAnnuncioValido();
        aggiornato.setTitolo("Ripetizioni di Java Avanzato");
        aggiornato.setCompetenzaOfferta("Programmazione Java Avanzata");
        aggiornato.setDisponibilita("Venerdì pomeriggio");
        aggiornato.setControprestazione("Lezioni di tedesco");

        AnnuncioDTO risultato = annuncioDatabase.modifica(pubblicato.getId(), PROPRIETARIO, aggiornato);

        assertNotNull(risultato, "La modifica deve restituire l'annuncio aggiornato");
        assertEquals(pubblicato.getId(), risultato.getId(), "L'id deve restare invariato");
        assertEquals(pubblicato.getDataCreazione(), risultato.getDataCreazione(),
                "La dataCreazione deve restare invariata");

        // L'annuncio modificato deve essere rileggibile dal database
        AnnuncioDTO ricaricato = annuncioDatabase.getAnnunciCollection().get(pubblicato.getId());
        assertNotNull(ricaricato, "L'annuncio deve essere ancora presente dopo la modifica");
        assertEquals("Ripetizioni di Java Avanzato", ricaricato.getTitolo());
        assertEquals("Programmazione Java Avanzata", ricaricato.getCompetenzaOfferta());
        assertEquals("Venerdì pomeriggio", ricaricato.getDisponibilita());
        assertEquals("Lezioni di tedesco", ricaricato.getControprestazione());
        assertEquals(PROPRIETARIO, ricaricato.getIdUtente(), "Il proprietario deve restare invariato");
    }

    @Test
    void testModificaDaNonProprietarioVieneRifiutata() {
        AnnuncioDTO pubblicato = pubblicaAnnuncio();

        AnnuncioDTO aggiornato = creaAnnuncioValido();
        aggiornato.setTitolo("Titolo Cambiato da Intruso");

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            annuncioDatabase.modifica(pubblicato.getId(), NON_PROPRIETARIO, aggiornato);
        });
        assertEquals("Non autorizzato: l'annuncio appartiene a un altro utente", ex.getMessage());

        // L'annuncio originale deve restare invariato
        AnnuncioDTO ricaricato = annuncioDatabase.getAnnunciCollection().get(pubblicato.getId());
        assertNotNull(ricaricato, "L'annuncio deve restare presente");
        assertEquals("Ripetizioni di Java", ricaricato.getTitolo(), "Il titolo non deve cambiare");
        assertEquals(PROPRIETARIO, ricaricato.getIdUtente());
    }

    @Test
    void testModificaAnnuncioInesistenteLanciaEccezione() {
        AnnuncioDTO aggiornato = creaAnnuncioValido();

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            annuncioDatabase.modifica("id-inesistente", PROPRIETARIO, aggiornato);
        });
        assertEquals("Annuncio non trovato", ex.getMessage());
    }

    @Test
    void testRimuoviDaProprietarioEliminaAnnuncio() {
        AnnuncioDTO pubblicato = pubblicaAnnuncio();
        assertTrue(annuncioDatabase.getAnnunciCollection().containsKey(pubblicato.getId()));

        annuncioDatabase.rimuovi(pubblicato.getId(), PROPRIETARIO);

        assertFalse(annuncioDatabase.getAnnunciCollection().containsKey(pubblicato.getId()),
                "L'annuncio non deve essere più presente dopo la rimozione");
        assertTrue(annuncioDatabase.getAnnunciCollection().isEmpty());
    }

    @Test
    void testRimuoviDaNonProprietarioVieneRifiutata() {
        AnnuncioDTO pubblicato = pubblicaAnnuncio();

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            annuncioDatabase.rimuovi(pubblicato.getId(), NON_PROPRIETARIO);
        });
        assertEquals("Non autorizzato: l'annuncio appartiene a un altro utente", ex.getMessage());

        // L'annuncio deve restare presente
        assertTrue(annuncioDatabase.getAnnunciCollection().containsKey(pubblicato.getId()),
                "L'annuncio deve restare presente se la rimozione è rifiutata");
        assertEquals(1, annuncioDatabase.getAnnunciCollection().size());
    }

    @Test
    void testRimuoviAnnuncioInesistenteLanciaEccezione() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            annuncioDatabase.rimuovi("id-inesistente", PROPRIETARIO);
        });
        assertEquals("Annuncio non trovato", ex.getMessage());
    }

    @Test
    void testModificaConCampiObbligatoriMancantiVieneRifiutata() {
        AnnuncioDTO pubblicato = pubblicaAnnuncio();

        AnnuncioDTO aggiornato = creaAnnuncioValido();
        aggiornato.setTitolo(null);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            annuncioDatabase.modifica(pubblicato.getId(), PROPRIETARIO, aggiornato);
        });
        assertEquals("Il campo 'titolo' è obbligatorio", ex.getMessage());

        // L'annuncio originale deve restare invariato
        AnnuncioDTO ricaricato = annuncioDatabase.getAnnunciCollection().get(pubblicato.getId());
        assertNotNull(ricaricato);
        assertEquals("Ripetizioni di Java", ricaricato.getTitolo(), "Il titolo non deve cambiare");
    }

    @Test
    void testModificaDaProprietarioNonCambiaProprietario() {
        AnnuncioDTO pubblicato = pubblicaAnnuncio();

        // Tentativo di "cambiare proprietario" durante la modifica
        AnnuncioDTO aggiornato = creaAnnuncioValido();
        aggiornato.setIdUtente(NON_PROPRIETARIO);
        aggiornato.setTitolo("Titolo Aggiornato");

        AnnuncioDTO risultato = annuncioDatabase.modifica(pubblicato.getId(), PROPRIETARIO, aggiornato);

        // Il proprietario reale non deve poter essere cambiato: l'idUtente resta quello originale
        assertEquals(PROPRIETARIO, risultato.getIdUtente(), "Il proprietario non deve cambiare");
        assertNotEquals(NON_PROPRIETARIO, risultato.getIdUtente());

        AnnuncioDTO ricaricato = annuncioDatabase.getAnnunciCollection().get(pubblicato.getId());
        assertEquals(PROPRIETARIO, ricaricato.getIdUtente());
    }

    // --- Annuncio con scambio in corso: non modificabile ne' rimovibile ---

    /**
     * Porta una richiesta su quell'annuncio fino allo stato ACCEPTED,
     * cioe' lo scambio e' in corso.
     */
    private void creaScambioAccettatoSu(String idAnnuncio) {
        RichiestaScambioDatabase richieste = new RichiestaScambioDatabase(dbTest);

        RichiestaScambioDTO richiesta = new RichiestaScambioDTO();
        richiesta.setIdAnnuncio(idAnnuncio);
        richiesta.setIdRichiedente(NON_PROPRIETARIO);
        richiesta.setIdCreatoreAnnuncio(PROPRIETARIO);
        RichiestaScambioDTO salvata = richieste.salva(richiesta);

        richieste.accetta(salvata.getId(), PROPRIETARIO);
    }

    @Test
    void testModificaBloccataConScambioInCorso() {
        AnnuncioDTO pubblicato = annuncioDatabase.pubblica(creaAnnuncioValido());
        creaScambioAccettatoSu(pubblicato.getId());

        AnnuncioDTO aggiornato = creaAnnuncioValido();
        aggiornato.setTitolo("Titolo cambiato");

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            annuncioDatabase.modifica(pubblicato.getId(), PROPRIETARIO, aggiornato);
        });
        assertEquals("Non puoi modificare un annuncio con uno scambio in corso", ex.getMessage());

        // L'annuncio deve essere rimasto quello originale
        AnnuncioDTO ricaricato = annuncioDatabase.getAnnunciCollection().get(pubblicato.getId());
        assertEquals("Ripetizioni di Java", ricaricato.getTitolo());
    }

    @Test
    void testRimozioneBloccataConScambioInCorso() {
        AnnuncioDTO pubblicato = annuncioDatabase.pubblica(creaAnnuncioValido());
        creaScambioAccettatoSu(pubblicato.getId());

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            annuncioDatabase.rimuovi(pubblicato.getId(), PROPRIETARIO);
        });
        assertEquals("Non puoi rimuovere un annuncio con uno scambio in corso", ex.getMessage());

        assertTrue(annuncioDatabase.getAnnunciCollection().containsKey(pubblicato.getId()),
                "L'annuncio non deve essere stato rimosso");
    }

    @Test
    void testRichiestaSoloPENDINGNonBloccaModificaERimozione() {
        AnnuncioDTO pubblicato = annuncioDatabase.pubblica(creaAnnuncioValido());

        // Richiesta ricevuta ma non ancora accettata: l'annuncio resta disponibile
        RichiestaScambioDatabase richieste = new RichiestaScambioDatabase(dbTest);
        RichiestaScambioDTO richiesta = new RichiestaScambioDTO();
        richiesta.setIdAnnuncio(pubblicato.getId());
        richiesta.setIdRichiedente(NON_PROPRIETARIO);
        richiesta.setIdCreatoreAnnuncio(PROPRIETARIO);
        richieste.salva(richiesta);

        AnnuncioDTO aggiornato = creaAnnuncioValido();
        aggiornato.setTitolo("Titolo aggiornato");
        AnnuncioDTO esito = annuncioDatabase.modifica(pubblicato.getId(), PROPRIETARIO, aggiornato);
        assertEquals("Titolo aggiornato", esito.getTitolo(), "Con richieste PENDING la modifica resta possibile");

        annuncioDatabase.rimuovi(pubblicato.getId(), PROPRIETARIO);
        assertFalse(annuncioDatabase.getAnnunciCollection().containsKey(pubblicato.getId()),
                "Con richieste PENDING la rimozione resta possibile");
    }

    @Test
    void testScambioCompletatoSbloccaModificaERimozione() {
        AnnuncioDTO pubblicato = annuncioDatabase.pubblica(creaAnnuncioValido());

        RichiestaScambioDatabase richieste = new RichiestaScambioDatabase(dbTest);
        RichiestaScambioDTO richiesta = new RichiestaScambioDTO();
        richiesta.setIdAnnuncio(pubblicato.getId());
        richiesta.setIdRichiedente(NON_PROPRIETARIO);
        richiesta.setIdCreatoreAnnuncio(PROPRIETARIO);
        RichiestaScambioDTO salvata = richieste.salva(richiesta);
        richieste.accetta(salvata.getId(), PROPRIETARIO);
        richieste.completa(salvata.getId(), PROPRIETARIO);

        // Scambio concluso: l'annuncio torna nella piena disponibilita' del proprietario
        AnnuncioDTO aggiornato = creaAnnuncioValido();
        aggiornato.setTitolo("Di nuovo modificabile");
        AnnuncioDTO esito = annuncioDatabase.modifica(pubblicato.getId(), PROPRIETARIO, aggiornato);
        assertEquals("Di nuovo modificabile", esito.getTitolo());

        annuncioDatabase.rimuovi(pubblicato.getId(), PROPRIETARIO);
        assertFalse(annuncioDatabase.getAnnunciCollection().containsKey(pubblicato.getId()));
    }

    @Test
    void testScambioSuUnAltroAnnuncioNonBlocca() {
        AnnuncioDTO mio = annuncioDatabase.pubblica(creaAnnuncioValido());

        AnnuncioDTO altro = creaAnnuncioValido();
        altro.setTitolo("Corso di chitarra");
        AnnuncioDTO altroPubblicato = annuncioDatabase.pubblica(altro);

        // Lo scambio in corso riguarda solo l'altro annuncio
        creaScambioAccettatoSu(altroPubblicato.getId());

        AnnuncioDTO aggiornato = creaAnnuncioValido();
        aggiornato.setTitolo("Titolo aggiornato");
        AnnuncioDTO esito = annuncioDatabase.modifica(mio.getId(), PROPRIETARIO, aggiornato);

        assertEquals("Titolo aggiornato", esito.getTitolo(),
                "Il blocco vale solo per l'annuncio con lo scambio in corso");
    }

    // --- Sospensione e riattivazione (disponibilita' dell'annuncio) ---

    @Test
    void testAnnuncioNasceDisponibile() {
        AnnuncioDTO pubblicato = annuncioDatabase.pubblica(creaAnnuncioValido());

        assertFalse(pubblicato.isSospeso(), "Un annuncio appena pubblicato non è sospeso");
        assertTrue(pubblicato.isDisponibile(), "Un annuncio appena pubblicato è disponibile");
    }

    @Test
    void testSospendiERiattivaAnnuncio() {
        AnnuncioDTO pubblicato = annuncioDatabase.pubblica(creaAnnuncioValido());

        AnnuncioDTO sospeso = annuncioDatabase.cambiaDisponibilita(pubblicato.getId(), PROPRIETARIO, true);
        assertTrue(sospeso.isSospeso(), "L'annuncio deve risultare sospeso");
        assertTrue(annuncioDatabase.getAnnunciCollection().get(pubblicato.getId()).isSospeso(),
                "Lo stato deve essere persistito");

        AnnuncioDTO riattivato = annuncioDatabase.cambiaDisponibilita(pubblicato.getId(), PROPRIETARIO, false);
        assertFalse(riattivato.isSospeso(), "L'annuncio deve tornare disponibile");
        assertFalse(annuncioDatabase.getAnnunciCollection().get(pubblicato.getId()).isSospeso());
    }

    @Test
    void testCambioDisponibilitaComeNonProprietarioRifiutato() {
        AnnuncioDTO pubblicato = annuncioDatabase.pubblica(creaAnnuncioValido());

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            annuncioDatabase.cambiaDisponibilita(pubblicato.getId(), NON_PROPRIETARIO, true);
        });
        assertEquals("Non autorizzato: l'annuncio appartiene a un altro utente", ex.getMessage());
        assertFalse(annuncioDatabase.getAnnunciCollection().get(pubblicato.getId()).isSospeso(),
                "L'annuncio non deve essere stato sospeso");
    }

    @Test
    void testCambioDisponibilitaBloccatoConScambioInCorso() {
        AnnuncioDTO pubblicato = annuncioDatabase.pubblica(creaAnnuncioValido());
        creaScambioAccettatoSu(pubblicato.getId());

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            annuncioDatabase.cambiaDisponibilita(pubblicato.getId(), PROPRIETARIO, true);
        });
        assertEquals("Non puoi cambiare la disponibilità di un annuncio con uno scambio in corso",
                ex.getMessage());
        assertFalse(annuncioDatabase.getAnnunciCollection().get(pubblicato.getId()).isSospeso());
    }

    @Test
    void testCambioDisponibilitaAnnuncioInesistente() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            annuncioDatabase.cambiaDisponibilita("id-inesistente", PROPRIETARIO, true);
        });
        assertEquals("Annuncio non trovato", ex.getMessage());
    }

    @Test
    void testModificaPreservaLoStatoDiSospensione() {
        AnnuncioDTO pubblicato = annuncioDatabase.pubblica(creaAnnuncioValido());
        annuncioDatabase.cambiaDisponibilita(pubblicato.getId(), PROPRIETARIO, true);

        // Il form di modifica non porta il flag: la modifica non deve riattivare l'annuncio
        AnnuncioDTO aggiornato = creaAnnuncioValido();
        aggiornato.setTitolo("Titolo aggiornato");
        AnnuncioDTO esito = annuncioDatabase.modifica(pubblicato.getId(), PROPRIETARIO, aggiornato);

        assertEquals("Titolo aggiornato", esito.getTitolo());
        assertTrue(esito.isSospeso(), "La modifica non deve riattivare un annuncio sospeso");
    }

    @Test
    void testAnnuncioSospesoRestaFraQuelliDelProprietario() {
        AnnuncioDTO pubblicato = annuncioDatabase.pubblica(creaAnnuncioValido());
        annuncioDatabase.cambiaDisponibilita(pubblicato.getId(), PROPRIETARIO, true);

        // Sparisce dal marketplace, non da "I miei annunci"
        assertEquals(1, annuncioDatabase.annunciDiUtente(PROPRIETARIO).size(),
                "L'annuncio sospeso resta fra quelli del proprietario");
        assertTrue(annuncioDatabase.annunciDiUtente(PROPRIETARIO).get(0).isSospeso());
    }
}
