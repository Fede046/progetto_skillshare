package it.unibo;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface RichiestaScambioServiceAsync {
    void inviaRichiestaScambio(String idAnnuncio, String idRichiedente, String messaggio,
            AsyncCallback<RichiestaScambioDTO> callback);
            void richiesteRicevuteDaCreatore(String emailCreatore,
            AsyncCallback<List<RichiestaScambioDTO>> callback);

    void richiesteInviateDaRichiedente(String emailRichiedente,
            AsyncCallback<List<RichiestaScambioDTO>> callback);

    void accetta(String idRichiesta, String emailCreatore,
            AsyncCallback<RichiestaScambioDTO> callback);

    void rifiuta(String idRichiesta, String emailCreatore,
            AsyncCallback<RichiestaScambioDTO> callback);
}
