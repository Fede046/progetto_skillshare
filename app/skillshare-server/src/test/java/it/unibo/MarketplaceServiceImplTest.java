package it.unibo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
}
