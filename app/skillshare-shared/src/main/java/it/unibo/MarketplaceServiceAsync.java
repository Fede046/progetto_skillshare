package it.unibo;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface MarketplaceServiceAsync {
    void listaAnnunci(AsyncCallback<List<AnnuncioDTO>> callback);
}
