package it.unibo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test di integrazione RPC per la ricerca avanzata del marketplace (US-9).
 * Verifica filtro per competenza, assenza di corrispondenze e ordinamento
 * per titolo passando dal servizio, non dal repository.
 */
public class MarketplaceRicercaAvanzataTest {

    private MarketplaceServiceImpl service;
    private AnnuncioDatabase db;
    private String email;

    /**
     * Il servizio legge dal database condiviso, dove restano gli annunci degli
     * altri test: ogni esecuzione usa quindi una competenza e un utente propri,
     * cosi' i filtri pescano solo cio' che il test ha appena pubblicato.
     */
    private String competenzaUnivoca;

    @BeforeEach
    void setUp() {
        String ts = String.valueOf(System.currentTimeMillis()) + "-" + System.nanoTime();
        service = new MarketplaceServiceImpl();
        db = new AnnuncioDatabase();
        competenzaUnivoca = "Uncinetto" + ts;

        email = "ricerca." + ts + "@unibo.it";
        UtenteDatabase.registra(new UtenteDTO(email, "@Password123", "Anna", "Ricerca"));
    }

    @Test
    void testFiltroCompetenzaRestituisceSoloAnnunciCorrispondentiIgnorandoMaiuscole() {
        // Due annunci con la competenza cercata, uno con una competenza diversa
        pubblica("Corso di uncinetto base", competenzaUnivoca);
        pubblica("Corso di uncinetto avanzato", competenzaUnivoca);
        pubblica("Partite di scacchi", "Scacchi" + competenzaUnivoca.hashCode());

        // La query arriva tutta in maiuscolo: il filtro deve ignorare il caso
        List<AnnuncioDTO> risultato = service.listaAnnunci(competenzaUnivoca.toUpperCase(), false);

        assertNotNull(risultato, "Il servizio deve restituire una lista, non null");
        assertEquals(2, risultato.size(), "Devono tornare solo i due annunci con la competenza cercata");

        for (AnnuncioDTO annuncio : risultato) {
            assertEquals(competenzaUnivoca, annuncio.getCompetenzaOfferta(),
                    "Nessun annuncio con competenza diversa deve finire nel risultato");
        }
    }

    @Test
    void testFiltroSenzaCorrispondenzeRestituisceListaVuota() {
        pubblica("Corso di uncinetto base", competenzaUnivoca);

        List<AnnuncioDTO> risultato = service.listaAnnunci("competenza-inesistente-" + competenzaUnivoca, false);

        assertNotNull(risultato, "Il servizio deve restituire una lista, non null");
        assertTrue(risultato.isEmpty(), "Un filtro senza corrispondenze restituisce lista vuota");
    }

    @Test
    void testOrdinamentoPerTitoloRestituisceAnnunciInOrdineAlfabetico() {
        // Pubblicati fuori ordine: senza ordinamento uscirebbero dal piu' recente
        pubblica("Zaino da trekking", competenzaUnivoca);
        pubblica("Aquilone artigianale", competenzaUnivoca);
        pubblica("Mandolino napoletano", competenzaUnivoca);

        List<AnnuncioDTO> risultato = service.listaAnnunci(competenzaUnivoca, true);

        assertEquals(3, risultato.size(), "Il filtro deve trovare i tre annunci del test");
        assertEquals("Aquilone artigianale", risultato.get(0).getTitolo());
        assertEquals("Mandolino napoletano", risultato.get(1).getTitolo());
        assertEquals("Zaino da trekking", risultato.get(2).getTitolo());
    }

    // Pubblica un annuncio dell'utente di test con titolo e competenza dati
    private void pubblica(String titolo, String competenza) {
        AnnuncioDTO annuncio = new AnnuncioDTO();
        annuncio.setIdUtente(email);
        annuncio.setTitolo(titolo);
        annuncio.setDescrizione("Annuncio creato dal test di ricerca avanzata");
        annuncio.setCompetenzaOfferta(competenza);
        annuncio.setDisponibilita("Su appuntamento");
        annuncio.setControprestazione("Da concordare");
        db.pubblica(annuncio);
    }
}
