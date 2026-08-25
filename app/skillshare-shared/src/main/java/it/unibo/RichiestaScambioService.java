package it.unibo;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("richiestaScambio")
public interface RichiestaScambioService extends RemoteService {
    /**
     * Invia una richiesta di scambio per un annuncio.
     * Il creatore dell'annuncio viene risolto dal server.
     *
     * @param idAnnuncio    L'id dell'annuncio oggetto dello scambio.
     * @param idRichiedente L'id dell'utente che invia la richiesta.
     * @param messaggio     Messaggio opzionale per il creatore (può essere null).
     * @return La richiesta salvata, con stato PENDING, id e dataCreazione valorizzati.
     * @throws IllegalArgumentException Se i dati non sono validi, se l'annuncio non esiste
     *                                  o se il richiedente è il creatore dell'annuncio.
     */
    RichiestaScambioDTO inviaRichiestaScambio(String idAnnuncio, String idRichiedente, String messaggio)
            throws IllegalArgumentException;
            /**
     * Recupera tutte le richieste di scambio ricevute dall'utente (in quanto creatore dell'annuncio).
     */
    List<RichiestaScambioDTO> richiesteRicevuteDaCreatore(String emailCreatore);

    /**
     * Recupera tutte le richieste di scambio inviate dall'utente.
     */
    List<RichiestaScambioDTO> richiesteInviateDaRichiedente(String emailRichiedente);

    /**
     * Accetta una richiesta di scambio.
     */
    RichiestaScambioDTO accetta(String idRichiesta, String emailCreatore) throws IllegalArgumentException;

    /**
     * Rifiuta una richiesta di scambio.
     */
    RichiestaScambioDTO rifiuta(String idRichiesta, String emailCreatore) throws IllegalArgumentException;
}
