package it.unibo;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface MarketplaceServiceAsync {
    void listaAnnunci(AsyncCallback<List<AnnuncioDTO>> callback);
    void listaAnnunci(String filtroCompetenza, boolean ordinaPerTitolo, AsyncCallback<List<AnnuncioDTO>> callback);
}
