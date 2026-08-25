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
     * Richieste di scambio ricevute da un creatore di annunci,
     * dalla più recente alla più vecchia.
     *
     * @param idCreatore L'id del creatore dell'annuncio.
     * @return Lista delle richieste ricevute, vuota se l'id è nullo/vuoto o non ce ne sono.
     */
    List<RichiestaScambioDTO> richiesteRicevuteDaCreatore(String idCreatore);

    /**
     * Richieste di scambio inviate da un utente richiedente,
     * dalla più recente alla più vecchia.
     *
     * @param idRichiedente L'id dell'utente che ha inviato le richieste.
     * @return Lista delle richieste inviate, vuota se l'id è nullo/vuoto o non ce ne sono.
     */
    List<RichiestaScambioDTO> richiesteInviateDaRichiedente(String idRichiedente);

    /**
     * Accetta una richiesta di scambio: solo il creatore dell'annuncio può accettarla.
     *
     * @param idRichiesta L'id della richiesta da accettare.
     * @param idCreatore  L'id dell'utente autenticato che accetta (deve essere il creatore dell'annuncio).
     * @return La richiesta aggiornata con stato ACCEPTED.
     * @throws IllegalArgumentException Se i dati non sono validi, se la richiesta non esiste
     *                                  o se l'utente non è il creatore dell'annuncio.
     */
    RichiestaScambioDTO accetta(String idRichiesta, String idCreatore) throws IllegalArgumentException;

    /**
     * Rifiuta una richiesta di scambio: solo il creatore dell'annuncio può rifiutarla.
     *
     * @param idRichiesta L'id della richiesta da rifiutare.
     * @param idCreatore  L'id dell'utente autenticato che rifiuta (deve essere il creatore dell'annuncio).
     * @return La richiesta aggiornata con stato REJECTED.
     * @throws IllegalArgumentException Se i dati non sono validi, se la richiesta non esiste
     *                                  o se l'utente non è il creatore dell'annuncio.
     */
    RichiestaScambioDTO rifiuta(String idRichiesta, String idCreatore) throws IllegalArgumentException;
}
