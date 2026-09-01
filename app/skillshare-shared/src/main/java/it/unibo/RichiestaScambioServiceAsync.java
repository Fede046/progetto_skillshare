package it.unibo;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

// Versione asincrona di RichiestaScambioService, quella che usa il client GWT
public interface RichiestaScambioServiceAsync {
    void inviaRichiestaScambio(String idAnnuncio, String idRichiedente, String messaggio,
            AsyncCallback<RichiestaScambioDTO> callback);

    void richiesteRicevuteDaCreatore(String idCreatore, AsyncCallback<List<RichiestaScambioDTO>> callback);

    void richiesteInviateDaRichiedente(String idRichiedente, AsyncCallback<List<RichiestaScambioDTO>> callback);

    void accetta(String idRichiesta, String idCreatore, AsyncCallback<RichiestaScambioDTO> callback);

    void rifiuta(String idRichiesta, String idCreatore, AsyncCallback<RichiestaScambioDTO> callback);

    void completa(String idRichiesta, String idUtente, AsyncCallback<RichiestaScambioDTO> callback);
}
