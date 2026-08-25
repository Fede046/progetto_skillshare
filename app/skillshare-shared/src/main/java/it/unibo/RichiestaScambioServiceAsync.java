package it.unibo;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface RichiestaScambioServiceAsync {
    void inviaRichiestaScambio(String idAnnuncio, String idRichiedente, String messaggio,
            AsyncCallback<RichiestaScambioDTO> callback);
}
