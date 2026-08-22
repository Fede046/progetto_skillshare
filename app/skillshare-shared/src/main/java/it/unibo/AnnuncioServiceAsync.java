package it.unibo;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AnnuncioServiceAsync {
    void pubblica(AnnuncioDTO annuncio, AsyncCallback<AnnuncioDTO> callback);

    void annunciDiUtente(String idUtente, AsyncCallback<List<AnnuncioDTO>> callback);
}
