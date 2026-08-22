package it.unibo;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AnnuncioServiceAsync {
    void pubblica(AnnuncioDTO annuncio, AsyncCallback<AnnuncioDTO> callback);
}
