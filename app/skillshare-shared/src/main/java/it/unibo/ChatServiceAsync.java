package it.unibo;

import java.util.List;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ChatServiceAsync {

    void inviaMessaggio(MessaggioDTO messaggio, AsyncCallback<MessaggioDTO> callback);

    void getMessaggi(String idRichiestaScambio, String idUtenteRichiedente,
            AsyncCallback<List<MessaggioDTO>> callback);
}