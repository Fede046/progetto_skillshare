package it.unibo;

import java.util.List;
import java.util.Set;

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

    /**
     * Ricerca testuale sugli annunci su uno o piu' campi selezionabili.
     * La query viene cercata come sottostringa (case-insensitive) e un annuncio
     * e' incluso se corrisponde in ALMENO UNO dei campi selezionati (logica OR).
     * Per ogni annuncio viene valorizzato nomeAutore con il nome completo dell'autore.
     *
     * @param query           Testo da ricercare. Se nullo o vuoto non applica alcun filtro
     *                        (restituisce tutti gli annunci, eventualmente ordinati).
     * @param campi           Insieme dei campi in cui cercare. Se nullo o vuoto, la ricerca
     *                        viene effettuata su TUTTI i campi disponibili.
     * @param ordinaPerTitolo Se true ordina alfabeticamente per titolo (A-Z);
     *                        se false mantiene l'ordine temporale decrescente.
     * @return Lista degli annunci corrispondenti, filtrati/ordinati.
     */
    List<AnnuncioDTO> cercaAnnunci(String query, Set<CampoRicerca> campi, boolean ordinaPerTitolo);
}
