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
}
