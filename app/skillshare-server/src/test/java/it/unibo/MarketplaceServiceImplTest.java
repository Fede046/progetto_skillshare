package it.unibo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
}
