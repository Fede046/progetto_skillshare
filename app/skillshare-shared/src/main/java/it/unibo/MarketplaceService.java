package it.unibo;

import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

// Servizio RPC per la vetrina degli annunci: ricerca, filtri e ordinamenti
@RemoteServiceRelativePath("marketplace")
public interface MarketplaceService extends RemoteService {
    // Annunci del marketplace, dal piu' recente al piu' vecchio. Per ogni annuncio viene valorizzato
    // nomeAutore con il nome completo dell'utente che lo ha pubblicato.
    List<AnnuncioDTO> listaAnnunci();

    // Annunci del marketplace filtrati per competenza e opzionalmente ordinati per titolo. Per ogni
    // annuncio viene valorizzato nomeAutore con il nome completo dell'autore.
    List<AnnuncioDTO> listaAnnunci(String filtroCompetenza, boolean ordinaPerTitolo, boolean ordinaPerRating);

    // Ricerca testuale sugli annunci su uno o piu' campi selezionabili.
    List<AnnuncioDTO> cercaAnnunci(String query, Set<CampoRicerca> campi, boolean ordinaPerTitolo, boolean ordinaPerRating);
}
