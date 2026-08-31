package it.unibo;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("annuncio")
public interface AnnuncioService extends RemoteService {
    /**
     * Pubblica un nuovo annuncio.
     * 
     * @param annuncio L'annuncio da pubblicare, con idUtente già valorizzato.
     * @return AnnuncioDTO salvato, completo di id e dataCreazione.
     * @throws IllegalArgumentException Se manca un campo obbligatorio.
     */
    AnnuncioDTO pubblica(AnnuncioDTO annuncio) throws IllegalArgumentException;

    /**
     * Annunci pubblicati da un utente, dal piu' recente al piu' vecchio.
     *
     * @param idUtente L'identificativo dell'utente di cui leggere gli annunci.
     * @return Lista degli annunci, vuota se l'utente non ne ha.
     */
    List<AnnuncioDTO> annunciDiUtente(String idUtente);

    /**
     * Modifica un annuncio esistente, consentita solo al proprietario.
     *
     * @param idAnnuncio          L'id dell'annuncio da modificare.
     * @param idUtenteRichiedente L'id dell'utente autenticato che richiede la modifica.
     * @param annuncioAggiornato  L'annuncio con i dati aggiornati.
     * @return L'annuncio aggiornato e salvato.
     * @throws IllegalArgumentException Se l'annuncio non esiste, se il richiedente
     *                                  non è il proprietario, se manca un campo obbligatorio
     *                                  o se sull'annuncio c'è uno scambio in corso.
     */
    AnnuncioDTO modifica(String idAnnuncio, String idUtenteRichiedente, AnnuncioDTO annuncioAggiornato) throws IllegalArgumentException;

    /**
     * Rimuove un annuncio esistente, consentita solo al proprietario.
     *
     * @param idAnnuncio          L'id dell'annuncio da rimuovere.
     * @param idUtenteRichiedente L'id dell'utente autenticato che richiede la rimozione.
     * @throws IllegalArgumentException Se l'annuncio non esiste, se il richiedente
     *                                  non è il proprietario o se sull'annuncio
     *                                  c'è uno scambio in corso.
     */
    void rimuovi(String idAnnuncio, String idUtenteRichiedente) throws IllegalArgumentException;
}
