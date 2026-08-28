package it.unibo;

import java.util.List;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("chatService")
public interface ChatService extends RemoteService {

    /**
     * Invia un messaggio di chat associato a una richiesta di scambio.
     */
    MessaggioDTO inviaMessaggio(MessaggioDTO messaggio) throws IllegalArgumentException;

    /**
     * Restituisce la lista dei messaggi per una specifica richiesta di scambio.
     */
    List getMessaggi(String idRichiestaScambio) throws IllegalArgumentException;
}