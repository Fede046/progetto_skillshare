package it.unibo;

import java.util.List;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;

// Adattatore RPC per la chat: delega a MessaggioDatabase, che controlla permessi e stato
public class ChatServiceImpl extends RemoteServiceServlet implements ChatService {

    private static final long serialVersionUID = 1L;

    private final MessaggioDatabase messaggioDatabase;

    public ChatServiceImpl() {
        this(new MessaggioDatabase());
    }

    // Costruttore usato dai test per iniettare il database 
    public ChatServiceImpl(MessaggioDatabase messaggioDatabase) {
        this.messaggioDatabase = messaggioDatabase;
    }

    @Override
    public MessaggioDTO inviaMessaggio(MessaggioDTO messaggio) throws IllegalArgumentException {
        if (messaggio == null) {
            throw new IllegalArgumentException("Dati del messaggio non validi");
        }
        // Delega direttamente a MessaggioDatabase senza logica aggiuntiva nel servlet
        return messaggioDatabase.inviaMessaggio(messaggio);
    }

    @Override
    public List<MessaggioDTO> getMessaggi(String idRichiestaScambio, String idUtenteRichiedente)
            throws IllegalArgumentException {
        // Autorizzazione e filtro vivono in MessaggioDatabase
        return messaggioDatabase.getMessaggi(idRichiestaScambio, idUtenteRichiedente);
    }
}