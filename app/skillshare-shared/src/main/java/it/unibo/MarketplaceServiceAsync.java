package it.unibo;

import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;

// Versione asincrona di MarketplaceService, quella che usa il client GWT
public interface MarketplaceServiceAsync {
    void listaAnnunci(AsyncCallback<List<AnnuncioDTO>> callback);

    void listaAnnunci(String filtroCompetenza, boolean ordinaPerTitolo,boolean ordinaPerRating, AsyncCallback<List<AnnuncioDTO>> callback);

    void cercaAnnunci(String query, Set<CampoRicerca> campi, boolean ordinaPerTitolo,boolean ordinaPerRating, AsyncCallback<List<AnnuncioDTO>> callback);
}
