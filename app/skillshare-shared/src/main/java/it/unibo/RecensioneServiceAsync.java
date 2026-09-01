package it.unibo;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

// Versione asincrona di RecensioneService, quella che usa il client GWT
public interface RecensioneServiceAsync {
    void lascia(RecensioneDTO recensione, AsyncCallback<RecensioneDTO> callback);

    void recensioniPerAnnuncio(String idAnnuncio, AsyncCallback<List<RecensioneDTO>> callback);
}
