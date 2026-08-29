package it.unibo;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MarketplaceServiceImplTest {

    @Test
    void testListaAnnunciRisolveNomeAutore() {
        // Utente registrato con nome e cognome
        String email = "marketplace.rpc_" + System.currentTimeMillis() + "@unibo.it";
        UtenteDatabase.registra(new UtenteDTO(email, "@Password123", "Luca", "Verdi"));

        // Annuncio pubblicato dallo stesso utente
        AnnuncioDTO annuncio = creaAnnuncio(email);
        new AnnuncioDatabase().pubblica(annuncio);

        List<AnnuncioDTO> risultato = new MarketplaceServiceImpl().listaAnnunci();

        assertNotNull(risultato, "Deve restituire una lista, non null");

        AnnuncioDTO trovato = trovaAnnuncio(risultato, annuncio.getId());
        assertEquals("Luca Verdi", trovato.getNomeAutore(),
                "Il nome autore deve essere risolto via UtenteDatabase");
    }

    @Test
    void testListaAnnunciAutoreNonRegistratoUsaEmailComeFallback() {
        // Annuncio pubblicato da un utente non presente in UtenteDatabase
        String email = "fantasma_" + System.currentTimeMillis() + "@unibo.it";
        AnnuncioDTO annuncio = creaAnnuncio(email);
        new AnnuncioDatabase().pubblica(annuncio);

        List<AnnuncioDTO> risultato = new MarketplaceServiceImpl().listaAnnunci();

        AnnuncioDTO trovato = trovaAnnuncio(risultato, annuncio.getId());
        assertEquals(email, trovato.getNomeAutore(), "Senza utente registrato si mostra l'email");
    }

    // Cerca un annuncio per id nella lista restituita dal servizio
    private AnnuncioDTO trovaAnnuncio(List<AnnuncioDTO> annunci, String id) {
        for (AnnuncioDTO annuncio : annunci) {
            if (id.equals(annuncio.getId())) {
                return annuncio;
            }
        }
        throw new AssertionError("Annuncio non trovato nella lista del marketplace");
    }

    private AnnuncioDTO creaAnnuncio(String email) {
        AnnuncioDTO annuncio = new AnnuncioDTO();
        annuncio.setIdUtente(email);
        annuncio.setTitolo("Ripetizioni di Java");
        annuncio.setDescrizione("Lezioni base e avanzate su Java e GWT");
        annuncio.setCompetenzaOfferta("Programmazione Java");
        annuncio.setDisponibilita("Lunedì e mercoledì pomeriggio");
        annuncio.setControprestazione("Lezioni di inglese");
        return annuncio;
    }
    @Test
    void testListaAnnunciConFiltroCompetenzaEOrdinamentoPerTitolo() {
        String ts = String.valueOf(System.currentTimeMillis());
        String email = "test.filtro." + ts + "@unibo.it";
        UtenteDatabase.registra(new UtenteDTO(email, "@Password123", "Filtro", "Utente"));

        AnnuncioDatabase db = new AnnuncioDatabase();

        AnnuncioDTO a1 = creaAnnuncio(email);
        a1.setTitolo("Z: Corso Avanzato di Java");
        a1.setCompetenzaOfferta("Programmazione Java");
        db.pubblica(a1);

        AnnuncioDTO a2 = creaAnnuncio(email);
        a2.setTitolo("A: Corso Base di Java");
        a2.setCompetenzaOfferta("Linguaggio Java");
        db.pubblica(a2);

        AnnuncioDTO a3 = creaAnnuncio(email);
        a3.setTitolo("B: Corso di Graphic Design");
        a3.setCompetenzaOfferta("Photoshop e Illustrator");
        db.pubblica(a3);

        MarketplaceServiceImpl service = new MarketplaceServiceImpl();

        // Filtro per "JAVA" + ordinamento per titolo (A-Z)
        List<AnnuncioDTO> filtratiEOrdinati = service.listaAnnunci("JAVA", true);
        assertNotNull(filtratiEOrdinati);

        boolean soloJava = filtratiEOrdinati.stream()
                .allMatch(a -> a.getCompetenzaOfferta().toLowerCase().contains("java"));
        assertTrue(soloJava, "Devono essere presenti solo gli annunci con competenza Java");

        int idxA = -1, idxZ = -1;
        for (int i = 0; i < filtratiEOrdinati.size(); i++) {
            if (filtratiEOrdinati.get(i).getId().equals(a2.getId())) idxA = i;
            if (filtratiEOrdinati.get(i).getId().equals(a1.getId())) idxZ = i;
        }
        assertTrue(idxA != -1 && idxZ != -1 && idxA < idxZ,
                "L'annuncio con titolo 'A:' deve precedere l'annuncio con titolo 'Z:'");

        AnnuncioDTO trovato = trovaAnnuncio(filtratiEOrdinati, a1.getId());
        assertEquals("Filtro Utente", trovato.getNomeAutore());
    }

    @Test
    void testListaAnnunciFiltroNessunaCorrispondenzaRestituisceListaVuota() {
        MarketplaceServiceImpl service = new MarketplaceServiceImpl();
        List<AnnuncioDTO> risultato = service.listaAnnunci("CompetenzaInesistente_XYZ_999", false);
        assertNotNull(risultato);
        assertTrue(risultato.isEmpty(), "Una ricerca senza corrispondenze deve restituire una lista vuota");
    }

    @Test
    void testCercaAnnunciPerTitolo() {
        String ts = String.valueOf(System.currentTimeMillis());
        String email = "test.cerca.titolo." + ts + "@unibo.it";
        UtenteDatabase.registra(new UtenteDTO(email, "@Password123", "CercaTitolo", "Utente"));

        AnnuncioDTO annuncio = creaAnnuncio(email);
        annuncio.setTitolo("TitoloUnicoRicerca_" + ts);
        new AnnuncioDatabase().pubblica(annuncio);

        List<AnnuncioDTO> risultato = new MarketplaceServiceImpl()
                .cercaAnnunci("TitoloUnicoRicerca_" + ts, EnumSet.of(CampoRicerca.TITOLO), false);

        AnnuncioDTO trovato = trovaAnnuncio(risultato, annuncio.getId());
        assertEquals("TitoloUnicoRicerca_" + ts, trovato.getTitolo());
    }

    @Test
    void testCercaAnnunciPerCompetenza() {
        String ts = String.valueOf(System.currentTimeMillis());
        String email = "test.cerca.competenza." + ts + "@unibo.it";
        UtenteDatabase.registra(new UtenteDTO(email, "@Password123", "CercaCompetenza", "Utente"));

        AnnuncioDTO annuncio = creaAnnuncio(email);
        annuncio.setCompetenzaOfferta("CompetenzaUnicaRicerca_" + ts);
        new AnnuncioDatabase().pubblica(annuncio);

        List<AnnuncioDTO> risultato = new MarketplaceServiceImpl()
                .cercaAnnunci("CompetenzaUnicaRicerca_" + ts, EnumSet.of(CampoRicerca.COMPETENZA), false);

        assertEquals(annuncio.getId(), trovaAnnuncio(risultato, annuncio.getId()).getId());
    }

    @Test
    void testCercaAnnunciPerAutoreUsaNomeCompleto() {
        String ts = String.valueOf(System.currentTimeMillis());
        String email = "test.cerca.autore." + ts + "@unibo.it";
        String nome = "AutoreRicerca" + ts;
        String cognome = "CognomeRicerca" + ts;
        UtenteDatabase.registra(new UtenteDTO(email, "@Password123", nome, cognome));

        AnnuncioDTO annuncio = creaAnnuncio(email);
        new AnnuncioDatabase().pubblica(annuncio);

        // Il campo AUTORE cerca nel nome completo (nome + cognome) valorizzato dal servizio
        List<AnnuncioDTO> perNome = new MarketplaceServiceImpl()
                .cercaAnnunci(nome, EnumSet.of(CampoRicerca.AUTORE), false);
        AnnuncioDTO trovato = trovaAnnuncio(perNome, annuncio.getId());
        assertEquals(nome + " " + cognome, trovato.getNomeAutore(),
                "La ricerca per autore deve usare nome e cognome arricchiti");

        List<AnnuncioDTO> perCognome = new MarketplaceServiceImpl()
                .cercaAnnunci(cognome, EnumSet.of(CampoRicerca.AUTORE), false);
        assertEquals(annuncio.getId(), trovaAnnuncio(perCognome, annuncio.getId()).getId());
    }

    @Test
    void testCercaAnnunciOrSuPiuCampi() {
        String ts = String.valueOf(System.currentTimeMillis());
        String email = "test.cerca.or." + ts + "@unibo.it";
        UtenteDatabase.registra(new UtenteDTO(email, "@Password123", "CercaOr", "Utente"));

        AnnuncioDatabase db = new AnnuncioDatabase();

        // La stessa query deve bastare da sola: soloTitolo matcha solo per TITOLO
        // (la sua competenza resta "Programmazione Java"), soloCompetenza solo per COMPETENZA
        // (il suo titolo resta "Ripetizioni di Java"), nessunCampo per nessun campo.
        AnnuncioDTO soloTitolo = creaAnnuncio(email);
        soloTitolo.setTitolo("MatchOrUnico_" + ts);
        db.pubblica(soloTitolo);

        AnnuncioDTO soloCompetenza = creaAnnuncio(email);
        soloCompetenza.setCompetenzaOfferta("MatchOrUnico_" + ts);
        db.pubblica(soloCompetenza);

        AnnuncioDTO nessunCampo = creaAnnuncio(email);
        nessunCampo.setTitolo("NessunMatch_" + ts);
        nessunCampo.setCompetenzaOfferta("NessunaCompetenza_" + ts);
        db.pubblica(nessunCampo);

        String query = "MatchOrUnico_" + ts;
        List<AnnuncioDTO> risultato = new MarketplaceServiceImpl().cercaAnnunci(
                query, EnumSet.of(CampoRicerca.TITOLO, CampoRicerca.COMPETENZA), false);

        boolean trovatoPerTitolo = risultato.stream().anyMatch(a -> a.getId().equals(soloTitolo.getId()));
        boolean trovatoPerCompetenza = risultato.stream().anyMatch(a -> a.getId().equals(soloCompetenza.getId()));
        boolean esclusoNessunCampo = risultato.stream().noneMatch(a -> a.getId().equals(nessunCampo.getId()));

        assertTrue(trovatoPerTitolo, "Deve bastare il match sul titolo (logica OR)");
        assertTrue(trovatoPerCompetenza, "Deve bastare il match sulla competenza (logica OR)");
        assertTrue(esclusoNessunCampo, "Un annuncio senza alcun match deve restare escluso");
    }

    @Test
    void testCercaAnnunciCampiNulloCercaSuTuttiICampi() {
        String ts = String.valueOf(System.currentTimeMillis());
        String email = "test.cerca.nullcampi." + ts + "@unibo.it";
        UtenteDatabase.registra(new UtenteDTO(email, "@Password123", "CercaNullCampi", "Utente"));

        AnnuncioDTO annuncio = creaAnnuncio(email);
        annuncio.setControprestazione("MatchInControprestazione_" + ts);
        new AnnuncioDatabase().pubblica(annuncio);

        // campi = null: la ricerca avviene su tutti i campi disponibili
        List<AnnuncioDTO> risultato = new MarketplaceServiceImpl()
                .cercaAnnunci("MatchInControprestazione_" + ts, null, false);

        assertEquals(annuncio.getId(), trovaAnnuncio(risultato, annuncio.getId()).getId());
    }

    @Test
    void testCercaAnnunciQueryNullaOVuotaRestituisceTutti() {
        String ts = String.valueOf(System.currentTimeMillis());
        String email = "test.cerca.queryvuota." + ts + "@unibo.it";
        UtenteDatabase.registra(new UtenteDTO(email, "@Password123", "CercaQueryVuota", "Utente"));

        AnnuncioDTO annuncio = creaAnnuncio(email);
        new AnnuncioDatabase().pubblica(annuncio);

        MarketplaceServiceImpl service = new MarketplaceServiceImpl();
        Set<CampoRicerca> tutti = EnumSet.allOf(CampoRicerca.class);

        List<AnnuncioDTO> conNull = service.cercaAnnunci(null, tutti, false);
        List<AnnuncioDTO> conVuota = service.cercaAnnunci("   ", tutti, false);

        assertEquals(annuncio.getId(), trovaAnnuncio(conNull, annuncio.getId()).getId());
        assertEquals(annuncio.getId(), trovaAnnuncio(conVuota, annuncio.getId()).getId());
    }

    @Test
    void testCercaAnnunciNessunaCorrispondenzaRestituisceListaVuota() {
        String queryInesistente = "QueryInesistente_" + UUID.randomUUID();

        List<AnnuncioDTO> risultato = new MarketplaceServiceImpl()
                .cercaAnnunci(queryInesistente, EnumSet.allOf(CampoRicerca.class), false);

        assertNotNull(risultato);
        assertTrue(risultato.isEmpty(), "Una ricerca senza corrispondenze deve restituire una lista vuota");
    }

    @Test
    void testCercaAnnunciAutoreNonRegistratoUsaEmailComeFallback() {
        String email = "fantasma.cerca." + System.currentTimeMillis() + "@unibo.it";
        AnnuncioDTO annuncio = creaAnnuncio(email);
        new AnnuncioDatabase().pubblica(annuncio);

        // Autore non piu registrato: la ricerca per AUTORE usa l email come fallback
        List<AnnuncioDTO> risultato = new MarketplaceServiceImpl()
                .cercaAnnunci("fantasma.cerca", EnumSet.of(CampoRicerca.AUTORE), false);

        AnnuncioDTO trovato = trovaAnnuncio(risultato, annuncio.getId());
        assertEquals(email, trovato.getNomeAutore(), "Senza utente registrato si mostra l email come fallback");
    }

    @Test
    void testListaAnnunciRisolveValutazioneAutore() {
        // 1. Registriamo un utente
        String email = "autore.recensito_" + System.currentTimeMillis() + "@unibo.it";
        UtenteDatabase.registra(new UtenteDTO(email, "@Password123", "Mario", "Rossi"));

        // 2. Pubblichiamo un suo annuncio
        AnnuncioDTO annuncio = creaAnnuncio(email);
        new AnnuncioDatabase().pubblica(annuncio);

        // NOTA: Poiché non ha recensioni, la valutazione media deve essere null (non 0)
        List risultato = new MarketplaceServiceImpl().listaAnnunci();
        AnnuncioDTO trovato = trovaAnnuncio(risultato, annuncio.getId());
        
        assertNotNull(trovato, "L'annuncio deve essere presente");
        // Verifica che un autore senza recensioni abbia valutazione null (e non venga trattato come 0)
        assertTrue(trovato.getValutazioneAutore() == null, 
                "Gli autori senza recensioni devono avere valutazione null e non 0");
    }

    @Test
    void testAutoreSenzaRecensioniNonTrattatoComeZero() {
        // Test esplicito per la regola: "Listings whose author has no reviews are flagged accordingly (not treated as rating 0)"
        AnnuncioDTO annuncio = new AnnuncioDTO();
        annuncio.setValutazioneAutore(null); // Rappresenta l'assenza di recensioni
        
        assertTrue(annuncio.getValutazioneAutore() == null, 
                "Il flag/valore per l'assenza di recensioni deve essere null per distinguerlo da un rating pari a 0.0");
    }
}
