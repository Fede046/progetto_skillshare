package it.unibo;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;

// Adattatore RPC per la registrazione: gira la richiesta a UtenteDatabase
public class RegistrazioneServiceImpl extends RemoteServiceServlet implements RegistrazioneService {
    
    private final UtenteDatabase utenteDatabase = new UtenteDatabase();

    @Override
    public boolean registraUtente(UtenteDTO utente) throws IllegalArgumentException {
        return utenteDatabase.registra(utente);
    }
}