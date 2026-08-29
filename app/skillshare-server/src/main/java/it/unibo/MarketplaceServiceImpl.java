package it.unibo;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;

public class MarketplaceServiceImpl extends RemoteServiceServlet implements MarketplaceService {

    private static final long serialVersionUID = 1L;

    @Override
    public List<AnnuncioDTO> listaAnnunci() {
        return listaAnnunci(null, false);
    }

    @Override
    public List<AnnuncioDTO> listaAnnunci(String filtroCompetenza, boolean ordinaPerTitolo) {
        AnnuncioDatabase db = new AnnuncioDatabase();

        // 1. Filtraggio per competenza (se il filtro è nullo/vuoto restituisce tutti
        // gli annunci)
        List<AnnuncioDTO> annunci = db.filtraPerCompetenza(filtroCompetenza);

        // 2. Ordinamento alfabetico per titolo se richiesto
        if (ordinaPerTitolo) {
            annunci = db.ordinaPerTitolo(annunci);
        }

        // 3. Arricchimento con il nome completo e la valutazione dell'autore
        for (AnnuncioDTO annuncio : annunci) {
            annuncio.setNomeAutore(nomeAutore(annuncio.getIdUtente()));
            annuncio.setValutazioneAutore(valutazioneAutore(annuncio.getIdUtente()));
        }

        return annunci;
    }

    @Override
    public List<AnnuncioDTO> cercaAnnunci(String query, Set<CampoRicerca> campi, boolean ordinaPerTitolo) {
        AnnuncioDatabase db = new AnnuncioDatabase();

        // 1. Parto da tutti gli annunci in ordine temporale decrescente
        List<AnnuncioDTO> tutti = db.tuttiGliAnnunci();

        // 2. Arricchimento con il nome autore e valutazione PRIMA del filtro
        for (AnnuncioDTO annuncio : tutti) {
            annuncio.setNomeAutore(nomeAutore(annuncio.getIdUtente()));
            annuncio.setValutazioneAutore(valutazioneAutore(annuncio.getIdUtente()));
        }

        // 3. Filtro testuale OR sui campi selezionati
        List<AnnuncioDTO> risultato = new ArrayList<>();
        for (AnnuncioDTO annuncio : tutti) {
            if (corrisponde(annuncio, query, campi)) {
                risultato.add(annuncio);
            }
        }

        // 4. Ordinamento alfabetico per titolo se richiesto
        if (ordinaPerTitolo) {
            risultato = db.ordinaPerTitolo(risultato);
        }

        return risultato;
    }

    /**
     * Verifica se un annuncio corrisponde alla query in almeno uno dei campi
     * selezionati (OR).
     * Query nulla/vuota => sempre true (nessun filtro).
     * Campi nulli/vuoti => ricerca su tutti i campi disponibili.
     */
    private boolean corrisponde(AnnuncioDTO a, String query, Set<CampoRicerca> campi) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }
        String q = query.trim().toLowerCase();

        Set<CampoRicerca> attivi = (campi == null || campi.isEmpty())
                ? EnumSet.allOf(CampoRicerca.class)
                : campi;

        for (CampoRicerca campo : attivi) {
            String valore = valoreCampo(a, campo);
            if (valore != null && valore.toLowerCase().contains(q)) {
                return true; // basta un campo che matcha
            }
        }
        return false;
    }

    // Estrae il valore testuale dell'annuncio corrispondente al campo di ricerca
    private String valoreCampo(AnnuncioDTO a, CampoRicerca campo) {
        switch (campo) {
            case TITOLO:
                return a.getTitolo();
            case COMPETENZA:
                return a.getCompetenzaOfferta();
            case CONTROPRESTAZIONE:
                return a.getControprestazione();
            case AUTORE:
                return a.getNomeAutore();
            default:
                return null;
        }
    }

    // Nome e cognome dell'autore, con fallback sull'email se l'utente non esiste
    // piu'
    private String nomeAutore(String idUtente) {
        try {
            UtenteDTO autore = UtenteDatabase.getProfilo(idUtente);
            String nome = autore.getNome() != null ? autore.getNome().trim() : "";
            String cognome = autore.getCognome() != null ? autore.getCognome().trim() : "";
            String completo = (nome + " " + cognome).trim();
            return completo.isEmpty() ? idUtente : completo;
        } catch (IllegalArgumentException e) {
            // Autore non piu' registrato: resta visibile la sua email
            return idUtente;
        }
    }

    // Valutazione media dell'autore, con restituzione di null se non ci sono
    // recensioni
    private Double valutazioneAutore(String idUtente) {
        try {
            RecensioneDatabase recDb = new RecensioneDatabase();
            return recDb.ratingMedio(idUtente);
        } catch (Exception e) {
            // In caso di problemi con il database, restituisce null (nessuna valutazione)
            return null;
        }
    }
}
