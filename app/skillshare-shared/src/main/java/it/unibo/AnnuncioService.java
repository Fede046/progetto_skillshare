package it.unibo;

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
}
