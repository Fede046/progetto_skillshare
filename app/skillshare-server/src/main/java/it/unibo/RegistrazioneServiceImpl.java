package it.unibo;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;

public class RegistrazioneServiceImpl extends RemoteServiceServlet implements RegistrazioneService {
    
    private final UtenteDatabase utenteDatabase = new UtenteDatabase();

    @Override
    public boolean registraUtente(UtenteDTO utente) throws IllegalArgumentException {
        return utenteDatabase.registra(utente);
    }
}