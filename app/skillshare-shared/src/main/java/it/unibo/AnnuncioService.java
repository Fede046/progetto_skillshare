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
}
