package it.unibo;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("marketplace")
public interface MarketplaceService extends RemoteService {
    /**
     * Annunci del marketplace, dal piu' recente al piu' vecchio.
     * Per ogni annuncio viene valorizzato nomeAutore con il nome completo
     * dell'utente che lo ha pubblicato.
     *
     * @return Lista di tutti gli annunci pubblicati, vuota se non ce ne sono.
     */
    List<AnnuncioDTO> listaAnnunci();
    /**
     * Annunci del marketplace filtrati per competenza e opzionalmente ordinati per titolo.
     * Per ogni annuncio viene valorizzato nomeAutore con il nome completo dell'autore.
     *
     * @param filtroCompetenza Testo da ricercare nella competenza offerta (case-insensitive).
     *                         Se nullo o vuoto restituisce tutti gli annunci.
     * @param ordinaPerTitolo  Se true ordina alfabeticamente per titolo (A-Z);
     *                         se false mantiene l'ordine temporale decrescente.
     * @return Lista degli annunci filtrati/ordinati.
     */
    List<AnnuncioDTO> listaAnnunci(String filtroCompetenza, boolean ordinaPerTitolo);
}
