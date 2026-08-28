package it.unibo;

import java.util.List;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;

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
    public List getMessaggi(String idRichiestaScambio) throws IllegalArgumentException {

        String idUtenteAutenticato = null;
        if (getThreadLocalRequest() != null && getThreadLocalRequest().getSession() != null) {
            Object userSession = getThreadLocalRequest().getSession().getAttribute("user");
            if (userSession instanceof String) {
                idUtenteAutenticato = (String) userSession;
            } else if (userSession instanceof UtenteDTO) {
                idUtenteAutenticato = ((UtenteDTO) userSession).getEmail();
            }
        }

       
        if (idUtenteAutenticato == null || idUtenteAutenticato.trim().isEmpty()) {
            // Fallback di sicurezza o eccezione se l'utente non è autenticato in sessione
            throw new IllegalArgumentException("Utente non autenticato");
        }

        return messaggioDatabase.getMessaggi(idRichiestaScambio, idUtenteAutenticato);
    }
}